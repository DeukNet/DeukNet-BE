package org.example.deuknetdomain.domain.permission.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 권한 비밀번호가 설정되지 않았을 때 발생하는 예외
 */
public class PermissionPasswordNotFoundException extends DomainException {

    public PermissionPasswordNotFoundException() {
        super(404, "PERMISSION_PASSWORD_NOT_FOUND", "Permission password not configured");
    }
}
