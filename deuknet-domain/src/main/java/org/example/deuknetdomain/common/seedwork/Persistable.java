package org.example.deuknetdomain.common.seedwork;

import java.util.UUID;

/**
 * 영속성 대상 객체를 나타내는 인터페이스
 */
public interface Persistable {

    /**
     * 엔티티의 식별자를 반환합니다.
     */
    UUID getId();

    /**
     * 엔티티가 새로 생성된 것인지 확인합니다.
     * ID가 null이면 아직 영속화되지 않은 새 엔티티로 간주합니다.
     *
     * @return ID가 null이면 true, 아니면 false
     */
    default boolean isNew() {
        return getId() == null;
    }
}
