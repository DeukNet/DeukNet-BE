package org.example.deuknetinfrastructure.external.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.example.deuknetinfrastructure.external.search.document.PostDetailDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Post 상세 검색 Adapter
 *
 * PostDetailDocument를 활용한 게시글 검색 기능을 제공합니다.
 * - 제목/내용 전문 검색 (Full-text search)
 * - 작성자 필터링
 * - 카테고리 필터링
 * - 상태 필터링
 * - 다양한 정렬 옵션 (최신순, 인기순, 조회수순 등)
 */
@Component
@RequiredArgsConstructor
public class PostDetailSearchAdapter {

    private static final String INDEX_NAME = "posts-detail";
    private final ElasticsearchClient elasticsearchClient;

    /**
     * Document 저장 (테스트용)
     */
    public void save(PostDetailDocument document) {
        try {
            // 인덱스가 없으면 생성
            ensureIndexExists();

            elasticsearchClient.index(i -> i
                .index(INDEX_NAME)
                .id(document.getIdAsString())
                .document(document)
            );

            // Refresh to make document immediately searchable
            elasticsearchClient.indices().refresh(r -> r.index(INDEX_NAME));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save document: " + document.getIdAsString(), e);
        }
    }

    /**
     * 인덱스 존재 여부 확인 및 생성
     */
    private void ensureIndexExists() throws IOException {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index(INDEX_NAME)).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c.index(INDEX_NAME));
            }
        } catch (Exception e) {
            // 인덱스 생성 중 에러는 무시 (동시성 이슈로 이미 생성되었을 수 있음)
        }
    }

    /**
     * ID로 게시글 조회
     */
    public Optional<PostDetailDocument> findById(UUID id) {
        try {
            var response = elasticsearchClient.get(g -> g
                    .index(INDEX_NAME)
                    .id(id.toString()),
                PostDetailDocument.class
            );

            if (response.found()) {
                return Optional.ofNullable(response.source());
            }
            return Optional.empty();
        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            // 인덱스가 없는 경우 빈 결과 반환 (CDC 파이프라인이 아직 인덱스를 생성하지 않았을 수 있음)
            if (e.getMessage() != null && e.getMessage().contains("index_not_found_exception")) {
                return Optional.empty();
            }
            throw new RuntimeException("Failed to find post by id: " + id, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to find post by id: " + id, e);
        }
    }

    /**
     * 제목과 내용으로 전문 검색
     *
     * @param keyword 검색 키워드
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 검색 결과 리스트
     */
    public List<PostDetailDocument> searchByTitleAndContent(String keyword, int page, int size) {
        Query multiMatchQuery = Query.of(q -> q
            .multiMatch(m -> m
                .query(keyword)
                .fields("title^2", "content")  // title에 2배 가중치
            )
        );

        return executeSearch(multiMatchQuery, page, size, "createdAt", SortOrder.Desc);
    }

    /**
     * 작성자로 게시글 검색
     *
     * @param authorId 작성자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과 리스트
     */
    public List<PostDetailDocument> searchByAuthor(UUID authorId, int page, int size) {
        Query termQuery = Query.of(q -> q
            .term(t -> t
                .field("authorId")
                .value(authorId.toString())
            )
        );

        return executeSearch(termQuery, page, size, "createdAt", SortOrder.Desc);
    }

    /**
     * 카테고리로 게시글 검색
     *
     * @param categoryId 카테고리 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과 리스트
     */
    public List<PostDetailDocument> searchByCategory(UUID categoryId, int page, int size) {
        Query termQuery = Query.of(q -> q
            .term(t -> t
                .field("categoryIds")
                .value(categoryId.toString())
            )
        );

        return executeSearch(termQuery, page, size, "createdAt", SortOrder.Desc);
    }

    /**
     * 상태로 게시글 검색
     *
     * @param status 게시글 상태 (PUBLISHED, DRAFT 등)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과 리스트
     */
    public List<PostDetailDocument> searchByStatus(String status, int page, int size) {
        Query termQuery = Query.of(q -> q
            .term(t -> t
                .field("status")
                .value(status)
            )
        );

        return executeSearch(termQuery, page, size, "createdAt", SortOrder.Desc);
    }

    /**
     * 복합 검색 (키워드 + 필터)
     *
     * @param keyword 검색 키워드 (null 가능)
     * @param authorId 작성자 ID (null 가능)
     * @param categoryId 카테고리 ID (null 가능)
     * @param status 상태 (null 가능)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortField 정렬 필드 (createdAt, viewCount, likeCount 등)
     * @param sortOrder 정렬 순서
     * @return 검색 결과 리스트
     */
    public List<PostDetailDocument> searchWithFilters(
            String keyword,
            UUID authorId,
            UUID categoryId,
            String status,
            int page,
            int size,
            String sortField,
            SortOrder sortOrder
    ) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 키워드 검색 (must)
        if (keyword != null && !keyword.isBlank()) {
            boolQueryBuilder.must(Query.of(q -> q
                .multiMatch(m -> m
                    .query(keyword)
                    .fields("title^2", "content")
                )
            ));
        }

        // 필터 조건들 (filter)
        if (authorId != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("authorId").value(authorId.toString()))
            ));
        }

        if (categoryId != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("categoryIds").value(categoryId.toString()))
            ));
        }

        if (status != null && !status.isBlank()) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("status").value(status))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

        return executeSearch(boolQuery, page, size, sortField, sortOrder);
    }

    /**
     * 인기 게시글 조회 (좋아요 + 댓글 수 기준)
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과 리스트
     */
    public List<PostDetailDocument> findPopularPosts(int page, int size) {
        Query matchAllQuery = Query.of(q -> q.matchAll(m -> m));

        // 좋아요 수로 정렬 (댓글 수도 고려 가능)
        return executeSearch(matchAllQuery, page, size, "likeCount", SortOrder.Desc);
    }

    /**
     * 최신 게시글 조회
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과 리스트
     */
    public List<PostDetailDocument> findRecentPosts(int page, int size) {
        Query statusQuery = Query.of(q -> q
            .term(t -> t.field("status").value("PUBLISHED"))
        );

        return executeSearch(statusQuery, page, size, "createdAt", SortOrder.Desc);
    }

    /**
     * 공통 검색 실행 메서드
     */
    private List<PostDetailDocument> executeSearch(
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

            SearchResponse<PostDetailDocument> response = elasticsearchClient.search(
                searchRequest,
                PostDetailDocument.class
            );

            return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());

        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            // 인덱스가 없는 경우 빈 리스트 반환
            if (e.getMessage() != null && (e.getMessage().contains("index_not_found_exception")
                    || e.getMessage().contains("all shards failed"))) {
                return List.of();
            }
            throw new RuntimeException("Failed to execute search", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute search", e);
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
            throw new RuntimeException("Failed to count documents", e);
        }
    }
}
