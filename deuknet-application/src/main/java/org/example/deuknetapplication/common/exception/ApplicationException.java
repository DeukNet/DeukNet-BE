package org.example.deuknetapplication.common.exception;

import lombok.Getter;
import org.example.deuknetdomain.common.exception.DeukNetException;

/**
 * Application 레이어 예외의 기본 클래스
 * 유스케이스 실행 중 발생하는 애플리케이션 로직 예외를 처리합니다.
 * 각 예외는 HTTP 상태 코드, 에러 코드, 메시지를 가집니다.
 */
@Getter
public abstract class ApplicationException extends DeukNetException {

    protected ApplicationException(int status, String code, String message) {
        super(status, code, message);
    }
}
