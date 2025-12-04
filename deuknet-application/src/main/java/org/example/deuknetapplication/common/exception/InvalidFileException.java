package org.example.deuknetapplication.common.exception;

/**
 * 유효하지 않은 파일일 때 발생하는 예외
 */
public class InvalidFileException extends ApplicationException {

    public InvalidFileException(String message) {
        super(400, "INVALID_FILE", message);
    }
}
