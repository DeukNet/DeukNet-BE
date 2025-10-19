package org.example.deuknetdomain.model.command.user.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 동일한 이메일의 User가 이미 존재할 때 발생하는 예외
 */
public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException() {
        super(409, "USER_ALREADY_EXISTS", "User with this email already exists");
    }
}
