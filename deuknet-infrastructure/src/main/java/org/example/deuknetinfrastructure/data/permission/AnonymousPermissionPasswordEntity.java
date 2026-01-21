package org.example.deuknetinfrastructure.data.permission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 익명 권한 비밀번호 JPA Entity
 * 관리자가 SQL로 직접 관리하는 평문 비밀번호
 */
@Setter
@Getter
@Entity
@Table(name = "anonymous_permission_passwords")
public class AnonymousPermissionPasswordEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String password;

    public AnonymousPermissionPasswordEntity() {
    }

    public AnonymousPermissionPasswordEntity(UUID id, String password) {
        this.id = id;
        this.password = password;
    }
}
