package org.example.deuknetdomain.common.exception;

/**
 * 유효하지 않은 값일 때 발생하는 예외
 * Domain-common에 위치하여 범용적으로 사용됩니다.
 */
public class InvalidValueException extends DomainException {

    public InvalidValueException(String message) {
        super(400, "INVALID_VALUE", message);
    }
}
