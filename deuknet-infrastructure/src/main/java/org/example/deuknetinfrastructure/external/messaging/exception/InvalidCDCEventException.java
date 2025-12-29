package org.example.deuknetinfrastructure.external.messaging.exception;

import org.example.deuknetinfrastructure.common.exception.InfrastructureException;

/**
 * 잘못된 CDC 이벤트 예외
 */
public class InvalidCDCEventException extends InfrastructureException {
    public InvalidCDCEventException(String message) {
        super(500, "INVALID_CDC_EVENT", message);
    }
}
