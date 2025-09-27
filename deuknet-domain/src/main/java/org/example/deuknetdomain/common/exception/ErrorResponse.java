package org.example.deuknetdomain.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final int status;

    private final String message;

    public static ErrorResponse from(ErrorCodes errorProperty) {
        return new ErrorResponse(errorProperty.getStatus(), errorProperty.getMessage());
    }
}
