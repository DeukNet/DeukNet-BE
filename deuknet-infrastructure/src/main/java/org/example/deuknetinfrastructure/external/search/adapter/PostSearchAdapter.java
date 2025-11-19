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
import org.example.deuknetapplication.port.in.post.SearchPostUseCase;
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
 * SearchPostUseCase와 PostSearchPort를 동시에 구현합니다.
 * - SearchPostUseCase: Controller가 직접 사용 (in port)
 * - PostSearchPort: GetPostByIdService가 사용 (out port)
 */
@Component
@RequiredArgsConstructor
public class PostSearchAdapter implements SearchPostUseCase, PostSearchPort {

    private static final String INDEX_NAME = "posts-detail";
    private final ElasticsearchClient elasticsearchClient;

    @Override
    public Optional<PostSearchResponse> findById(UUID id) {
        try {
            var document = elasticsearchClient.get(g -> g
                    .index(INDEX_NAME)
                    .id(id.toString()),
                PostDetailDocument.class
            );

            if (document.found()) {
                return Optional.ofNullable(document.source()).map(this::toResponse);
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
                doc.getCategoryIds() != null ? doc.getCategoryIds().stream().map(UUID::fromString).toList() : List.of(),
                doc.getCategoryNames() != null ? doc.getCategoryNames() : List.of(),
                doc.getViewCount(),
                doc.getCommentCount(),
                doc.getLikeCount(),
                doc.getDislikeCount() != null ? doc.getDislikeCount() : 0L,
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    /**
     * Document 저장 (테스트용)
     * Elasticsearch Client를 사용하여 Document를 저장합니다.
     * 테스트 환경에서 인덱스가 없으면 자동으로 생성합니다.
     *
     * Connection is closed 에러를 처리하기 위해 재시도 로직 포함
     */
    public void save(PostDetailDocument document) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                saveInternal(document);
                return;  // 성공 시 즉시 반환
            } catch (Exception e) {
                lastException = e;
                retryCount++;

                if (isRetriableException(e) && retryCount < maxRetries) {
                    System.out.println("Elasticsearch 연결 오류 발생 (save), 재시도 " + retryCount + "/" + maxRetries);
                    try {
                        Thread.sleep(100 * retryCount);  // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        throw new SearchOperationException("Failed to save document: " + document.getIdAsString() + " after " + maxRetries + " retries", lastException);
    }

    private void saveInternal(PostDetailDocument document) throws IOException {
        ensureIndexExists();

        elasticsearchClient.index(i -> i
                .index(INDEX_NAME)
                .id(document.getIdAsString())
                .document(document)
        );

        elasticsearchClient.indices().refresh(r -> r.index(INDEX_NAME));
    }

    /**
     * 테스트 환경에서 인덱스가 없으면 생성합니다.
     * Spring Data Elasticsearch가 PostDetailDocumentRepository를 통해
     * 자동으로 인덱스를 생성하므로, 여기서는 인덱스 존재 여부만 확인합니다.
     */
    private void ensureIndexExists() throws IOException {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index(INDEX_NAME)).value();
            if (!exists) {
                // 인덱스가 없으면 동적으로 생성됩니다.
                // Spring이 @Document 어노테이션을 기반으로 매핑을 관리합니다.
            }
        } catch (Exception e) {
            // 인덱스 확인 중 에러는 무시
        }
    }

    /**
     * PostDetailProjection을 Elasticsearch에 인덱싱
     * Debezium에서 호출됩니다.
     */
    public void indexPostDetail(String payloadJson) {
        try {
            System.out.println("===== Indexing PostDetail =====");
            System.out.println("Payload JSON: " + payloadJson);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.findAndRegisterModules(); // LocalDateTime 등을 위해 필요

            PostDetailDocument document = mapper.readValue(payloadJson, PostDetailDocument.class);
            System.out.println("Document deserialized successfully: id=" + document.getIdAsString());

            save(document);
            System.out.println("Document saved successfully to Elasticsearch");
        } catch (Exception e) {
            System.err.println("Failed to index PostDetail: " + e.getMessage());
            e.printStackTrace();
            throw new SearchOperationException("Failed to index PostDetail", e);
        }
    }

    /**
     * PostCountProjection으로 Elasticsearch 업데이트
     * Debezium에서 호출됩니다.
     *
     * Connection is closed 에러를 처리하기 위해 재시도 로직 포함
     */
    public void updatePostCounts(String payloadJson) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                updatePostCountsInternal(payloadJson);
                return;  // 성공 시 즉시 반환
            } catch (Exception e) {
                lastException = e;
                retryCount++;

                if (isRetriableException(e) && retryCount < maxRetries) {
                    System.out.println("Elasticsearch 연결 오류 발생, 재시도 " + retryCount + "/" + maxRetries);
                    try {
                        Thread.sleep(100 * retryCount);  // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        throw new SearchOperationException("Failed to update post counts after " + maxRetries + " retries", lastException);
    }

    private void updatePostCountsInternal(String payloadJson) throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode payload = mapper.readTree(payloadJson);

        String postId = payload.get("id").asText();

        // Partial update를 위한 스크립트 구성
        StringBuilder scriptBuilder = new StringBuilder();
        com.fasterxml.jackson.databind.node.ObjectNode params = mapper.createObjectNode();

        if (payload.has("viewCount")) {
            scriptBuilder.append("ctx._source.viewCount = params.viewCount; ");
            params.put("viewCount", payload.get("viewCount").asLong());
        }
        if (payload.has("likeCount")) {
            scriptBuilder.append("ctx._source.likeCount = params.likeCount; ");
            params.put("likeCount", payload.get("likeCount").asLong());
        }
        if (payload.has("dislikeCount")) {
            scriptBuilder.append("ctx._source.dislikeCount = params.dislikeCount; ");
            params.put("dislikeCount", payload.get("dislikeCount").asLong());
        }
        if (payload.has("commentCount")) {
            scriptBuilder.append("ctx._source.commentCount = params.commentCount; ");
            params.put("commentCount", payload.get("commentCount").asLong());
        }

        if (scriptBuilder.length() > 0) {
            String script = scriptBuilder.toString();
            java.util.Map<String, co.elastic.clients.json.JsonData> paramsMap = new java.util.HashMap<>();

            if (payload.has("viewCount")) {
                paramsMap.put("viewCount", co.elastic.clients.json.JsonData.of(payload.get("viewCount").asLong()));
            }
            if (payload.has("likeCount")) {
                paramsMap.put("likeCount", co.elastic.clients.json.JsonData.of(payload.get("likeCount").asLong()));
            }
            if (payload.has("dislikeCount")) {
                paramsMap.put("dislikeCount", co.elastic.clients.json.JsonData.of(payload.get("dislikeCount").asLong()));
            }
            if (payload.has("commentCount")) {
                paramsMap.put("commentCount", co.elastic.clients.json.JsonData.of(payload.get("commentCount").asLong()));
            }

            elasticsearchClient.update(u -> u
                    .index(INDEX_NAME)
                    .id(postId)
                    .script(s -> s
                            .inline(i -> i
                                    .source(script)
                                    .params(paramsMap)
                            )
                    ),
                    PostDetailDocument.class
            );
        }
    }

    /**
     * 재시도 가능한 예외인지 확인
     */
    private boolean isRetriableException(Exception e) {
        String message = e.getMessage();
        return message != null && (
                message.contains("Connection is closed") ||
                message.contains("Connection reset") ||
                message.contains("Broken pipe") ||
                message.contains("Connection refused")
        );
    }

    /**
     * Post 삭제
     * Debezium에서 호출됩니다.
     */
    public void deletePost(String postId) {
        try {
            elasticsearchClient.delete(d -> d
                    .index(INDEX_NAME)
                    .id(postId)
            );
        } catch (Exception e) {
            throw new SearchOperationException("Failed to delete post: " + postId, e);
        }
    }
}
