package org.example.seedwork.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetinfrastructure.external.messaging.debezium.DebeziumEventHandler;
import org.example.deuknetinfrastructure.external.messaging.handler.CDCEventHandler;
import org.example.seedwork.TestPostgreSQLContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CDC 테스트를 위한 설정
 *
 * 실제 DebeziumEventHandler 대신 TestDebeziumEventHandler를 사용하여
 * CDC 이벤트를 캡처하고 검증할 수 있도록 합니다.
 *
 * 주의: debezium.enabled=true 설정은 테스트 클래스에서 @TestPropertySource로 설정해야 합니다.
 */
@Slf4j
@TestConfiguration
public class DebeziumTestConfig {

    @Bean
    @Primary  // 실제 DebeziumEventHandler를 Override
    public DebeziumEventHandler testDebeziumEventHandler(
            List<CDCEventHandler> CDCEventHandlers,
            ObjectMapper objectMapper
    ) {
        return new TestDebeziumEventHandler(CDCEventHandlers, objectMapper);
    }

    @Bean
    public io.debezium.config.Configuration testDebeziumConfiguration() {
        PostgreSQLContainer<?> postgres = TestPostgreSQLContainer.getInstance();

        return io.debezium.config.Configuration.create()
                // Engine 설정
                .with("name", "test-deuknet-outbox-connector")
                .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
                .with("offset.storage", "org.apache.kafka.connect.storage.MemoryOffsetBackingStore")
                .with("offset.flush.interval.ms", "1000")

                // Database 연결 설정 (Testcontainers 동적 포트 사용)
                .with("database.hostname", postgres.getHost())
                .with("database.port", postgres.getFirstMappedPort())
                .with("database.user", postgres.getUsername())
                .with("database.password", postgres.getPassword())
                .with("database.dbname", postgres.getDatabaseName())
                .with("topic.prefix", "test-deuknet")

                // PostgreSQL 특정 설정
                .with("plugin.name", "pgoutput")
                .with("publication.name", "deuknet_outbox_publication")
                // publication.autocreate.mode 제거: publication은 수동으로 생성됨
                .with("slot.name", "test_deuknet_outbox_slot")

                // 테이블 필터링
                .with("schema.include.list", "public")
                .with("table.include.list", "public.outbox_events")

                // Outbox Event Router 설정
                .with("transforms", "outbox")
                .with("transforms.outbox.type", "io.debezium.transforms.outbox.EventRouter")
                .with("transforms.outbox.table.expand.json.payload", "true")  // JSON 확장 - projection 데이터 직접 접근
                .with("transforms.outbox.table.field.event.id", "id")
                .with("transforms.outbox.table.field.event.key", "aggregateid")
                .with("transforms.outbox.table.field.event.type", "type")
                .with("transforms.outbox.table.field.event.payload", "payload")
                // timestamp 필드 매핑 제거 - INT64 타입 지원 안됨
                .with("transforms.outbox.route.by.field", "aggregatetype")  // aggregatetype으로 라우팅
                .with("transforms.outbox.route.topic.replacement", "outbox.event.${routedByValue}")
                // 이벤트 타입과 aggregateId를 envelope에 추가
                .with("transforms.outbox.table.fields.additional.placement", "type:envelope:eventType,aggregateid:envelope:aggregateId")

                // Snapshot 설정 (초기 로드)
                .with("snapshot.mode", "initial")

                .build();
    }

    @Bean(destroyMethod = "close")
    @Primary
    public DebeziumEngine<ChangeEvent<String, String>> testDebeziumEngine(
            io.debezium.config.Configuration testDebeziumConfiguration,
            DebeziumEventHandler eventHandler
    ) {
        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create(Json.class)
                .using(testDebeziumConfiguration.asProperties())
                .notifying(record -> {
                    try {
                        String key = record.key();
                        String value = record.value();

                        if (value != null) {
                            eventHandler.handleEvent(key, value);
                        }
                    } catch (Exception e) {
                        log.error("Error processing Debezium record in test", e);
                    }
                })
                .using((success, message, error) -> {
                    if (!success) {
                        log.error("Test Debezium engine error: {}", message, error);
                    }
                })
                .build();

        // 별도 스레드에서 실행
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);

        log.info("Test Debezium Embedded Engine started successfully");
        return engine;
    }
}
