package org.example.deuknetinfrastructure.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.event.reaction.ReactionAddedEvent;
import org.example.deuknetapplication.event.reaction.ReactionRemovedEvent;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetinfrastructure.data.document.ReactionCountDocument;
import org.example.deuknetinfrastructure.data.query.reaction.ReactionCountDocumentRepository;
import org.example.deuknetinfrastructure.outbox.OutboxEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Reaction Event Handler (Event Sourcing)
 *
 * OutboxEvent에서 Reaction 이벤트를 소비하여 ReactionCountProjection을 업데이트합니다.
 * Event Sourcing의 핵심: 이벤트를 replay하여 현재 상태를 재구성합니다.
 *
 * 처리 흐름:
 * 1. OutboxEventScheduler가 OutboxEvent 폴링
 * 2. ReactionEventHandler.handle() 호출
 * 3. 이벤트 타입 확인 (ReactionAdded or ReactionRemoved)
 * 4. ReactionCountDocument 조회 (없으면 생성)
 * 5. 이벤트에 따라 카운트 증감
 * 6. Elasticsearch에 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactionEventHandler {

    private final ReactionCountDocumentRepository reactionCountDocumentRepository;
    private final ObjectMapper objectMapper;

    /**
     * Reaction 이벤트를 처리합니다 (Event Sourcing)
     *
     * @param outboxEvent Outbox에 저장된 이벤트
     */
    public void handle(OutboxEvent outboxEvent) {
        String eventType = outboxEvent.getEventType();

        try {
            switch (eventType) {
                case "ReactionAdded":
                    handleReactionAdded(outboxEvent);
                    break;
                case "ReactionRemoved":
                    handleReactionRemoved(outboxEvent);
                    break;
                default:
                    // 다른 이벤트는 무시
                    log.debug("Skipping non-reaction event: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to handle reaction event: eventId={}, type={}",
                    outboxEvent.getId(), eventType, e);
            throw new RuntimeException("Failed to handle reaction event", e);
        }
    }

    /**
     * ReactionAdded 이벤트 처리: 카운트 증가 (Event Sourcing)
     */
    private void handleReactionAdded(OutboxEvent outboxEvent) throws Exception {
        ReactionAddedEvent event = objectMapper.readValue(
                outboxEvent.getPayload(), ReactionAddedEvent.class);

        UUID targetId = event.getTargetId();
        UUID eventId = event.getReactionId();
        ReactionType reactionType = event.getReactionType();

        // 1. 기존 Document 조회 또는 생성
        ReactionCountDocument document = reactionCountDocumentRepository
                .findByTargetId(targetId)
                .orElseGet(() -> ReactionCountDocument.create(targetId));

        // 2. 중복 이벤트 체크 (Idempotency)
        if (document.isDuplicateEvent(eventId)) {
            log.warn("Duplicate event ignored: eventId={}, targetId={}", eventId, targetId);
            return;
        }

        // 3. Event Sourcing: 이벤트에 따라 상태 변경 + 버전 업데이트
        if (reactionType == ReactionType.LIKE) {
            document.incrementLike(eventId, event.getOccurredAt());
        } else if (reactionType == ReactionType.DISLIKE) {
            document.incrementDislike(eventId, event.getOccurredAt());
        }

        // 4. Elasticsearch에 저장 (캡슐화)
        reactionCountDocumentRepository.saveReactionCount(document);

        log.info("ReactionAdded processed: targetId={}, type={}, likeCount={}, dislikeCount={}, version={}, eventCount={}",
                targetId, reactionType, document.getLikeCount(), document.getDislikeCount(),
                document.getVersion(), document.getEventCount());
    }

    /**
     * ReactionRemoved 이벤트 처리: 카운트 감소 (Event Sourcing)
     */
    private void handleReactionRemoved(OutboxEvent outboxEvent) throws Exception {
        ReactionRemovedEvent event = objectMapper.readValue(
                outboxEvent.getPayload(), ReactionRemovedEvent.class);

        UUID targetId = event.getTargetId();
        UUID eventId = event.getReactionId();
        ReactionType reactionType = event.getReactionType();

        // 1. 기존 Document 조회 (없으면 에러)
        ReactionCountDocument document = reactionCountDocumentRepository
                .findByTargetId(targetId)
                .orElseThrow(() -> new IllegalStateException(
                        "ReactionCountDocument not found for targetId: " + targetId));

        // 2. 중복 이벤트 체크 (Idempotency)
        if (document.isDuplicateEvent(eventId)) {
            log.warn("Duplicate event ignored: eventId={}, targetId={}", eventId, targetId);
            return;
        }

        // 3. Event Sourcing: 이벤트에 따라 상태 변경 + 버전 업데이트
        if (reactionType == ReactionType.LIKE) {
            document.decrementLike(eventId, event.getOccurredAt());
        } else if (reactionType == ReactionType.DISLIKE) {
            document.decrementDislike(eventId, event.getOccurredAt());
        }

        // 4. Elasticsearch에 저장 (캡슐화)
        reactionCountDocumentRepository.saveReactionCount(document);

        log.info("ReactionRemoved processed: targetId={}, type={}, likeCount={}, dislikeCount={}, version={}, eventCount={}",
                targetId, reactionType, document.getLikeCount(), document.getDislikeCount(),
                document.getVersion(), document.getEventCount());
    }
}
