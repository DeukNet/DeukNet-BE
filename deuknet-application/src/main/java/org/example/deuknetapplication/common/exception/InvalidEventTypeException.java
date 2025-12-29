package org.example.deuknetapplication.common.exception;

/**
 * 잘못된 이벤트 타입 예외
 */
public class InvalidEventTypeException extends ApplicationException {
    public InvalidEventTypeException(String typeName) {
        super(500, "INVALID_EVENT_TYPE", "Unknown event type: " + typeName);
    }
}
