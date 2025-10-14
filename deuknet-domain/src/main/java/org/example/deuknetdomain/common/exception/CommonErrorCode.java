package org.example.deuknetdomain.common.exception;

public enum CommonErrorCode implements ErrorCode {
    
    // 400 Bad Request
    INVALID_INPUT_VALUE(400, "C001", "Invalid input value"),
    INVALID_TYPE_VALUE(400, "C002", "Invalid type value"),
    
    // 401 Unauthorized
    UNAUTHORIZED(401, "C003", "Unauthorized"),
    
    // 403 Forbidden
    FORBIDDEN(403, "C004", "Forbidden"),
    ACCESS_DENIED(403, "C005", "Access denied"),
    
    // 404 Not Found
    ENTITY_NOT_FOUND(404, "C006", "Entity not found"),
    
    // 409 Conflict
    DUPLICATE_RESOURCE(409, "C007", "Duplicate resource"),
    
    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(500, "C008", "Internal server error");

    private final int status;
    private final String code;
    private final String message;

    CommonErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
