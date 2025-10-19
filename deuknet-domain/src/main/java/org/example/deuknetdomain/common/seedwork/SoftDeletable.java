package org.example.deuknetdomain.common.seedwork;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Soft Delete를 지원하는 엔티티를 위한 인터페이스
 * 물리적 삭제 대신 삭제 상태를 표시하여 데이터를 보존합니다.
 */
public interface SoftDeletable {

    /**
     * 엔티티를 삭제 상태로 표시합니다.
     * 물리적으로 삭제되지 않고 deletedAt 시각이 기록됩니다.
     */
    void delete();

    /**
     * 엔티티가 삭제되었는지 확인합니다.
     *
     * @return 삭제되었으면 true, 아니면 false
     */
    boolean isDeleted();

    /**
     * 엔티티가 삭제된 시각을 반환합니다.
     *
     * @return 삭제된 시각, 삭제되지 않았으면 null
     */
    LocalDateTime getDeletedAt();

    /**
     * 엔티티를 삭제한 사용자의 ID를 반환합니다.
     * 사용자 정보가 필요 없는 경우 null을 반환할 수 있습니다.
     *
     * @return 삭제한 사용자 ID, 없으면 null
     */
    default UUID getDeletedBy() {
        return null;
    }

    /**
     * 삭제된 엔티티를 복원합니다.
     * 필요한 경우 서브클래스에서 구현합니다.
     */
    default void restore() {
        throw new UnsupportedOperationException("Restore is not supported");
    }
}
