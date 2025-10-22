package org.example.deuknetdomain.domain.user.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 이메일 형식이 유효하지 않을 때 발생하는 예외
 */
public class InvalidEmailFormatException extends DomainException {

    public InvalidEmailFormatException() {
        super(400, "INVALID_EMAIL_FORMAT", "Invalid email format");
    }
}
