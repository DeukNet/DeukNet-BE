package org.example.deuknetinfrastructure.external.messaging.exception;

import org.example.deuknetinfrastructure.common.exception.InfrastructureException;

/**
 * 이벤트 발행 실패 시 발생하는 예외
 * Outbox 이벤트 저장, 직렬화, 메시지 브로커 발행 등의 오류를 처리합니다.
 */
public class EventPublishException extends InfrastructureException {

    public EventPublishException(String message) {
        super(500, "EVENT_PUBLISH_FAILED", message);
    }

    public EventPublishException(String message, Throwable cause) {
        super(500, "EVENT_PUBLISH_FAILED", message + ": " + cause.getMessage());
    }
}
