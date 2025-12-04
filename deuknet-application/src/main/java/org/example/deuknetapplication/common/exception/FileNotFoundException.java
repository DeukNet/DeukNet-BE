package org.example.deuknetapplication.common.exception;

/**
 * 파일을 찾을 수 없을 때 발생하는 예외
 */
public class FileNotFoundException extends ApplicationException {

    public FileNotFoundException(String fileName) {
        super(404, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다: " + fileName);
    }

    public FileNotFoundException(String fileName, Throwable cause) {
        super(404, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다: " + fileName);
        initCause(cause);
    }
}
