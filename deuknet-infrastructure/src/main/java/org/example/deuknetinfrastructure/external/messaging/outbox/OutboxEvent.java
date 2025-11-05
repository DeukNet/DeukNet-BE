package org.example.deuknetinfrastructure.external.messaging.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.deuknetinfrastructure.common.seedwork.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transactional Outbox Pattern을 위한 이벤트 저장 엔티티
 *
 * 이벤트를 데이터베이스에 저장하여 최소 1회 전달(At-least-once delivery)을 보장합니다.
 * 별도의 스케줄러가 이 테이블을 폴링하여 이벤트를 메시지 브로커에 발행합니다.
 *
 * Application 레이어에서 넘긴 객체는 JSON으로 직렬화되어 payload에 저장됩니다.
 */
@Getter
@Setter
@Entity
@Table(name = "outbox_events")
public class OutboxEvent extends BaseEntity {

    /**
     * Aggregate 타입 (예: Post, Comment 등)
     */
    @Column(name = "aggregate_type", nullable = false, length = 255)
    private String aggregateType;

    /**
     * 이벤트 타입 (예: PostCreated, CommentAdded 등)
     */
    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;

    /**
     * 페이로드 타입 (완전한 클래스명)
     * 역직렬화 시 사용됩니다.
     * 예: "org.example.deuknetdomain.model.query.post.PostDetailProjection"
     */
    @Column(name = "payload_type", nullable = false, length = 255)
    private String payloadType;

    /**
     * 이벤트가 발생한 Aggregate의 ID
     */
    @Column(name = "aggregate_id", nullable = false, columnDefinition = "UUID")
    private UUID aggregateId;

    /**
     * 이벤트 발생 시각
     */
    @Column(name = "occurred_on", nullable = false, updatable = false)
    private LocalDateTime occurredOn;

    /**
     * 이벤트 페이로드 (JSON 형식)
     * Application에서 넘긴 객체가 JSON으로 직렬화되어 저장됩니다.
     */
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    /**
     * 이벤트 처리 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    /**
     * 재시도 횟수
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    /**
     * 마지막 처리 시각
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * 에러 메시지 (실패한 경우)
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    protected OutboxEvent() {
        super();
    }

    public OutboxEvent(UUID id, String aggregateType, String eventType, String payloadType, UUID aggregateId, String payload) {
        super(id);
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payloadType = payloadType;
        this.aggregateId = aggregateId;
        this.occurredOn = LocalDateTime.now();
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
    }

    /**
     * 이벤트 발행 처리 중으로 상태 변경
     */
    public void markAsProcessing() {
        this.status = OutboxStatus.PROCESSING;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 이벤트 발행 성공
     */
    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 이벤트 발행 실패
     */
    public void markAsFailed(String errorMessage) {
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    /**
     * 재시도 가능 여부 확인 (최대 3회)
     */
    public boolean canRetry() {
        return this.retryCount < 3;
    }
}
