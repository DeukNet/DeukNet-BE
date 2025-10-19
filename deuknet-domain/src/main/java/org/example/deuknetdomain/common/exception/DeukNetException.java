package org.example.deuknetdomain.common.exception;

import lombok.Getter;

@Getter
public class DeukNetException extends RuntimeException {

    private final int status;
    private final String code;

    protected DeukNetException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
