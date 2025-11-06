package org.example.deuknetinfrastructure.external.messaging.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
@Table(name = "outbox")
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

    protected OutboxEvent() {
    }

    public OutboxEvent(UUID id, String aggregatetype, String aggregateid, String type, String payload, Long timestamp) {
        this.id = id;
        this.aggregatetype = aggregatetype;
        this.aggregateid = aggregateid;
        this.type = type;
        this.payload = payload;
        this.timestamp = timestamp;
    }
}
