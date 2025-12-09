package org.example.deuknetinfrastructure.external.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.port.out.external.search.PostProjectionCommandPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetinfrastructure.external.search.document.PostDetailDocument;
import org.example.deuknetinfrastructure.external.search.exception.SearchOperationException;
import org.example.deuknetinfrastructure.external.search.mapper.PostDetailDocumentMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component // todo 개 병신 코드 리펙토링 꼭 할 것, 직렬화 문제... 이건 진짜 왜 일어나냐?
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

            // JSON을 PostDetailProjection으로 직접 역직렬화 (올바른 타입으로)
            PostDetailProjection projection = objectMapper.readValue(payloadJson, PostDetailProjection.class);

            // Projection을 Document로 변환하여 Elasticsearch에 저장
            // (User 정보는 제외하고 authorId, authorType만 저장됨)
            save(projection);
        } catch (Exception e) {
            throw new SearchOperationException("Failed to index PostDetail", e);
        }
    }

    /**
     * PostCountProjection으로 Elasticsearch 업데이트
     * Debezium에서 호출됩니다.
     */
    public void updatePostCounts(String payloadJson) {
        try {
            updatePostCountsInternal(payloadJson);
        } catch (Exception e) {
            throw new SearchOperationException("Failed to update post counts", e);
        }
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

            try {
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
            } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
                // 문서가 존재하지 않는 경우 (Post가 아직 인덱싱되지 않음)
                // Eventual consistency를 위해 경고 로그만 남기고 스킵
                if (e.getMessage() != null && e.getMessage().contains("document_missing_exception")) {
                    log.warn("Post document not yet indexed in Elasticsearch, skipping count update for postId: {}", postId);
                    return;
                }
                throw e;
            }
        }
    }

    /**
     * Document 저장 (테스트용)
     * Elasticsearch Client를 사용하여 Document를 저장합니다.
     * 테스트 환경에서 인덱스가 없으면 자동으로 생성합니다.
     */
    @Override
    public void save(PostDetailProjection projection) {
        PostDetailDocument document = mapper.toDocument(projection);

        try {
            saveInternal(document);
        } catch (Exception e) {
            throw new SearchOperationException("Failed to save document: " + document.getIdAsString(), e);
        }
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
}
