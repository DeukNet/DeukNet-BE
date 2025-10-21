package org.example.deuknetinfrastructure.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox 이벤트를 폴링하여 메시지 브로커에 발행하는 스케줄러
 *
 * 주기적으로 outbox_events 테이블을 확인하여 PENDING 상태의 이벤트를
 * 메시지 브로커(Kafka, RabbitMQ 등)에 발행합니다.
 *
 * 역할 분리:
 * - OutboxEventPublisherAdapter: Application에서 호출하여 Outbox에 저장
 * - OutboxEventScheduler (이 클래스): 주기적으로 Outbox를 폴링하여 실제 발행
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxEventScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventQueryAdapter outboxEventQueryAdapter;

    // TODO: 실제 메시지 브로커 연동 시 추가
    // private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 5초마다 대기 중인 이벤트를 발행
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventQueryAdapter.findPendingEvents(100);

        if (!pendingEvents.isEmpty()) {
            log.info("Publishing {} pending outbox events", pendingEvents.size());
        }

        for (OutboxEvent event : pendingEvents) {
            try {
                publishEvent(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event: {}", event.getId(), e);
                event.markAsFailed(e.getMessage());
                outboxEventRepository.save(event);
            }
        }
    }

    /**
     * 실패한 이벤트 재시도 (1분마다)
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailedEvents() {
        LocalDateTime retryAfter = LocalDateTime.now().minusMinutes(5);
        List<OutboxEvent> failedEvents =
            outboxEventQueryAdapter.findFailedEventsForRetry(retryAfter, 3);

        if (!failedEvents.isEmpty()) {
            log.info("Retrying {} failed outbox events", failedEvents.size());
        }

        for (OutboxEvent event : failedEvents) {
            try {
                publishEvent(event);
            } catch (Exception e) {
                log.error("Retry failed for outbox event: {}", event.getId(), e);
                event.markAsFailed(e.getMessage());
                outboxEventRepository.save(event);
            }
        }
    }

    /**
     * 발행 완료된 오래된 이벤트 정리 (1시간마다)
     */
    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime before = LocalDateTime.now().minusDays(7);
        List<OutboxEvent> oldEvents = outboxEventQueryAdapter.findPublishedEventsBefore(before);

        if (!oldEvents.isEmpty()) {
            log.info("Cleaning up {} old outbox events", oldEvents.size());
            outboxEventRepository.deleteAll(oldEvents);
        }
    }

    /**
     * 이벤트를 메시지 브로커에 발행
     */
    private void publishEvent(OutboxEvent event) {
        event.markAsProcessing();
        outboxEventRepository.save(event);

        try {
            // TODO: 실제 메시지 브로커 연동
            // kafkaTemplate.send(event.getEventType(), event.getPayload());

            // 현재는 로그만 출력 (개발 단계)
            log.debug("Publishing event: type={}, aggregateId={}",
                event.getEventType(), event.getAggregateId());

            event.markAsPublished();
            outboxEventRepository.save(event);

            log.info("Successfully published outbox event: {}", event.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event to message broker", e);
        }
    }
}
