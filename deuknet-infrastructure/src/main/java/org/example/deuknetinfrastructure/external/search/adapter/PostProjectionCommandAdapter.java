package org.example.deuknetinfrastructure.external.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.port.out.external.search.PostProjectionCommandPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetinfrastructure.external.search.document.PostDetailDocument;
import org.example.deuknetinfrastructure.external.search.exception.SearchOperationException;
import org.example.deuknetinfrastructure.external.search.mapper.PostDetailDocumentMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class PostProjectionCommandAdapter implements PostProjectionCommandPort {

    private static final String INDEX_NAME = "posts-detail";
    private final ElasticsearchClient elasticsearchClient;
    private final PostDetailDocumentMapper mapper;

    /**
     * PostDetailProjection을 Elasticsearch에 인덱싱
     * Debezium에서 호출됩니다.
     */
    public void indexPostDetail(String payloadJson) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.findAndRegisterModules(); // LocalDateTime 등을 위해 필요

            PostDetailDocument document = objectMapper.readValue(payloadJson, PostDetailDocument.class);
            PostDetailProjection projection = mapper.toProjection(document);

            save(projection);
        } catch (Exception e) {
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
     * Document 저장 (테스트용)
     * Elasticsearch Client를 사용하여 Document를 저장합니다.
     * 테스트 환경에서 인덱스가 없으면 자동으로 생성합니다.
     *
     * Connection is closed 에러를 처리하기 위해 재시도 로직 포함
     */
    @Override
    public void save(PostDetailProjection projection) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;

        PostDetailDocument document = mapper.toDocument(projection);

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
}
