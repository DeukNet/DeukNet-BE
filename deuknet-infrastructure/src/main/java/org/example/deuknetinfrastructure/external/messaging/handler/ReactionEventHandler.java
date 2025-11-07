package org.example.deuknetinfrastructure.external.messaging.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetinfrastructure.external.search.adapter.PostSearchAdapter;
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

    private final PostSearchAdapter postSearchAdapter;

    public ReactionEventHandler(PostSearchAdapter postSearchAdapter) {
        this.postSearchAdapter = postSearchAdapter;
    }

    @Override
    public boolean canHandle(EventType eventType) {
        return eventType == EventType.REACTION_ADDED
                || eventType == EventType.REACTION_REMOVED;
    }

    @Override
    public void handle(EventType eventType, String aggregateId, String payloadJson) throws Exception {
        postSearchAdapter.updatePostCounts(payloadJson);
        log.info("{} - Reaction count updated", eventType);
    }
}
