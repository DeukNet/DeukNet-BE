package org.example.deuknetdomain.domain.permission.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 권한 비밀번호가 일치하지 않을 때 발생하는 예외
 */
public class InvalidPermissionPasswordException extends DomainException {

    public InvalidPermissionPasswordException() {
        super(401, "INVALID_PERMISSION_PASSWORD", "Invalid permission password");
    }
}
