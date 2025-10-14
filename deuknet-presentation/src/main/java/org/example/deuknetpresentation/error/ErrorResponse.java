package org.example.deuknetpresentation.error;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 에러 응답 DTO
 */
public record ErrorResponse(
        int status,
        String code,
        String message,
        LocalDateTime timestamp,
        List<FieldError> fieldErrors
) {
    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message, LocalDateTime.now(), null);
    }

    public static ErrorResponse of(int status, String code, String message, List<FieldError> fieldErrors) {
        return new ErrorResponse(status, code, message, LocalDateTime.now(), fieldErrors);
    }

    /**
     * 필드 검증 에러
     */
    public record FieldError(
            String field,
            Object rejectedValue,
            String message
    ) {
        public static FieldError of(String field, Object rejectedValue, String message) {
            return new FieldError(field, rejectedValue, message);
        }
    }
}
