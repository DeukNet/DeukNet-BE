package org.example.deuknetinfrastructure.common.seedwork;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Soft Delete를 지원하는 JPA Entity의 기반 클래스
 * 물리적으로 삭제하지 않고 삭제 시각과 삭제한 사용자를 기록합니다.
 *
 * 주의: Soft Delete된 엔티티를 조회에서 제외하려면
 * @Where(clause = "deleted_at IS NULL") 또는
 * @SQLDelete, @SQLRestriction 등을 활용해야 합니다.
 */
@Getter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseEntity implements org.example.deuknetdomain.common.seedwork.SoftDeletable {

    /**
     * 엔티티가 삭제된 시각
     * null이면 삭제되지 않은 상태입니다.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 엔티티를 삭제한 사용자의 ID
     * null이면 삭제되지 않았거나 사용자 정보가 없는 경우입니다.
     */
    @Column(name = "deleted_by", columnDefinition = "BINARY(16)")
    private UUID deletedBy;

    protected SoftDeletableEntity() {
        super();
    }

    protected SoftDeletableEntity(UUID id) {
        super(id);
    }

    /**
     * 엔티티를 삭제 상태로 표시합니다.
     * 물리적으로 삭제되지 않고 deletedAt 시각이 기록됩니다.
     */
    @Override
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        // deletedBy는 필요시 SecurityUtil 등을 통해 설정 가능
    }

    /**
     * 엔티티를 삭제 상태로 표시하고 삭제한 사용자를 기록합니다.
     *
     * @param deletedBy 삭제한 사용자의 ID
     */
    public void delete(UUID deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 엔티티가 삭제되었는지 확인합니다.
     *
     * @return deletedAt이 null이 아니면 true
     */
    @Override
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 삭제된 엔티티를 복원합니다.
     * deletedAt과 deletedBy를 null로 설정합니다.
     */
    @Override
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }
}
