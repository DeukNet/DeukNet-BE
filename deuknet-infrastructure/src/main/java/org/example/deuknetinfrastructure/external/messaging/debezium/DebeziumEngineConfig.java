package org.example.deuknetinfrastructure.external.messaging.debezium;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Debezium Embedded Engine 설정
 *
 * PostgreSQL의 Outbox 테이블을 CDC로 모니터링합니다.
 *
 * 주의사항:
 * - 반드시 단일 인스턴스로만 실행해야 합니다 (replicas=1)
 * - Scale-out 필요 시 Kafka Connect로 전환하세요
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "debezium.enabled", havingValue = "true")
public class DebeziumEngineConfig {

    private final DebeziumProperties properties;
    private final DebeziumEventHandler eventHandler;

    public DebeziumEngineConfig(
            DebeziumProperties properties,
            DebeziumEventHandler eventHandler
    ) {
        this.properties = properties;
        this.eventHandler = eventHandler;
    }

    @Bean
    public io.debezium.config.Configuration debeziumConfiguration() {
        return io.debezium.config.Configuration.create()
                // Engine 설정
                .with("name", properties.getConnectorName())
                .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
                .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", properties.getOffsetStorageFileName())
                .with("offset.flush.interval.ms", "1000")

                // Database 연결 설정
                .with("database.hostname", properties.getDatabase().getHostname())
                .with("database.port", properties.getDatabase().getPort())
                .with("database.user", properties.getDatabase().getUsername())
                .with("database.password", properties.getDatabase().getPassword())
                .with("database.dbname", properties.getDatabase().getName())
                .with("topic.prefix", properties.getDatabase().getServerName())

                // PostgreSQL 특정 설정
                .with("plugin.name", "pgoutput")
                .with("publication.name", "deuknet_outbox_publication")
                // publication.autocreate.mode 제거: publication은 수동으로 생성됨
                .with("slot.name", "deuknet_outbox_slot")

                // 테이블 필터링
                .with("schema.include.list", properties.getDatabase().getSchemaIncludeList())
                .with("table.include.list", properties.getDatabase().getTableIncludeList())

                // Outbox Event Router 설정
                .with("transforms", "outbox")
                .with("transforms.outbox.type", "io.debezium.transforms.outbox.EventRouter")
                .with("transforms.outbox.table.expand.json.payload", "true")
                .with("transforms.outbox.table.field.event.id", "id")
                .with("transforms.outbox.table.field.event.key", "aggregateid")
                .with("transforms.outbox.table.field.event.type", "type")
                .with("transforms.outbox.table.field.event.payload", "payload")
                .with("transforms.outbox.table.fields.additional.placement", "type:envelope:eventType,aggregateid:envelope:aggregateId")
                .with("transforms.outbox.route.topic.replacement", "outbox.event.${routedByValue}")

                // Snapshot 설정 (초기 로드)
                .with("snapshot.mode", "initial")

                .build();
    }

    @Bean(destroyMethod = "close")
    public DebeziumEngine<ChangeEvent<String, String>> debeziumEngine(
            io.debezium.config.Configuration debeziumConfiguration
    ) {
        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create(Json.class)
                .using(debeziumConfiguration.asProperties())
                .notifying(record -> {
                    try {
                        String key = record.key();
                        String value = record.value();

                        if (value != null) {
                            eventHandler.handleEvent(key, value);
                        }
                    } catch (Exception e) {
                        log.error("Error processing Debezium record", e);
                    }
                })
                .using((success, message, error) -> {
                    if (!success) {
                        log.error("Debezium engine error: {}", message, error);
                    }
                })
                .build();

        // 별도 스레드에서 실행
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);

        log.info("Debezium Embedded Engine started successfully");
        return engine;
    }
}
