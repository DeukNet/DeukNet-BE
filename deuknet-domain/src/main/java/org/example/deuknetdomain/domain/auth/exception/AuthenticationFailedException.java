package org.example.deuknetdomain.domain.auth.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 인증에 실패했을 때 발생하는 예외
 */
public class AuthenticationFailedException extends DomainException {

    public AuthenticationFailedException() {
        super(401, "AUTHENTICATION_FAILED", "Authentication failed");
    }
}
