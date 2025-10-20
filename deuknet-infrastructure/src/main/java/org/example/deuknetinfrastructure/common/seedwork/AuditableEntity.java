package org.example.deuknetinfrastructure.common.seedwork;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.util.UUID;

/**
 * 감사(Audit) 정보를 포함하는 JPA Entity의 기반 클래스
 * 생성/수정 시각뿐만 아니라 생성/수정한 사용자 정보도 자동으로 관리합니다.
 *
 * 사용하려면 AuditorAware<UUID> 구현체를 빈으로 등록해야 합니다:
 * <pre>
 * {@code
 * @Bean
 * public AuditorAware<UUID> auditorProvider() {
 *     return () -> {
 *         // 현재 로그인한 사용자의 UUID를 반환
 *         return Optional.of(SecurityUtil.getCurrentUserId());
 *     };
 * }
 * }
 * </pre>
 */
@Getter
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity implements org.example.deuknetdomain.common.seedwork.Auditable {

    /**
     * 엔티티를 생성한 사용자의 ID
     * INSERT 시 자동으로 현재 사용자 ID가 설정됩니다.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false, columnDefinition = "BINARY(16)")
    private UUID createdBy;

    /**
     * 엔티티를 마지막으로 수정한 사용자의 ID
     * INSERT 및 UPDATE 시 자동으로 현재 사용자 ID가 설정됩니다.
     */
    @LastModifiedBy
    @Column(name = "updated_by", columnDefinition = "BINARY(16)")
    private UUID updatedBy;

    protected AuditableEntity() {
        super();
    }

    protected AuditableEntity(UUID id) {
        super(id);
    }
}
