package org.example.deuknetinfrastructure.common.seedwork;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 감사(Audit) 정보와 Soft Delete를 모두 지원하는 JPA Entity의 기반 클래스
 *
 * 포함하는 필드:
 * - id: UUID 식별자
 * - createdAt, updatedAt: 생성/수정 시각
 * - createdBy, updatedBy: 생성/수정한 사용자
 * - deletedAt, deletedBy: 삭제 시각 및 삭제한 사용자
 *
 * 사용하려면:
 * 1. @EnableJpaAuditing을 설정 클래스에 추가
 * 2. AuditorAware<UUID> 구현체를 빈으로 등록
 */
@Getter
@MappedSuperclass
public abstract class AuditableSoftDeletableEntity extends BaseEntity
        implements org.example.deuknetdomain.common.seedwork.Auditable,
                   org.example.deuknetdomain.common.seedwork.SoftDeletable {

    /**
     * 엔티티를 생성한 사용자의 ID
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false, columnDefinition = "BINARY(16)")
    private UUID createdBy;

    /**
     * 엔티티를 마지막으로 수정한 사용자의 ID
     */
    @LastModifiedBy
    @Column(name = "updated_by", columnDefinition = "BINARY(16)")
    private UUID updatedBy;

    /**
     * 엔티티가 삭제된 시각
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 엔티티를 삭제한 사용자의 ID
     */
    @Column(name = "deleted_by", columnDefinition = "BINARY(16)")
    private UUID deletedBy;

    protected AuditableSoftDeletableEntity() {
        super();
    }

    protected AuditableSoftDeletableEntity(UUID id) {
        super(id);
    }

    /**
     * 엔티티를 삭제 상태로 표시합니다.
     */
    @Override
    public void delete() {
        this.deletedAt = LocalDateTime.now();
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
     */
    @Override
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 삭제된 엔티티를 복원합니다.
     */
    @Override
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }
}
