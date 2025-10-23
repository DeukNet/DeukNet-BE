package org.example.deuknetinfrastructure.data.document;

import lombok.Getter;
import lombok.Setter;
import org.example.deuknetinfrastructure.common.seedwork.BaseDocument;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reaction 집계 정보 Elasticsearch Document (Event Sourcing)
 *
 * Event Sourcing 방식으로 ReactionAdded/ReactionRemoved 이벤트를 처리하여
 * 대상(Post, Comment)별 리액션 타입별 카운트를 저장합니다.
 *
 * Event Sourcing 메타데이터:
 * - version: Optimistic Locking 및 이벤트 순서 보장
 * - lastEventId: 마지막 적용된 이벤트 ID (중복 방지)
 * - lastEventTimestamp: 마지막 이벤트 발생 시각
 * - eventCount: 적용된 총 이벤트 수
 *
 * 인덱스 구조:
 * - Document ID: targetId (Post ID 또는 Comment ID)
 * - 하나의 대상당 하나의 Document
 * - 리액션 타입별 카운트 필드
 */
@Getter
@Setter
@Document(indexName = "reaction_counts")
public class ReactionCountDocument extends BaseDocument {

    // ========== Event Sourcing 메타데이터 ==========

    /**
     * Projection 버전 (Event Sourcing)
     *
     * 이벤트가 적용될 때마다 증가합니다.
     * Optimistic Locking에 사용됩니다.
     */
    @Field(type = FieldType.Long)
    private Long version;

    /**
     * 마지막 적용된 이벤트 ID
     *
     * 중복 이벤트 적용 방지에 사용됩니다.
     */
    @Field(type = FieldType.Keyword)
    private String lastEventId;

    /**
     * 마지막 이벤트 발생 시각
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime lastEventTimestamp;

    /**
     * 적용된 총 이벤트 수
     */
    @Field(type = FieldType.Long)
    private Long eventCount;

    // ========== 집계 데이터 ==========

    /**
     * LIKE 리액션 수
     */
    @Field(type = FieldType.Long)
    private Long likeCount;

    /**
     * DISLIKE 리액션 수
     */
    @Field(type = FieldType.Long)
    private Long dislikeCount;

    /**
     * 전체 리액션 수 (캐시 필드)
     */
    @Field(type = FieldType.Long)
    private Long totalCount;

    protected ReactionCountDocument() {
        super();
    }

    public ReactionCountDocument(UUID targetId) {
        super(targetId);
        // Event Sourcing 메타데이터 초기화
        this.version = 0L;
        this.lastEventId = null;
        this.lastEventTimestamp = null;
        this.eventCount = 0L;
        // 집계 데이터 초기화
        this.likeCount = 0L;
        this.dislikeCount = 0L;
        this.totalCount = 0L;
    }

    /**
     * ReactionCount Document를 생성합니다 (초기 상태)
     *
     * @param targetId 대상 ID (Post ID 또는 Comment ID)
     * @return 생성된 Document
     */
    public static ReactionCountDocument create(UUID targetId) {
        return new ReactionCountDocument(targetId);
    }

    /**
     * 이벤트를 적용하고 버전을 업데이트합니다 (Event Sourcing)
     *
     * @param eventId 적용할 이벤트 ID
     * @param occurredAt 이벤트 발생 시각
     */
    public void applyEvent(UUID eventId, LocalDateTime occurredAt) {
        this.version++;
        this.eventCount++;
        this.lastEventId = eventId.toString();
        this.lastEventTimestamp = occurredAt;
    }

    /**
     * LIKE 리액션을 추가합니다 (Event: ReactionAdded with LIKE)
     *
     * @param eventId 이벤트 ID
     * @param occurredAt 이벤트 발생 시각
     */
    public void incrementLike(UUID eventId, LocalDateTime occurredAt) {
        this.likeCount++;
        this.totalCount++;
        applyEvent(eventId, occurredAt);
    }

    /**
     * LIKE 리액션을 제거합니다 (Event: ReactionRemoved with LIKE)
     *
     * @param eventId 이벤트 ID
     * @param occurredAt 이벤트 발생 시각
     */
    public void decrementLike(UUID eventId, LocalDateTime occurredAt) {
        if (this.likeCount > 0) {
            this.likeCount--;
            this.totalCount--;
        }
        applyEvent(eventId, occurredAt);
    }

    /**
     * DISLIKE 리액션을 추가합니다 (Event: ReactionAdded with DISLIKE)
     *
     * @param eventId 이벤트 ID
     * @param occurredAt 이벤트 발생 시각
     */
    public void incrementDislike(UUID eventId, LocalDateTime occurredAt) {
        this.dislikeCount++;
        this.totalCount++;
        applyEvent(eventId, occurredAt);
    }

    /**
     * DISLIKE 리액션을 제거합니다 (Event: ReactionRemoved with DISLIKE)
     *
     * @param eventId 이벤트 ID
     * @param occurredAt 이벤트 발생 시각
     */
    public void decrementDislike(UUID eventId, LocalDateTime occurredAt) {
        if (this.dislikeCount > 0) {
            this.dislikeCount--;
            this.totalCount--;
        }
        applyEvent(eventId, occurredAt);
    }

    /**
     * 중복 이벤트인지 확인합니다 (Idempotency)
     *
     * @param eventId 확인할 이벤트 ID
     * @return 이미 적용된 이벤트면 true
     */
    public boolean isDuplicateEvent(UUID eventId) {
        return this.lastEventId != null && this.lastEventId.equals(eventId.toString());
    }

    /**
     * 전체 카운트를 재계산합니다 (이벤트 리플레이 시 사용)
     */
    public void recalculateTotalCount() {
        this.totalCount = this.likeCount + this.dislikeCount;
    }
}
