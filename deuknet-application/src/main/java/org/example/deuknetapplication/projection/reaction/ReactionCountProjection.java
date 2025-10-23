package org.example.deuknetapplication.projection.reaction;

import lombok.Builder;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.EventSourcingProjection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reaction 집계 정보 Projection (Event Sourcing)
 *
 * 특정 대상(Post, Comment)에 대한 리액션 타입별 카운트를 저장합니다.
 * Event Sourcing 방식으로 ReactionAdded/ReactionRemoved 이벤트를 소싱하여
 * 현재 상태를 재구성합니다.
 *
 * 사용 목적:
 * - 게시글/댓글의 좋아요/싫어요 수 빠른 조회
 * - 읽기 모델 최적화 (역정규화)
 * - 이벤트 기반 집계 관리
 *
 * Event Sourcing:
 * - version: 이벤트 적용 횟수 (Optimistic Locking)
 * - lastEventId: 마지막 적용 이벤트 ID
 * - eventCount: 총 적용된 이벤트 수
 */
@Getter
public class ReactionCountProjection extends EventSourcingProjection {

    /**
     * LIKE 리액션 수
     */
    private final Long likeCount;

    /**
     * DISLIKE 리액션 수
     */
    private final Long dislikeCount;

    /**
     * 전체 리액션 수 (LIKE + DISLIKE)
     */
    private final Long totalCount;

    @Builder
    public ReactionCountProjection(UUID id, Long version, UUID lastEventId,
                                   LocalDateTime lastEventTimestamp, Long eventCount,
                                   Long likeCount, Long dislikeCount) {
        super(id, version, lastEventId, lastEventTimestamp, eventCount);
        this.likeCount = likeCount != null ? likeCount : 0L;
        this.dislikeCount = dislikeCount != null ? dislikeCount : 0L;
        this.totalCount = this.likeCount + this.dislikeCount;
    }

    /**
     * 빈 ReactionCount를 생성합니다 (모든 카운트 0, 초기 버전)
     */
    public static ReactionCountProjection empty(UUID targetId) {
        return ReactionCountProjection.builder()
                .id(targetId)
                .version(0L)
                .lastEventId(null)
                .lastEventTimestamp(null)
                .eventCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .build();
    }
}
