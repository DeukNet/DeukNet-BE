package org.example.deuknetdomain.common.exception;

import lombok.Getter;

@Getter
public class CommunityException extends RuntimeException{

    private final ErrorCodes errorCodes;

    public CommunityException(ErrorCodes errorCodes) {
        super(errorCodes.getMessage());
        this.errorCodes = errorCodes;
    }
}
