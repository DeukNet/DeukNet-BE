package org.example.deuknetdomain.common.seedwork;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 엔티티의 생성 및 수정 이력을 추적하는 인터페이스
 * 감사(Audit) 정보가 필요한 엔티티에서 구현합니다.
 */
public interface Auditable {

    /**
     * 엔티티가 생성된 시각을 반환합니다.
     */
    LocalDateTime getCreatedAt();

    /**
     * 엔티티가 마지막으로 수정된 시각을 반환합니다.
     */
    LocalDateTime getUpdatedAt();

    /**
     * 엔티티를 생성한 사용자의 ID를 반환합니다.
     * 사용자 정보가 필요 없는 경우 null을 반환할 수 있습니다.
     */
    default UUID getCreatedBy() {
        return null;
    }

    /**
     * 엔티티를 마지막으로 수정한 사용자의 ID를 반환합니다.
     * 사용자 정보가 필요 없는 경우 null을 반환할 수 있습니다.
     */
    default UUID getUpdatedBy() {
        return null;
    }
}
