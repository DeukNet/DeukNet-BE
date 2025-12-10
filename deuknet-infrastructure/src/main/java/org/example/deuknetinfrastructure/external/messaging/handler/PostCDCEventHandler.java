package org.example.deuknetinfrastructure.external.messaging.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.out.external.search.PostProjectionCommandPort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Post 이벤트 전용 핸들러
 *
 * 책임:
 * - POST_CREATED, POST_UPDATED, POST_PUBLISHED, POST_DELETED 이벤트 처리
 * - PostDetailProjection Elasticsearch 동기화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostCDCEventHandler implements CDCEventHandler {

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
    @Retryable(
            retryFor = {IOException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void handle(CDCEventMessage message) throws Exception {

        EventType eventType = message.eventType();
        String aggregateId = message.aggregateId();
        String payloadJson = message.payloadJson();

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
     * PostDetailProjection을 Elasticsearch에 인덱싱
     */
    private void handlePostEvent(String payloadJson, EventType eventType) throws Exception {
        // 모든 Post 이벤트는 PostDetailProjection을 포함
        postProjectionCommandPort.indexPostDetail(payloadJson);
        log.info("{} - PostDetailProjection indexed", eventType);
    }
}
