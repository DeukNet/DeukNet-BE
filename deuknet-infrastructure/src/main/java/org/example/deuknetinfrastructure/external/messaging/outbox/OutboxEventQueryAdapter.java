package org.example.deuknetinfrastructure.external.messaging.outbox;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Outbox 이벤트 조회 어댑터
 * <br>
 * Debezium CDC 방식에서는 이벤트 폴링이 필요 없지만,
 * 모니터링 및 관리 목적으로 사용할 수 있는 쿼리 메서드를 제공합니다.
 */
@Repository
@RequiredArgsConstructor
public class OutboxEventQueryAdapter {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 Aggregate의 이벤트 조회
     * <br>
     * @param aggregateId Aggregate ID (문자열)
     * @return 해당 Aggregate의 이벤트 목록 (timestamp 순)
     */
    public List<OutboxEvent> findByAggregateId(String aggregateId) {
        QOutboxEvent outboxEvent = QOutboxEvent.outboxEvent;
        return queryFactory
                .selectFrom(outboxEvent)
                .where(outboxEvent.aggregateid.eq(aggregateId))
                .orderBy(outboxEvent.timestamp.asc())
                .fetch();
    }

    /**
     * 특정 Aggregate 타입의 이벤트 조회
     * <br>
     * @param aggregateType Aggregate 타입 (예: Post, Comment)
     * @param limit 조회할 최대 개수
     * @return 해당 타입의 최근 이벤트 목록
     */
    public List<OutboxEvent> findByAggregateType(String aggregateType, int limit) {
        QOutboxEvent outboxEvent = QOutboxEvent.outboxEvent;
        return queryFactory
                .selectFrom(outboxEvent)
                .where(outboxEvent.aggregatetype.eq(aggregateType))
                .orderBy(outboxEvent.timestamp.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 특정 이벤트 타입의 통계 조회
     * <br>
     * @param eventType 이벤트 타입
     * @return 해당 타입의 이벤트 수
     */
    public long countByEventType(String eventType) {
        QOutboxEvent outboxEvent = QOutboxEvent.outboxEvent;
        Long count = queryFactory
                .select(outboxEvent.count())
                .from(outboxEvent)
                .where(outboxEvent.type.eq(eventType))
                .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * 최근 이벤트 조회 (모니터링용)
     * <br>
     * @param limit 조회할 최대 개수
     * @return 최근 이벤트 목록
     */
    public List<OutboxEvent> findRecentEvents(int limit) {
        QOutboxEvent outboxEvent = QOutboxEvent.outboxEvent;
        return queryFactory
                .selectFrom(outboxEvent)
                .orderBy(outboxEvent.timestamp.desc())
                .limit(limit)
                .fetch();
    }
}
