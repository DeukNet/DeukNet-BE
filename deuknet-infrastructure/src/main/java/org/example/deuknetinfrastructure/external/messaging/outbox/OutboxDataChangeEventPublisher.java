package org.example.deuknetinfrastructure.external.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetdomain.common.seedwork.Projection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * DataChangeEventPublisher 포트의 Outbox 패턴 구현체
 *
 * Application에서 요청한 데이터 변경 이벤트 발행을
 * Transactional Outbox Pattern으로 구현합니다.
 *
 * 구현 방식:
 * 1. 데이터 변경 이벤트를 JSON으로 직렬화
 * 2. Outbox 테이블에 저장 (트랜잭션 보장)
 * 3. 별도 스케줄러(OutboxEventScheduler)가 폴링하여 메시지 브로커에 발행
 *
 * 네이밍:
 * - "Outbox"로 시작: 이 클래스가 Outbox 패턴 구현임을 명시
 * - Infrastructure 레이어에만 존재하므로 기술적 용어 사용 가능
 * - 나중에 CdcDataChangeEventPublisher 등 다른 구현체와 구분 가능
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxDataChangeEventPublisher implements DataChangeEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * 이벤트 발행 (실제로는 Outbox에 저장)
     *
     * @param eventType 이벤트 타입
     * @param aggregateId Aggregate ID
     * @param projection 발행할 Projection 객체 (자동으로 JSON 직렬화), null 가능 (삭제 이벤트 등)
     */
    @Override
    @Transactional
    public void publish(String eventType, UUID aggregateId, Projection projection) {
        try {
            // 1. Projection을 JSON으로 직렬화
            String jsonPayload = projection != null ? objectMapper.writeValueAsString(projection) : null;

            // 2. Projection의 타입 정보 추출
            String payloadType = projection != null ? projection.getClass().getName() : null;

            // 3. OutboxEvent 엔티티 생성
            OutboxEvent outboxEvent = new OutboxEvent(
                UUID.randomUUID(),
                eventType,
                payloadType,
                aggregateId,
                jsonPayload
            );

            // 4. Outbox 테이블에 저장 (트랜잭션과 함께 커밋됨)
            outboxEventRepository.save(outboxEvent);

            log.debug("Event saved to outbox: type={}, payloadType={}, aggregateId={}",
                eventType, payloadType, aggregateId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize projection: type={}, aggregateId={}", eventType, aggregateId, e);
            throw new RuntimeException("Failed to serialize projection to JSON", e);
        }
    }
}
