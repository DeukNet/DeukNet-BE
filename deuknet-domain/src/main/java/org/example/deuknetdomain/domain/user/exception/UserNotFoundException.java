package org.example.deuknetdomain.domain.user.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * User를 찾을 수 없을 때 발생하는 예외
 */
public class UserNotFoundException extends DomainException {

    public UserNotFoundException() {
        super(404, "USER_NOT_FOUND", "User not found");
    }
}
