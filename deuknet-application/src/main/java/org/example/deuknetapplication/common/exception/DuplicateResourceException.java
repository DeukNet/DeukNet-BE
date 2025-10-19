package org.example.deuknetapplication.common.exception;

/**
 * 중복된 리소스가 존재할 때 발생하는 예외
 */
public class DuplicateResourceException extends ApplicationException {

    public DuplicateResourceException() {
        super(409, "DUPLICATE_RESOURCE", "Resource already exists");
    }
}
