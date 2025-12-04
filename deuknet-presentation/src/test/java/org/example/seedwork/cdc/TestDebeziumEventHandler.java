package org.example.seedwork.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.example.deuknetinfrastructure.external.messaging.debezium.DebeziumEventHandler;
import org.example.deuknetinfrastructure.external.messaging.handler.CDCEventHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 테스트용 Debezium 이벤트 핸들러
 *
 * CDC 이벤트를 캡처하여 테스트에서 검증할 수 있도록 합니다.
 */
public class TestDebeziumEventHandler extends DebeziumEventHandler {

    private final List<CapturedEvent> capturedEvents = new CopyOnWriteArrayList<>();

    public TestDebeziumEventHandler(
            List<CDCEventHandler> CDCEventHandlers,
            ObjectMapper objectMapper
    ) {
        super(CDCEventHandlers, objectMapper);
    }

    @Override
    public void handleEvent(String key, String value) {
        // 이벤트 캡처 (테스트 검증용) - 먼저 캡처하여 예외 발생 시에도 검증 가능
        capturedEvents.add(new CapturedEvent(key, value, Instant.now()));

        // 실제 로직 실행 (Elasticsearch 동기화)
        // 테스트 환경에서는 Elasticsearch가 없을 수 있으므로 예외 무시
        try {
            super.handleEvent(key, value);
        } catch (Exception e) {
            // 테스트에서는 Elasticsearch 오류는 무시 (CDC 이벤트 처리는 정상)
            // 로그는 남기지 않음 (테스트 출력을 깔끔하게 유지)
        }
    }

    public List<CapturedEvent> getCapturedEvents() {
        return new ArrayList<>(capturedEvents);
    }

    public void clear() {
        capturedEvents.clear();
    }

    /**
     * 캡처된 CDC 이벤트
     */
    @Getter
    public static class CapturedEvent {
        private final String key;
        private final String value;
        private final Instant capturedAt;

        public CapturedEvent(String key, String value, Instant capturedAt) {
            this.key = key;
            this.value = value;
            this.capturedAt = capturedAt;
        }
    }
}
