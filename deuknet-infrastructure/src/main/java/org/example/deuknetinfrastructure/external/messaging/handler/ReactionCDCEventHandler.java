package org.example.deuknetinfrastructure.external.messaging.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.out.external.search.PostProjectionCommandPort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Reaction 이벤트 전용 핸들러
 *
 * 책임:
 * - REACTION_ADDED, REACTION_REMOVED 이벤트 처리
 * - PostDetailProjection 업데이트 (좋아요, 싫어요, 조회수 포함)
 */
@Slf4j
@Component
public class ReactionCDCEventHandler implements CDCEventHandler {

    private final PostProjectionCommandPort postProjectionCommandPort;

    public ReactionCDCEventHandler(PostProjectionCommandPort postProjectionCommandPort) {
        this.postProjectionCommandPort = postProjectionCommandPort;
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return eventType == EventType.REACTION_ADDED
                || eventType == EventType.REACTION_REMOVED;
    }

    @Override
    @Retryable(
            retryFor = {IOException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void handle(CDCEventMessage message) throws Exception {
        String payloadJson = message.payloadJson();
        EventType eventType = message.eventType();

        // Reaction 변경 시 전체 PostDetailProjection 업데이트
        postProjectionCommandPort.indexPostDetail(payloadJson);
        log.info("{} - PostDetailProjection updated with reaction counts", eventType);
    }
}
