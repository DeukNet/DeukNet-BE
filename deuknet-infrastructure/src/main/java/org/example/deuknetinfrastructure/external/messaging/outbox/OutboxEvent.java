package org.example.deuknetinfrastructure.external.messaging.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Debezium Outbox Event Router 호환 엔티티
 *
 * Debezium의 Outbox Event Router SMT를 사용하기 위한 표준 테이블 구조입니다.
 *
 * 필수 컬럼 (Debezium 표준):
 * - id: 이벤트의 고유 식별자 (UUID)
 * - aggregatetype: Aggregate 타입
 * - aggregateid: Aggregate ID (문자열로 저장)
 * - type: 이벤트 타입
 * - payload: 이벤트 페이로드 (JSON)
 * - timestamp: 이벤트 발생 시각 (epoch milliseconds)
 *
 * Debezium Connector 설정 예시:
 * transforms=outbox
 * transforms.outbox.type=io.debezium.transforms.outbox.EventRouter
 * transforms.outbox.table.field.event.id=id
 * transforms.outbox.table.field.event.key=aggregateid
 * transforms.outbox.table.field.event.type=type
 * transforms.outbox.table.field.event.timestamp=timestamp
 * transforms.outbox.table.field.event.payload=payload
 * transforms.outbox.route.topic.replacement=${routedByValue}.events
 */
@Getter
@Setter
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    /**
     * 이벤트 고유 식별자
     * Debezium 필수 필드
     */
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    /**
     * Aggregate ID (문자열)
     * Debezium에서 메시지 키로 사용됨
     * Kafka 파티셔닝의 기준이 됩니다.
     */
    @Column(name = "aggregateid", nullable = false)
    private String aggregateid;

    /**
     * Aggregate 타입 (예: Post, Comment 등)
     * Debezium에서 토픽 라우팅에 사용됨
     */
    @Column(name = "aggregatetype", nullable = false, length = 255)
    private String aggregatetype;

    /**
     * 이벤트 타입 (예: PostCreated, CommentAdded 등)
     * Debezium 필수 필드
     */
    @Column(name = "type", nullable = false, length = 255)
    private String type;

    /**
     * 이벤트 페이로드 (JSON 형식)
     * Debezium 필수 필드
     */
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    /**
     * 이벤트 발생 시각 (epoch milliseconds)
     * Debezium 필수 필드
     */
    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    // 기존 Outbox 패턴 필드들 (하위 호환성 유지)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "aggregate_id", nullable = false, columnDefinition = "UUID")
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 255)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;

    @Column(name = "occurred_on", nullable = false)
    private LocalDateTime occurredOn;

    @Column(name = "payload_type", nullable = false, length = 255)
    private String payloadType;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    protected OutboxEvent() {
    }

    public OutboxEvent(UUID id, String aggregatetype, String aggregateid, String type, String payload, Long timestamp) {
        this.id = id;
        this.aggregatetype = aggregatetype;
        this.aggregateid = aggregateid;
        this.type = type;
        this.payload = payload;
        this.timestamp = timestamp;

        // 기존 필드들 기본값 설정
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.occurredOn = now;
        this.aggregateId = UUID.fromString(aggregateid);
        this.aggregateType = aggregatetype;
        this.eventType = type;
        this.payloadType = aggregatetype;  // aggregatetype을 payloadType으로 사용
        this.retryCount = 0;
        this.status = "PENDING";
    }
}
