package org.example.deuknetinfrastructure.external.messaging.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.out.external.search.PostProjectionCommandPort;
import org.springframework.stereotype.Component;

/**
 * Post 이벤트 전용 핸들러
 *
 * 책임:
 * - POST_CREATED, POST_UPDATED, POST_PUBLISHED, POST_DELETED 이벤트 처리
 * - PostDetailProjection, PostCountProjection 구분 및 Elasticsearch 동기화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventHandler implements EventHandler {

    private final PostProjectionCommandPort postProjectionCommandPort;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canHandle(EventType eventType) {
        return eventType == EventType.POST_CREATED
                || eventType == EventType.POST_UPDATED
                || eventType == EventType.POST_PUBLISHED
                || eventType == EventType.POST_DELETED;
    }

    @Override
    public void handle(EventType eventType, String aggregateId, String payloadJson) throws Exception {
        log.info("Handling event: type={}, aggregateId={}", eventType, aggregateId);
        log.debug("Payload JSON: {}", payloadJson);

        if (eventType == EventType.POST_DELETED) {
            postProjectionCommandPort.deletePost(aggregateId);
            log.info("Post deleted from Elasticsearch: id={}", aggregateId);
            return;
        }

        // POST_CREATED, POST_UPDATED, POST_PUBLISHED
        handlePostEvent(payloadJson, eventType);
    }

    /**
     * Post 이벤트 처리
     * PostDetailProjection과 PostCountProjection을 구분하여 처리
     */
    private void handlePostEvent(String payloadJson, EventType eventType) throws Exception {
        JsonNode payload = objectMapper.readTree(payloadJson);

        // PostDetailProjection인지 PostCountProjection인지 구분
        if (payload.has("title")) {
            // PostDetailProjection
            postProjectionCommandPort.indexPostDetail(payloadJson);
            log.info("{} - PostDetailProjection indexed", eventType);
        } else if (payload.has("viewCount") || payload.has("commentCount") ||
                   payload.has("likeCount") || payload.has("dislikeCount")) {
            // PostCountProjection (count 필드 중 하나라도 있으면 PostCountProjection)
            postProjectionCommandPort.updatePostCounts(payloadJson);
            log.info("{} - PostCountProjection updated", eventType);
        }
    }
}
