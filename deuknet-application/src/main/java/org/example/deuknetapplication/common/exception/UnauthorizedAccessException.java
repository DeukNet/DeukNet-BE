package org.example.deuknetapplication.common.exception;

/**
 * 인증되지 않은 접근 시 발생하는 예외
 */
public class UnauthorizedAccessException extends ApplicationException {

    public UnauthorizedAccessException() {
        super(401, "UNAUTHORIZED_ACCESS", "Unauthorized access");
    }
}
