package org.example.deuknetinfrastructure.external.messaging.debezium;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetinfrastructure.external.search.adapter.PostSearchAdapter;
import org.springframework.stereotype.Component;

/**
 * Debezium 이벤트 핸들러
 *
 * Outbox 테이블의 변경 사항을 감지하여 Elasticsearch에 동기화합니다.
 */
@Slf4j
@Component
public class DebeziumEventHandler {

    private final PostSearchAdapter postSearchAdapter;
    private final ObjectMapper objectMapper;

    public DebeziumEventHandler(
            PostSearchAdapter postSearchAdapter,
            ObjectMapper objectMapper
    ) {
        this.postSearchAdapter = postSearchAdapter;
        this.objectMapper = objectMapper;
    }

    /**
     * Debezium 이벤트 처리
     *
     * @param key CDC 키 (보통 Primary Key)
     * @param value CDC 값 (변경된 데이터)
     */
    public void handleEvent(String key, String value) {
        try {
            JsonNode payload = objectMapper.readTree(value);
            JsonNode after = payload.get("after");

            if (after == null) {
                log.debug("DELETE event detected, skipping. key={}", key);
                return;
            }

            // Outbox 필드 추출
            String eventType = after.get("type").asText();
            String aggregateId = after.get("aggregateid").asText();
            String payloadJson = after.get("payload").asText();

            log.info("Processing CDC event: type={}, aggregateId={}", eventType, aggregateId);

            // 이벤트 타입별 처리
            switch (eventType) {
                case "PostCreated":
                case "PostUpdated":
                case "PostPublished":
                    handlePostEvent(payloadJson, eventType);
                    break;

                case "PostDeleted":
                    postSearchAdapter.deletePost(aggregateId);
                    log.info("Post deleted from Elasticsearch: id={}", aggregateId);
                    break;

                case "ReactionAdded":
                case "ReactionRemoved":
                    handleReactionEvent(payloadJson, eventType);
                    break;

                default:
                    log.warn("Unknown event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Failed to process Debezium event. key={}, value={}", key, value, e);
            // TODO: DLQ(Dead Letter Queue) 또는 재시도 로직 구현
        }
    }

    /**
     * Post 이벤트 처리
     */
    private void handlePostEvent(String payloadJson, String eventType) throws Exception {
        JsonNode payload = objectMapper.readTree(payloadJson);

        // PostDetailProjection인지 PostCountProjection인지 구분
        if (payload.has("title")) {
            // PostDetailProjection
            postSearchAdapter.indexPostDetail(payloadJson);
            log.info("{} - PostDetailProjection indexed", eventType);
        } else if (payload.has("viewCount")) {
            // PostCountProjection
            postSearchAdapter.updatePostCounts(payloadJson);
            log.info("{} - PostCountProjection updated", eventType);
        }
    }

    /**
     * Reaction 이벤트 처리
     */
    private void handleReactionEvent(String payloadJson, String eventType) throws Exception {
        postSearchAdapter.updatePostCounts(payloadJson);
        log.info("{} - Reaction count updated", eventType);
    }
}
