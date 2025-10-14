package org.example.deuknetdomain.common.exception;

public class BusinessException extends DomainException {
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
