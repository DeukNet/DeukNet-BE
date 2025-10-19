package org.example.deuknetinfrastructure.common.exception;

import lombok.Getter;
import org.example.deuknetdomain.common.exception.DeukNetException;

/**
 * Infrastructure 레이어 예외의 기본 추상 클래스
 * 외부 시스템 연동, 데이터베이스 접근 등 인프라 관련 예외를 처리합니다.
 */
@Getter
public abstract class InfrastructureException extends DeukNetException {

    protected InfrastructureException(int status, String code, String message) {
        super(status, code, message);
    }
}
