package org.example.deuknetapplication.port.in.permission;

/**
 * 익명 권한 확인 유스케이스
 */
public interface CheckAnonymousAccessUseCase {

    /**
     * 현재 사용자의 익명 접근 권한 확인
     *
     * @return 익명 접근 권한 여부
     */
    boolean hasAnonymousAccess();
}
