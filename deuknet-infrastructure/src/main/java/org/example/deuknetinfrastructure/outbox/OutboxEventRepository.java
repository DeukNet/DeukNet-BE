package org.example.deuknetinfrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Outbox 이벤트 저장소
 *
 * 기본 CRUD 작업만 제공합니다.
 * 복잡한 쿼리는 OutboxEventQueryAdapter에서 QueryDSL로 구현합니다.
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Aggregate ID로 Outbox 이벤트 조회 (테스트용)
     */
    List<OutboxEvent> findByAggregateId(UUID aggregateId);
}
