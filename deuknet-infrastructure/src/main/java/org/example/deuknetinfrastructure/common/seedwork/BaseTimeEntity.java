package org.example.deuknetinfrastructure.common.seedwork;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA Entity의 생성/수정 시각을 자동으로 관리하는 기반 클래스
 * Spring Data JPA의 Auditing 기능을 활용합니다.
 *
 * 사용하려면 @EnableJpaAuditing을 설정 클래스에 추가해야 합니다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    /**
     * 엔티티가 생성된 시각
     * INSERT 시 자동으로 현재 시각이 설정됩니다.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 엔티티가 마지막으로 수정된 시각
     * INSERT 및 UPDATE 시 자동으로 현재 시각이 설정됩니다.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
