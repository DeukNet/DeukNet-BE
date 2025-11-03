package org.example.deuknetinfrastructure.external.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.example.deuknetinfrastructure.external.search.document.CommentDocument;
import org.example.deuknetinfrastructure.external.search.exception.SearchOperationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Comment 검색 Adapter
 *
 * CommentDocument를 활용한 댓글 검색 기능을 제공합니다.
 * - 댓글 내용 전문 검색
 * - 게시글별 댓글 조회
 * - 작성자별 댓글 조회
 * - 대댓글 필터링
 */
@Component
@RequiredArgsConstructor
public class CommentSearchAdapter {

    private static final String INDEX_NAME = "comments";
    private final ElasticsearchClient elasticsearchClient;

    /**
     * ID로 댓글 조회
     */
    public Optional<CommentDocument> findById(UUID id) {
        try {
            var response = elasticsearchClient.get(g -> g
                    .index(INDEX_NAME)
                    .id(id.toString()),
                CommentDocument.class
            );

            if (response.found()) {
                return Optional.ofNullable(response.source());
            }
            return Optional.empty();
        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            // 인덱스가 없는 경우 빈 결과 반환
            if (e.getMessage() != null && e.getMessage().contains("index_not_found_exception")) {
                return Optional.empty();
            }
            throw new SearchOperationException("Failed to find comment by id: " + id, e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to find comment by id: " + id, e);
        }
    }

    /**
     * 게시글별 댓글 조회
     *
     * @param postId 게시글 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 댓글 리스트
     */
    public List<CommentDocument> findByPostId(UUID postId, int page, int size) {
        Query termQuery = Query.of(q -> q
            .term(t -> t
                .field("postId")
                .value(postId.toString())
            )
        );

        return executeSearch(termQuery, page, size, "createdAt", SortOrder.Asc);
    }

    /**
     * 게시글의 최상위 댓글만 조회 (대댓글 제외)
     *
     * @param postId 게시글 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 최상위 댓글 리스트
     */
    public List<CommentDocument> findTopLevelCommentsByPostId(UUID postId, int page, int size) {
        BoolQuery boolQuery = BoolQuery.of(b -> b
            .must(Query.of(q -> q.term(t -> t.field("postId").value(postId.toString()))))
            .must(Query.of(q -> q.term(t -> t.field("isReply").value(false))))
        );

        Query query = Query.of(q -> q.bool(boolQuery));
        return executeSearch(query, page, size, "createdAt", SortOrder.Asc);
    }

    /**
     * 특정 댓글의 대댓글 조회
     *
     * @param parentCommentId 부모 댓글 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 대댓글 리스트
     */
    public List<CommentDocument> findRepliesByParentId(UUID parentCommentId, int page, int size) {
        Query termQuery = Query.of(q -> q
            .term(t -> t
                .field("parentCommentId")
                .value(parentCommentId.toString())
            )
        );

        return executeSearch(termQuery, page, size, "createdAt", SortOrder.Asc);
    }

    /**
     * 작성자별 댓글 조회
     *
     * @param authorId 작성자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 댓글 리스트
     */
    public List<CommentDocument> findByAuthorId(UUID authorId, int page, int size) {
        Query termQuery = Query.of(q -> q
            .term(t -> t
                .field("authorId")
                .value(authorId.toString())
            )
        );

        return executeSearch(termQuery, page, size, "createdAt", SortOrder.Desc);
    }

    /**
     * 댓글 내용으로 검색
     *
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과 리스트
     */
    public List<CommentDocument> searchByContent(String keyword, int page, int size) {
        Query matchQuery = Query.of(q -> q
            .match(m -> m
                .field("content")
                .query(keyword)
            )
        );

        return executeSearch(matchQuery, page, size, "createdAt", SortOrder.Desc);
    }

    /**
     * 복합 검색 (키워드 + 필터)
     *
     * @param keyword 검색 키워드 (null 가능)
     * @param postId 게시글 ID (null 가능)
     * @param authorId 작성자 ID (null 가능)
     * @param isReply 대댓글 여부 (null 가능)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과 리스트
     */
    public List<CommentDocument> searchWithFilters(
            String keyword,
            UUID postId,
            UUID authorId,
            Boolean isReply,
            int page,
            int size
    ) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 키워드 검색
        if (keyword != null && !keyword.isBlank()) {
            boolQueryBuilder.must(Query.of(q -> q
                .match(m -> m
                    .field("content")
                    .query(keyword)
                )
            ));
        }

        // 필터 조건들
        if (postId != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("postId").value(postId.toString()))
            ));
        }

        if (authorId != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("authorId").value(authorId.toString()))
            ));
        }

        if (isReply != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("isReply").value(isReply))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

        return executeSearch(boolQuery, page, size, "createdAt", SortOrder.Desc);
    }

    /**
     * 공통 검색 실행 메서드
     */
    private List<CommentDocument> executeSearch(
            Query query,
            int page,
            int size,
            String sortField,
            SortOrder sortOrder
    ) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .query(query)
                .from(page * size)
                .size(size)
                .sort(sort -> sort
                    .field(f -> f
                        .field(sortField)
                        .order(sortOrder)
                    )
                )
            );

            SearchResponse<CommentDocument> response = elasticsearchClient.search(
                searchRequest,
                CommentDocument.class
            );

            return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());

        } catch (IOException e) {
            throw new SearchOperationException("Failed to execute search", e);
        }
    }

    /**
     * 전체 개수 조회
     */
    public long count(Query query) {
        try {
            var response = elasticsearchClient.count(c -> c
                .index(INDEX_NAME)
                .query(query)
            );
            return response.count();
        } catch (IOException e) {
            throw new SearchOperationException("Failed to count documents", e);
        }
    }

    /**
     * 게시글의 댓글 수 조회
     */
    public long countByPostId(UUID postId) {
        Query termQuery = Query.of(q -> q
            .term(t -> t.field("postId").value(postId.toString()))
        );
        return count(termQuery);
    }
}
