package org.example.deuknetinfrastructure.external.messaging.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.out.external.search.PostProjectionCommandPort;
import org.springframework.stereotype.Component;

/**
 * Reaction 이벤트 전용 핸들러
 *
 * 책임:
 * - REACTION_ADDED, REACTION_REMOVED 이벤트 처리
 * - PostCountProjection 업데이트 (좋아요, 싫어요, 조회수)
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
    public void handle(CDCEventMessage message) throws Exception {
        String payloadJson = message.payloadJson();
        EventType eventType = message.eventType();

        postProjectionCommandPort.updatePostCounts(payloadJson);
        log.info("{} - Reaction count updated", eventType);
    }
}
