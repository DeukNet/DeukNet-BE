package org.example.deuknetdomain.common.seedwork;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event Sourcing 기반 Projection의 기반 클래스
 *
 * Event Sourcing으로 재구성되는 Projection은 이 클래스를 상속합니다.
 * 이벤트 replay를 통해 현재 상태를 재구성하며, 버전 관리를 통해
 * 이벤트 순서와 일관성을 보장합니다.
 *
 * 주요 기능:
 * - 버전 관리: Optimistic Locking, 이벤트 순서 보장
 * - 이벤트 추적: 마지막 적용된 이벤트 ID와 시각
 * - 재구성 지원: 이벤트 카운트로 rebuild 여부 판단
 *
 * Event Sourcing 원칙:
 * - Projection은 이벤트의 결과물 (Materialized View)
 * - 언제든지 이벤트를 replay하여 재구성 가능
 * - 버전 충돌 시 이벤트 재적용으로 해결
 */
@Getter
public abstract class EventSourcingProjection extends Projection {

    /**
     * Projection 버전
     *
     * 이벤트가 적용될 때마다 증가합니다.
     * Optimistic Locking 및 이벤트 순서 보장에 사용됩니다.
     *
     * 예시:
     * - version=0: 초기 상태
     * - version=5: 5개의 이벤트가 적용됨
     */
    private final Long version;

    /**
     * 마지막으로 적용된 이벤트 ID
     *
     * Event replay 시 중복 적용 방지에 사용됩니다.
     * 이 ID 이후의 이벤트만 적용하면 됩니다.
     */
    private final UUID lastEventId;

    /**
     * 마지막 이벤트 발생 시각
     *
     * Projection이 얼마나 최신인지 확인할 수 있습니다.
     * 모니터링 및 디버깅에 유용합니다.
     */
    private final LocalDateTime lastEventTimestamp;

    /**
     * 적용된 이벤트 수
     *
     * 이 Projection을 구성하는데 사용된 총 이벤트 수입니다.
     * rebuild 필요 여부 판단에 사용할 수 있습니다.
     */
    private final Long eventCount;

    protected EventSourcingProjection(UUID id, Long version, UUID lastEventId,
                                      LocalDateTime lastEventTimestamp, Long eventCount) {
        super(id);
        this.version = version != null ? version : 0L;
        this.lastEventId = lastEventId;
        this.lastEventTimestamp = lastEventTimestamp;
        this.eventCount = eventCount != null ? eventCount : 0L;
    }

    /**
     * 다음 버전 번호를 반환합니다.
     */
    public Long getNextVersion() {
        return this.version + 1;
    }

    /**
     * 이벤트 적용 후 다음 이벤트 카운트를 반환합니다.
     */
    public Long getNextEventCount() {
        return this.eventCount + 1;
    }

    /**
     * Projection이 초기 상태(이벤트 미적용)인지 확인합니다.
     */
    public boolean isInitialState() {
        return this.version == 0L && this.eventCount == 0L;
    }
}
