package org.example.deuknetapplication.common.exception;

/**
 * 리소스의 소유자가 아닌 사용자가 작업을 수행하려고 할 때 발생하는 예외
 */
public class OwnerMismatchException extends ApplicationException {

    public OwnerMismatchException() {
        super(403, "OWNER_MISMATCH", "Only the owner can perform this action");
    }
}
