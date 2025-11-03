package org.example.deuknetinfrastructure.external.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.post.PostSearchPort;
import org.example.deuknetinfrastructure.external.search.document.PostDetailDocument;
import org.example.deuknetinfrastructure.external.search.exception.SearchOperationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 게시글 검색 Adapter (Elasticsearch)
 *
 * 단일 search 메서드로 모든 검색 조건을 AND로 처리합니다.
 */
@Component
@RequiredArgsConstructor
public class PostSearchAdapter implements PostSearchPort {

    private static final String INDEX_NAME = "posts-detail";
    private final ElasticsearchClient elasticsearchClient;

    @Override
    public Optional<PostSearchResponse> findById(UUID id) {
        try {
            var response = elasticsearchClient.get(g -> g
                    .index(INDEX_NAME)
                    .id(id.toString()),
                PostDetailDocument.class
            );

            if (response.found()) {
                return Optional.ofNullable(response.source()).map(this::toResponse);
            }
            return Optional.empty();
        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            if (e.getMessage() != null && e.getMessage().contains("index_not_found_exception")) {
                return Optional.empty();
            }
            throw new SearchOperationException("Failed to find post by id: " + id, e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to find post by id: " + id, e);
        }
    }

    @Override
    public List<PostSearchResponse> search(PostSearchRequest request) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 키워드 검색 (must) - 제목/내용 전문 검색
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            boolQueryBuilder.must(Query.of(q -> q
                .multiMatch(m -> m
                    .query(request.getKeyword())
                    .fields("title^2", "content")  // title에 2배 가중치
                )
            ));
        }

        // 작성자 필터 (filter)
        if (request.getAuthorId() != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("authorId").value(request.getAuthorId().toString()))
            ));
        }

        // 카테고리 필터 (filter)
        if (request.getCategoryId() != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("categoryIds").value(request.getCategoryId().toString()))
            ));
        }

        // 상태 필터 (filter)
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("status").value(request.getStatus()))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

        // 정렬 순서 변환
        SortOrder sortOrder = "asc".equalsIgnoreCase(request.getSortOrder())
                ? SortOrder.Asc
                : SortOrder.Desc;

        return executeSearch(boolQuery, request.getPage(), request.getSize(), request.getSortBy(), sortOrder);
    }

    @Override
    public List<PostSearchResponse> findPopularPosts(int page, int size) {
        Query matchAllQuery = Query.of(q -> q.matchAll(m -> m));
        return executeSearch(matchAllQuery, page, size, "likeCount", SortOrder.Desc);
    }

    /**
     * 공통 검색 실행 메서드
     */
    private List<PostSearchResponse> executeSearch(
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
                .map(this::toResponse)
                .collect(Collectors.toList());

        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            // 인덱스가 없는 경우 빈 리스트 반환
            if (e.getMessage() != null && (e.getMessage().contains("index_not_found_exception")
                    || e.getMessage().contains("all shards failed"))) {
                return List.of();
            }
            throw new SearchOperationException("Failed to execute search", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to execute search", e);
        }
    }

    /**
     * Document를 Response로 변환
     */
    private PostSearchResponse toResponse(PostDetailDocument doc) {
        return new PostSearchResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getContent(),
                UUID.fromString(doc.getAuthorId()),
                doc.getAuthorUsername(),
                doc.getAuthorDisplayName(),
                doc.getStatus(),
                doc.getCategoryIds().stream().map(UUID::fromString).toList(),
                doc.getCategoryNames(),
                doc.getViewCount(),
                doc.getCommentCount(),
                doc.getLikeCount(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    /**
     * Document 저장 (테스트용)
     */
    public void save(PostDetailDocument document) {
        try {
            ensureIndexExists();

            elasticsearchClient.index(i -> i
                .index(INDEX_NAME)
                .id(document.getIdAsString())
                .document(document)
            );

            elasticsearchClient.indices().refresh(r -> r.index(INDEX_NAME));
        } catch (IOException e) {
            throw new SearchOperationException("Failed to save document: " + document.getIdAsString(), e);
        }
    }

    private void ensureIndexExists() throws IOException {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index(INDEX_NAME)).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                    .index(INDEX_NAME)
                    .mappings(m -> m
                        .properties("id", p -> p.keyword(k -> k))
                        .properties("title", p -> p.text(t -> t))
                        .properties("content", p -> p.text(t -> t))
                        .properties("authorId", p -> p.keyword(k -> k))
                        .properties("authorUsername", p -> p.keyword(k -> k))
                        .properties("authorDisplayName", p -> p.text(t -> t))
                        .properties("status", p -> p.keyword(k -> k))
                        .properties("categoryIds", p -> p.keyword(k -> k))
                        .properties("categoryNames", p -> p.text(t -> t))
                        .properties("viewCount", p -> p.long_(l -> l))
                        .properties("commentCount", p -> p.long_(l -> l))
                        .properties("likeCount", p -> p.long_(l -> l))
                        .properties("createdAt", p -> p.date(d -> d))
                        .properties("updatedAt", p -> p.date(d -> d))
                    )
                );
            }
        } catch (Exception e) {
            // 인덱스 생성 중 에러는 무시 (동시성 이슈로 이미 생성되었을 수 있음)
        }
    }
}
