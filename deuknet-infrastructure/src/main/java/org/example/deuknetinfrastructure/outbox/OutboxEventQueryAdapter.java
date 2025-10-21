package org.example.deuknetinfrastructure.outbox;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// import static org.example.deuknetinfrastructure.outbox.QOutboxEvent.outboxEvent;

/**
 * Outbox 이벤트 조회 어댑터
 *
 * QueryDSL을 사용하여 타입 안전한 복잡한 쿼리를 제공합니다.
 * Repository의 문자열 기반 JPQL 대신 컴파일 타임 검증이 가능합니다.
 */
@Repository
@RequiredArgsConstructor
public class OutboxEventQueryAdapter {

    private final JPAQueryFactory queryFactory;

    /**
     * 발행 대기 중인 이벤트 조회
     *
     * 오래된 것부터 처리하기 위해 생성 시각 기준 정렬합니다.
     *
     * @param limit 조회할 최대 개수
     * @return 발행 대기 중인 이벤트 목록
     */
    public List<OutboxEvent> findPendingEvents(int limit) {
        QOutboxEvent outboxEvent = QOutboxEvent.outboxEvent;
        return queryFactory
                .selectFrom(outboxEvent)
                .where(outboxEvent.status.eq(OutboxStatus.PENDING))
                .orderBy(outboxEvent.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 실패했지만 재시도 가능한 이벤트 조회
     *
     * 일정 시간(5분)이 지난 후 재시도합니다.
     *
     * @param retryAfter 재시도 기준 시각
     * @param maxRetryCount 최대 재시도 횟수
     * @return 재시도 가능한 이벤트 목록
     */
    public List<OutboxEvent> findFailedEventsForRetry(LocalDateTime retryAfter, int maxRetryCount) {
        QOutboxEvent outboxEvent = QOutboxEvent.outboxEvent;
        return queryFactory
                .selectFrom(outboxEvent)
                .where(
                        outboxEvent.status.eq(OutboxStatus.FAILED),
                        outboxEvent.retryCount.lt(maxRetryCount),
                        outboxEvent.processedAt.before(retryAfter)
                )
                .orderBy(outboxEvent.createdAt.asc())
                .fetch();
    }

    /**
     * 특정 Aggregate의 이벤트 조회
     *
     * @param aggregateId Aggregate ID
     * @return 해당 Aggregate의 이벤트 목록 (생성 시각 순)
     */
    public List<OutboxEvent> findByAggregateId(UUID aggregateId) {
        QOutboxEvent outboxEvent = QOutboxEvent.outboxEvent;
        return queryFactory
                .selectFrom(outboxEvent)
                .where(outboxEvent.aggregateId.eq(aggregateId))
                .orderBy(outboxEvent.createdAt.asc())
                .fetch();
    }

    /**
     * 발행 완료된 오래된 이벤트 조회
     *
     * 보관 기간이 지난 이벤트를 정리하기 위해 사용합니다.
     *
     * @param before 기준 시각 이전의 이벤트
     * @return 삭제 대상 이벤트 목록
     */
    public List<OutboxEvent> findPublishedEventsBefore(LocalDateTime before) {
        QOutboxEvent outboxEvent = QOutboxEvent.outboxEvent;
        return queryFactory
                .selectFrom(outboxEvent)
                .where(
                        outboxEvent.status.eq(OutboxStatus.PUBLISHED),
                        outboxEvent.processedAt.before(before)
                )
                .fetch();
    }

    /**
     * 특정 이벤트 타입의 통계 조회 예시
     *
     * @param eventType 이벤트 타입
     * @return 해당 타입의 이벤트 수
     */
    public long countByEventType(String eventType) {
        QOutboxEvent outboxEvent = QOutboxEvent.outboxEvent;
        Long count = queryFactory
                .select(outboxEvent.count())
                .from(outboxEvent)
                .where(outboxEvent.eventType.eq(eventType))
                .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * 상태별 이벤트 수 조회
     *
     * @param status 상태
     * @return 해당 상태의 이벤트 수
     */
    public long countByStatus(OutboxStatus status) {
        QOutboxEvent outboxEvent = QOutboxEvent.outboxEvent;
        Long count = queryFactory
                .select(outboxEvent.count())
                .from(outboxEvent)
                .where(outboxEvent.status.eq(status))
                .fetchOne();
        return count != null ? count : 0L;
    }
}
