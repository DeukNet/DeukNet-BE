package org.example.seedwork.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.example.deuknetinfrastructure.external.messaging.debezium.DebeziumEventHandler;
import org.example.deuknetinfrastructure.external.search.adapter.PostSearchAdapter;

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
            PostSearchAdapter postSearchAdapter,
            ObjectMapper objectMapper
    ) {
        super(postSearchAdapter, objectMapper);
    }

    @Override
    public void handleEvent(String key, String value) {
        // 실제 로직 실행 (Elasticsearch 동기화)
        super.handleEvent(key, value);

        // 이벤트 캡처 (테스트 검증용)
        capturedEvents.add(new CapturedEvent(key, value, Instant.now()));
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
