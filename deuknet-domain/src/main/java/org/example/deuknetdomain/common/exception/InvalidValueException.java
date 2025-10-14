package org.example.deuknetdomain.common.exception;

public class InvalidValueException extends DomainException {
    
    public InvalidValueException(String message) {
        super(new ErrorCode() {
            @Override
            public int getStatus() {
                return 400;
            }

            @Override
            public String getCode() {
                return "INVALID_VALUE";
            }

            @Override
            public String getMessage() {
                return message;
            }
        });
    }
}
