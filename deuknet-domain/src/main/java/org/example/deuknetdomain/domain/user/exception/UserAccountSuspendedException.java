package org.example.deuknetdomain.domain.user.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * User 계정이 정지되었을 때 발생하는 예외
 */
public class UserAccountSuspendedException extends DomainException {

    public UserAccountSuspendedException() {
        super(403, "USER_ACCOUNT_SUSPENDED", "User account is suspended");
    }
}
