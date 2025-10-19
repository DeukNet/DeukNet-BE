package org.example.deuknetapplication.common.exception;

/**
 * 리소스가 사용 중일 때 발생하는 예외
 */
public class ResourceInUseException extends ApplicationException {

    public ResourceInUseException() {
        super(409, "RESOURCE_IN_USE", "Resource is currently in use");
    }
}
