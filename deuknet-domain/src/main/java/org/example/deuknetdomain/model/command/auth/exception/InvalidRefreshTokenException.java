package org.example.deuknetdomain.model.command.auth.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * Refresh 토큰이 유효하지 않을 때 발생하는 예외
 */
public class InvalidRefreshTokenException extends DomainException {

    public InvalidRefreshTokenException() {
        super(401, "INVALID_REFRESH_TOKEN", "Invalid refresh token");
    }
}
