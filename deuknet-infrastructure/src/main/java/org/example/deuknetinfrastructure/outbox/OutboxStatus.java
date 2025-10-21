package org.example.deuknetinfrastructure.outbox;

/**
 * Outbox 이벤트의 처리 상태
 */
public enum OutboxStatus {
    /**
     * 이벤트가 생성되었지만 아직 발행되지 않음
     */
    PENDING,

    /**
     * 이벤트 발행 중
     */
    PROCESSING,

    /**
     * 이벤트가 성공적으로 발행됨
     */
    PUBLISHED,

    /**
     * 이벤트 발행 실패 (재시도 가능)
     */
    FAILED
}
