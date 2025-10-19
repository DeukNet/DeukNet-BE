package org.example.deuknetdomain.common.exception;

import lombok.Getter;

/**
 * 도메인 레이어 예외의 기본 추상 클래스
 * 모든 도메인 예외는 이 클래스를 상속받습니다.
 * 각 예외는 HTTP 상태 코드, 에러 코드, 메시지를 가집니다.
 */
@Getter
public abstract class DomainException extends DeukNetException {

    protected DomainException(int status, String code, String message) {
        super(status, code, message);
    }
}
