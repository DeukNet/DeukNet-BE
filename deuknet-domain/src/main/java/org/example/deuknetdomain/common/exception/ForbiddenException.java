package org.example.deuknetdomain.common.exception;

/**
 * 권한이 없을 때 발생하는 예외
 * Domain-common에 위치하여 범용적으로 사용됩니다.
 */
public class ForbiddenException extends DomainException {

    public ForbiddenException(String message) {
        super(403, "FORBIDDEN", message);
    }
}
