package org.example.deuknetinfrastructure.external.messaging.debezium;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetinfrastructure.external.messaging.handler.EventHandler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Debezium 이벤트 오케스트레이터
 *
 * 책임:
 * - Outbox 테이블의 CDC 이벤트를 수신
 * - EventType별로 적절한 EventHandler에 위임
 * - Strategy 패턴을 사용하여 이벤트 처리 로직 분리
 */
@Slf4j
@Component
public class DebeziumEventHandler {

    private final List<EventHandler> eventHandlers;
    private final ObjectMapper objectMapper;

    public DebeziumEventHandler(
            List<EventHandler> eventHandlers,
            ObjectMapper objectMapper
    ) {
        this.eventHandlers = eventHandlers;
        this.objectMapper = objectMapper;
    }

    /**
     * Debezium 이벤트 처리 (오케스트레이션)
     *
     * @param key CDC 키 (보통 Primary Key)
     * @param value CDC 값 (변경된 데이터)
     */
    public void handleEvent(String key, String value) {
        try {
            JsonNode root = objectMapper.readTree(value);

            JsonNode envelope = root.get("payload");
            if (envelope == null || envelope.isNull()) {
                log.debug("Empty envelope, skipping. key={}", key);
                return;
            }

            String eventTypeName = envelope.get("eventType").asText();
            String aggregateId = envelope.get("aggregateId").asText();

            // Payload는 이미 JSON 객체로 확장되어 있으므로 다시 문자열로 변환
            JsonNode payloadNode = envelope.get("payload");
            String payloadJson = objectMapper.writeValueAsString(payloadNode);

            // EventType enum으로 변환 (타입 안정성 보장)
            if (!EventType.isValid(eventTypeName)) {
                log.warn("Unknown event type: {}", eventTypeName);
                return;
            }

            EventType eventType = EventType.fromTypeName(eventTypeName);
            log.info("Processing CDC event: type={}, aggregateId={}", eventType, aggregateId);

            // 적절한 EventHandler에 위임 (Strategy 패턴)
            boolean handled = false;
            for (EventHandler handler : eventHandlers) {
                if (handler.canHandle(eventType)) {
                    handler.handle(eventType, aggregateId, payloadJson);
                    handled = true;
                    break;
                }
            }

            if (!handled) {
                log.warn("No handler found for event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Failed to process Debezium event. key={}, value={}", key, value, e);
            // TODO: DLQ(Dead Letter Queue) 또는 재시도 로직 구현
        }
    }
}
