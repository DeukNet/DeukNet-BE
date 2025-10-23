package org.example.deuknetdomain.common.seedwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Event Sourcing 기반 Aggregate Root
 *
 * Event Sourcing 패턴을 사용하는 Aggregate는 이 클래스를 상속합니다.
 * 모든 상태 변경은 이벤트를 통해 이루어지며, 이벤트를 replay하여
 * 현재 상태를 재구성할 수 있습니다.
 *
 * 주요 기능:
 * - 도메인 이벤트 수집 및 관리
 * - 이벤트 발행 (Outbox Pattern과 연동)
 * - 이벤트 replay를 통한 상태 재구성
 *
 * 사용 예시:
 * <pre>
 * public class ReactionAggregate extends EventSourcedAggregate {
 *     private Long likeCount;
 *
 *     public void addLike() {
 *         ReactionAddedEvent event = new ReactionAddedEvent(...);
 *         addEvent(event);
 *         apply(event);
 *     }
 *
 *     private void apply(ReactionAddedEvent event) {
 *         this.likeCount++;
 *     }
 * }
 * </pre>
 */
public abstract class EventSourcedAggregate extends AggregateRoot {

    /**
     * 발생한 도메인 이벤트 목록
     *
     * Aggregate에서 발생한 모든 이벤트가 저장됩니다.
     * 트랜잭션 커밋 시 Outbox로 발행됩니다.
     */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Aggregate 버전
     *
     * 이벤트가 적용될 때마다 증가합니다.
     * Optimistic Locking 및 이벤트 순서 보장에 사용됩니다.
     */
    private Long version;

    protected EventSourcedAggregate(UUID id) {
        super(id);
        this.version = 0L;
    }

    protected EventSourcedAggregate(UUID id, Long version) {
        super(id);
        this.version = version != null ? version : 0L;
    }

    /**
     * 도메인 이벤트를 추가합니다.
     *
     * Aggregate에서 비즈니스 로직 수행 후 이벤트를 발생시킬 때 사용합니다.
     * 추가된 이벤트는 getUncommittedEvents()로 조회할 수 있습니다.
     *
     * @param event 발생한 도메인 이벤트
     */
    protected void addEvent(DomainEvent event) {
        this.domainEvents.add(event);
        this.version++;
    }

    /**
     * 아직 커밋되지 않은 도메인 이벤트 목록을 반환합니다.
     *
     * Service layer에서 이 메서드를 호출하여 발생한 이벤트를
     * Outbox로 발행합니다.
     *
     * @return 발생한 도메인 이벤트 목록 (읽기 전용)
     */
    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 이벤트 목록을 초기화합니다.
     *
     * 이벤트가 Outbox에 저장된 후 호출하여
     * 중복 발행을 방지합니다.
     */
    public void clearEvents() {
        this.domainEvents.clear();
    }

    /**
     * 현재 Aggregate 버전을 반환합니다.
     *
     * @return Aggregate 버전
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Aggregate 버전을 설정합니다.
     *
     * Event replay 시 사용됩니다.
     *
     * @param version 설정할 버전
     */
    protected void setVersion(Long version) {
        this.version = version;
    }

    /**
     * 발생한 이벤트가 있는지 확인합니다.
     *
     * @return 이벤트가 있으면 true
     */
    public boolean hasUncommittedEvents() {
        return !domainEvents.isEmpty();
    }
}
