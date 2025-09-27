package org.example.deuknetdomain.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorStatus {

    public static final int BAD_REQUEST = 400;

    public static final int UNAUTHORIZED = 401;

    public static final int FORBIDDEN = 403;

    public static final int NOT_FOUND = 404;

    public static final int CONFLICT = 409;

    public static final int TOO_MANY_REQUEST = 429;

    public static final int INTERNAL_SERVER_ERROR = 500;
}
