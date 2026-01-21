package org.example.deuknetinfrastructure.data.permission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * 익명 권한 비밀번호 Spring Data JPA Repository
 */
public interface JpaAnonymousPermissionPasswordRepository extends JpaRepository<AnonymousPermissionPasswordEntity, UUID> {

    /**
     * 첫 번째 비밀번호 조회 (단일 레코드만 존재한다고 가정)
     */
    Optional<AnonymousPermissionPasswordEntity> findFirstBy();
}
