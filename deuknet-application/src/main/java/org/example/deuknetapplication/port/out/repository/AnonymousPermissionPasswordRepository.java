package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.domain.permission.AnonymousPermissionPassword;

import java.util.Optional;

/**
 * 익명 권한 비밀번호 저장소 포트
 */
public interface AnonymousPermissionPasswordRepository {

    /**
     * 현재 설정된 비밀번호 조회 (단일 레코드만 존재)
     *
     * @return 비밀번호 도메인 객체
     */
    Optional<AnonymousPermissionPassword> findFirst();
}
