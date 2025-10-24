package org.example.deuknetinfrastructure.external.search.document;

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
 * 댓글 통계 정보 Elasticsearch Document (Event Sourcing)
 *
 * Event Sourcing 방식으로 CommentAdded/CommentRemoved, ReactionAdded/ReactionRemoved
 * 이벤트를 처리하여 댓글별 집계 데이터를 저장합니다.
 *
 * Event Sourcing 메타데이터:
 * - version: Optimistic Locking 및 이벤트 순서 보장
 * - lastEventId: 마지막 적용된 이벤트 ID (중복 방지)
 * - lastEventTimestamp: 마지막 이벤트 발생 시각
 * - eventCount: 적용된 총 이벤트 수
 */
@Getter
@Setter
@Document(indexName = "comment_counts")
public class CommentCountDocument extends BaseDocument {

    // ========== Event Sourcing 메타데이터 ==========

    /**
     * Projection 버전 (Event Sourcing)
     */
    @Field(type = FieldType.Long)
    private Long version;

    /**
     * 마지막 적용된 이벤트 ID
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
     * 대댓글 수
     */
    @Field(type = FieldType.Long)
    private Long replyCount;

    /**
     * 리액션 수 (좋아요 등)
     */
    @Field(type = FieldType.Long)
    private Long reactionCount;

    protected CommentCountDocument() {
        super();
    }

    public CommentCountDocument(UUID commentId) {
        super(commentId);
        // Event Sourcing 메타데이터 초기화
        this.version = 0L;
        this.lastEventId = null;
        this.lastEventTimestamp = null;
        this.eventCount = 0L;
        // 집계 데이터 초기화
        this.replyCount = 0L;
        this.reactionCount = 0L;
    }

    /**
     * CommentCount Document를 생성합니다 (초기 상태)
     *
     * @param commentId 댓글 ID
     * @return 생성된 Document
     */
    public static CommentCountDocument create(UUID commentId) {
        return new CommentCountDocument(commentId);
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
     * 대댓글 수를 증가시킵니다 (Event Sourcing)
     *
     * @param eventId 이벤트 ID
     * @param occurredAt 이벤트 발생 시각
     */
    public void incrementReplyCount(UUID eventId, LocalDateTime occurredAt) {
        this.replyCount++;
        applyEvent(eventId, occurredAt);
    }

    /**
     * 대댓글 수를 감소시킵니다 (Event Sourcing)
     *
     * @param eventId 이벤트 ID
     * @param occurredAt 이벤트 발생 시각
     */
    public void decrementReplyCount(UUID eventId, LocalDateTime occurredAt) {
        if (this.replyCount > 0) {
            this.replyCount--;
        }
        applyEvent(eventId, occurredAt);
    }

    /**
     * 리액션 수를 증가시킵니다 (Event Sourcing)
     *
     * @param eventId 이벤트 ID
     * @param occurredAt 이벤트 발생 시각
     */
    public void incrementReactionCount(UUID eventId, LocalDateTime occurredAt) {
        this.reactionCount++;
        applyEvent(eventId, occurredAt);
    }

    /**
     * 리액션 수를 감소시킵니다 (Event Sourcing)
     *
     * @param eventId 이벤트 ID
     * @param occurredAt 이벤트 발생 시각
     */
    public void decrementReactionCount(UUID eventId, LocalDateTime occurredAt) {
        if (this.reactionCount > 0) {
            this.reactionCount--;
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
}
