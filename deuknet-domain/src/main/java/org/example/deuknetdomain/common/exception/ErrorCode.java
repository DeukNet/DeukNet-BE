package org.example.deuknetdomain.common.exception;

public interface ErrorCode {
    int getStatus();
    String getCode();
    String getMessage();
}
