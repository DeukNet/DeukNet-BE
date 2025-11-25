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
public class ReactionEventHandler implements EventHandler {

    private final PostProjectionCommandPort postProjectionCommandPort;

    public ReactionEventHandler(PostProjectionCommandPort postProjectionCommandPort) {
        this.postProjectionCommandPort = postProjectionCommandPort;
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return eventType == EventType.REACTION_ADDED
                || eventType == EventType.REACTION_REMOVED;
    }

    @Override
    public void handle(EventType eventType, String aggregateId, String payloadJson) throws Exception {
        postProjectionCommandPort.updatePostCounts(payloadJson);
        log.info("{} - Reaction count updated", eventType);
    }
}
