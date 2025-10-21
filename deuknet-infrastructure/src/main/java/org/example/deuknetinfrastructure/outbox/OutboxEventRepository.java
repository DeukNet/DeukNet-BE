package org.example.deuknetinfrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Outbox 이벤트 저장소
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * 발행 대기 중인 이벤트 조회
     * 오래된 것부터 처리하기 위해 생성 시각 기준 정렬
     *
     * @param limit 조회할 최대 개수
     * @return 발행 대기 중인 이벤트 목록
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PENDING' ORDER BY o.createdAt ASC LIMIT :limit")
    List<OutboxEvent> findPendingEvents(@Param("limit") int limit);

    /**
     * 실패했지만 재시도 가능한 이벤트 조회
     * 일정 시간(5분)이 지난 후 재시도
     *
     * @param retryAfter 재시도 기준 시각
     * @param maxRetryCount 최대 재시도 횟수
     * @return 재시도 가능한 이벤트 목록
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'FAILED' " +
           "AND o.retryCount < :maxRetryCount " +
           "AND o.processedAt < :retryAfter " +
           "ORDER BY o.createdAt ASC")
    List<OutboxEvent> findFailedEventsForRetry(
        @Param("retryAfter") LocalDateTime retryAfter,
        @Param("maxRetryCount") int maxRetryCount
    );

    /**
     * 특정 Aggregate의 이벤트 조회
     *
     * @param aggregateId Aggregate ID
     * @return 해당 Aggregate의 이벤트 목록
     */
    List<OutboxEvent> findByAggregateIdOrderByCreatedAtAsc(UUID aggregateId);

    /**
     * 발행 완료된 오래된 이벤트 삭제를 위한 조회
     * 보관 기간이 지난 이벤트를 정리
     *
     * @param before 기준 시각 이전의 이벤트
     * @return 삭제 대상 이벤트 목록
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PUBLISHED' " +
           "AND o.processedAt < :before")
    List<OutboxEvent> findPublishedEventsBefore(@Param("before") LocalDateTime before);
}
