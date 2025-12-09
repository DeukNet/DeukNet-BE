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
            objectMapper.findAndRegisterModules();

            PostDetailProjection projection = objectMapper.readValue(payloadJson, PostDetailProjection.class);

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

        if (!scriptBuilder.isEmpty()) {
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

        elasticsearchClient.index(i -> i
                .index(INDEX_NAME)
                .id(document.getIdAsString())
                .document(document)
        );

        elasticsearchClient.indices().refresh(r -> r.index(INDEX_NAME));
    }
}
