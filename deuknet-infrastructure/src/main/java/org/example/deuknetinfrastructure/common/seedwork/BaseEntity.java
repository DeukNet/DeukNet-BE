package org.example.deuknetinfrastructure.common.seedwork;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Persistable;

import java.util.Objects;
import java.util.UUID;

/**
 * JPA Entity의 기반 클래스
 * UUID 기반 식별자와 생성/수정 시각을 포함합니다.
 *
 * 이 클래스를 상속받는 JPA Entity는:
 * - UUID 타입의 ID
 * - createdAt, updatedAt 필드
 * - ID 기반 equals/hashCode
 * 를 자동으로 갖게 됩니다.
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity extends BaseTimeEntity implements Persistable {

    /**
     * 엔티티의 고유 식별자
     * UUID를 사용하여 분산 환경에서도 충돌 없이 ID를 생성할 수 있습니다.
     */
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    protected BaseEntity() {
        // JPA를 위한 기본 생성자
    }

    protected BaseEntity(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    /**
     * ID 기반 동등성 비교
     * 같은 ID를 가진 Entity는 동일한 객체로 간주됩니다.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    /**
     * ID 기반 해시코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
