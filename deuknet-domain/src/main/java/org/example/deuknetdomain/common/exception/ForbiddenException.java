package org.example.deuknetdomain.common.exception;

public class ForbiddenException extends DomainException {
    
    public ForbiddenException(String message) {
        super(new ErrorCode() {
            @Override
            public int getStatus() {
                return 403;
            }

            @Override
            public String getCode() {
                return "FORBIDDEN";
            }

            @Override
            public String getMessage() {
                return message;
            }
        });
    }
}
