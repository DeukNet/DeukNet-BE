package org.example.deuknetdomain.domain.permission;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Entity;

import java.util.UUID;

/**
 * 익명 권한 비밀번호 도메인 객체
 * 관리자가 SQL로 직접 관리하는 평문 비밀번호
 */
@Getter
public class AnonymousPermissionPassword extends Entity {

    private final String password;

    private AnonymousPermissionPassword(UUID id, String password) {
        super(id);
        this.password = password;
    }

    /**
     * 비밀번호 복원 (DB에서 조회 시 사용)
     */
    public static AnonymousPermissionPassword restore(UUID id, String password) {
        return new AnonymousPermissionPassword(id, password);
    }

    /**
     * 비밀번호 검증
     *
     * @param inputPassword 입력된 비밀번호
     * @return 비밀번호 일치 여부
     */
    public boolean matches(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }
}
