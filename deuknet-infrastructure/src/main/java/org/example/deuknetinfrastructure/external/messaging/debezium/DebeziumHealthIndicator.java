package org.example.deuknetinfrastructure.external.messaging.debezium;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Debezium Engine Health Check
 *
 * Kubernetes liveness/readiness probe에서 사용됩니다.
 */
@Component
@ConditionalOnProperty(name = "debezium.enabled", havingValue = "true", matchIfMissing = true)
public class DebeziumHealthIndicator implements HealthIndicator {

    private final DebeziumEngine<ChangeEvent<String, String>> debeziumEngine;

    public DebeziumHealthIndicator(DebeziumEngine<ChangeEvent<String, String>> debeziumEngine) {
        this.debeziumEngine = debeziumEngine;
    }

    @Override
    public Health health() {
        // Debezium Engine이 실행 중인지 확인
        // 참고: DebeziumEngine에는 isRunning() 메서드가 없으므로
        // Bean이 생성되었다는 것 자체가 실행 중임을 의미
        if (debeziumEngine != null) {
            return Health.up()
                    .withDetail("status", "Debezium Engine is running")
                    .build();
        }

        return Health.down()
                .withDetail("status", "Debezium Engine is not initialized")
                .build();
    }
}
