package org.example.deuknetapplication.port.in.permission;

/**
 * 익명 권한 신청 유스케이스
 */
public interface RequestAnonymousAccessUseCase {

    /**
     * 익명 권한 신청
     * 비밀번호 검증 후 현재 사용자에게 익명 접근 권한 부여
     *
     * @param command 비밀번호가 포함된 커맨드
     */
    void requestAnonymousAccess(RequestAnonymousAccessCommand command);
}
