package org.example.deuknetapplication.projection.comment;

import lombok.Builder;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.EventSourcingProjection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 댓글 통계 정보 Projection (Event Sourcing)
 *
 * 댓글의 집계 데이터(대댓글 수, 리액션 수 등)를 저장합니다.
 * Event Sourcing 방식으로 CommentAdded/CommentRemoved, ReactionAdded/ReactionRemoved
 * 이벤트를 소싱하여 현재 상태를 재구성합니다.
 *
 * Event Sourcing:
 * - version: 이벤트 적용 횟수 (Optimistic Locking)
 * - lastEventId: 마지막 적용 이벤트 ID
 * - eventCount: 총 적용된 이벤트 수
 */
@Getter
public class CommentCountProjection extends EventSourcingProjection {

    /**
     * 대댓글 수
     */
    private final Long replyCount;

    /**
     * 리액션 수 (좋아요 등)
     */
    private final Long reactionCount;

    @Builder
    public CommentCountProjection(UUID id, Long version, UUID lastEventId,
                                  LocalDateTime lastEventTimestamp, Long eventCount,
                                  Long replyCount, Long reactionCount) {
        super(id, version, lastEventId, lastEventTimestamp, eventCount);
        this.replyCount = replyCount != null ? replyCount : 0L;
        this.reactionCount = reactionCount != null ? reactionCount : 0L;
    }

    /**
     * 빈 CommentCount를 생성합니다 (모든 카운트 0, 초기 버전)
     */
    public static CommentCountProjection empty(UUID commentId) {
        return CommentCountProjection.builder()
                .id(commentId)
                .version(0L)
                .lastEventId(null)
                .lastEventTimestamp(null)
                .eventCount(0L)
                .replyCount(0L)
                .reactionCount(0L)
                .build();
    }
}
