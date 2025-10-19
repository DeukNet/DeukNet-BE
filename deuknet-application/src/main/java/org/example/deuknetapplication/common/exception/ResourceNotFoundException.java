package org.example.deuknetapplication.common.exception;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException() {
        super(404, "RESOURCE_NOT_FOUND", "Resource not found");
    }
}
