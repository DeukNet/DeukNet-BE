package org.example.deuknetdomain.model.command.auth.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 토큰이 유효하지 않을 때 발생하는 예외
 */
public class InvalidTokenException extends DomainException {

    public InvalidTokenException() {
        super(401, "INVALID_TOKEN", "Invalid token");
    }
}
