package org.example.deuknetdomain.domain.auth.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 토큰이 만료되었을 때 발생하는 예외
 */
public class TokenExpiredException extends DomainException {

    public TokenExpiredException() {
        super(401, "TOKEN_EXPIRED", "Token has expired");
    }
}
