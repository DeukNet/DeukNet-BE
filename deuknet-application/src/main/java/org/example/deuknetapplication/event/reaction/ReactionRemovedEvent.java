package org.example.deuknetapplication.event.reaction;

import lombok.Builder;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Projection;
import org.example.deuknetdomain.domain.reaction.ReactionType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reaction 삭제 이벤트 (Event Sourcing)
 *
 * Reaction이 삭제되었다는 사실(fact)을 기록합니다.
 * 이 이벤트를 replay하여 ReactionCountProjection을 재구성합니다.
 *
 * Event Sourcing 원칙:
 * - 과거에 발생한 불변의 사실
 * - 이벤트만으로 현재 상태를 재구성 가능
 * - 삭제 불가, 보정은 새로운 이벤트로
 */
@Getter
public class ReactionRemovedEvent extends Projection {

    /**
     * 삭제된 Reaction의 ID
     */
    private final UUID reactionId;

    /**
     * Reaction 대상 ID (Post ID 또는 Comment ID)
     */
    private final UUID targetId;

    /**
     * Reaction 타입 (LIKE, DISLIKE)
     */
    private final ReactionType reactionType;

    /**
     * 반응을 삭제한 사용자 ID
     */
    private final UUID userId;

    /**
     * 이벤트 발생 시각
     */
    private final LocalDateTime occurredAt;

    @Builder
    public ReactionRemovedEvent(UUID reactionId, UUID targetId, ReactionType reactionType,
                               UUID userId, LocalDateTime occurredAt) {
        super(reactionId);
        this.reactionId = reactionId;
        this.targetId = targetId;
        this.reactionType = reactionType;
        this.userId = userId;
        this.occurredAt = occurredAt != null ? occurredAt : LocalDateTime.now();
    }
}
