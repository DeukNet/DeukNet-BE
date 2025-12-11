package org.example.deuknetdomain.common.seedwork;

import lombok.Getter;

import java.util.UUID;

/**
 * Projection의 기반 클래스
 * CQRS의 Query 모델로 사용되며, 읽기 전용(Immutable)입니다.
 * Entity와 달리 모든 필드를 final로 선언하여 불변성을 보장해야 합니다.
 */
@Getter
public abstract class Projection implements Persistable {

    private final UUID id;

    public Projection(UUID id) {
        this.id = id;
    }
}
