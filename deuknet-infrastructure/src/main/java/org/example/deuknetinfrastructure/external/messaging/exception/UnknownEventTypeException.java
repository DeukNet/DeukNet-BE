package org.example.deuknetinfrastructure.external.messaging.exception;

import org.example.deuknetinfrastructure.common.exception.InfrastructureException;

/**
 * 알 수 없는 이벤트 타입 예외
 */
public class UnknownEventTypeException extends InfrastructureException {
    public UnknownEventTypeException(String typeName) {
        super(500, "UNKNOWN_EVENT_TYPE", "Unknown event type: " + typeName);
    }
}
