package org.example.deuknetdomain.common.exception;

public interface ErrorCodes {

    CommunityException throwException();

    int getStatus();

    String getMessage();
}
