package org.example.deuknetdomain.common.seedwork;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity의 기반 클래스
 * Entity는 식별자(ID) 기반 동등성을 갖습니다.
 */
@Getter
public abstract class Entity implements Persistable {

    private final UUID id;

    public Entity(UUID id) {
        this.id = id;
    }

    /**
     * Entity는 ID 기반으로 동등성을 판단합니다.
     * 같은 ID를 가진 Entity는 동일한 객체로 간주됩니다.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;
        Entity entity = (Entity) o;
        return Objects.equals(id, entity.id);
    }

    /**
     * equals와 일관성 있는 hashCode
     * ID를 기반으로 hash 값을 계산합니다.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
