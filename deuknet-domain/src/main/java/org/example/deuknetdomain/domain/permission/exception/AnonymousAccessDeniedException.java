package org.example.deuknetdomain.domain.permission.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 익명 접근 권한이 없을 때 발생하는 예외
 */
public class AnonymousAccessDeniedException extends DomainException {

    public AnonymousAccessDeniedException() {
        super(403, "ANONYMOUS_ACCESS_DENIED", "Anonymous access permission required");
    }
}
