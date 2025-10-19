package org.example.deuknetdomain.common.seedwork;

import java.util.Objects;

/**
 * Value Object의 기반 클래스
 * Value Object는 불변이며 속성 기반 동등성을 갖습니다.
 */
public abstract class ValueObject implements Immutable {

    /**
     * Value Object는 모든 속성 기반으로 동등성을 판단해야 합니다.
     * 서브클래스에서 반드시 구현해야 합니다.
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * equals와 일관성 있는 hashCode를 구현해야 합니다.
     * 서브클래스에서 반드시 구현해야 합니다.
     */
    @Override
    public abstract int hashCode();

    /**
     * Value Object 생성 시 유효성 검증을 위한 템플릿 메소드
     * 서브클래스에서 필요시 오버라이드
     */
    protected void validate() {
        // 서브클래스에서 구현
    }

    /**
     * 두 Value Object의 속성 기반 동등성 검사를 위한 유틸리티 메소드
     */
    protected boolean equalsByProperties(Object o, Object... properties) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.hash(properties) == properties.hashCode();
    }
}
