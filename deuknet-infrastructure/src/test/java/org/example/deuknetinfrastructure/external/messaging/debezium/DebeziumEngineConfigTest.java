package org.example.deuknetinfrastructure.external.messaging.debezium;

import io.debezium.config.Configuration;
import org.example.deuknetinfrastructure.data.debezium.DebeziumOffsetJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DebeziumEngineConfig 단위 테스트
 *
 * Debezium Embedded Engine이 올바른 설정으로 초기화되는지 검증합니다.
 * 특히 Outbox Event Router의 additional.placement 설정이 제대로 적용되는지 확인합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DebeziumEngineConfig Unit Test")
class DebeziumEngineConfigTest {

    @Mock
    private DebeziumEventHandler mockEventHandler;

    @Mock
    private DebeziumOffsetJpaRepository mockOffsetRepository;

    private DebeziumProperties properties;
    private DebeziumEngineConfig debeziumEngineConfig;

    @BeforeEach
    void setUp() {
        // Given: DebeziumProperties 설정
        properties = new DebeziumProperties();
        properties.setConnectorName("test-connector");

        DebeziumProperties.Database database = new DebeziumProperties.Database();
        database.setHostname("localhost");
        database.setPort(5432);
        database.setName("testdb");
        database.setUsername("testuser");
        database.setPassword("testpass");
        database.setServerName("testserver");
        database.setSchemaIncludeList("public");
        database.setTableIncludeList("public.outbox_events");

        properties.setDatabase(database);

        debeziumEngineConfig = new DebeziumEngineConfig(properties, mockEventHandler, mockOffsetRepository);
    }

    @Test
    @DisplayName("Debezium Configuration이 올바른 connector 설정을 포함한다")
    void shouldConfigureConnectorCorrectly() {
        // When: Configuration 생성
        Configuration config = debeziumEngineConfig.debeziumConfiguration();

        // Then: Connector 기본 설정이 올바르게 적용되어야 함
        assertThat(config.getString("name")).isEqualTo("test-connector");
        assertThat(config.getString("connector.class"))
                .isEqualTo("io.debezium.connector.postgresql.PostgresConnector");
        assertThat(config.getString("offset.storage"))
                .isEqualTo("org.example.deuknetinfrastructure.external.messaging.debezium.DatabaseOffsetBackingStore");
    }

    @Test
    @DisplayName("Debezium Configuration이 올바른 데이터베이스 연결 설정을 포함한다")
    void shouldConfigureDatabaseConnectionCorrectly() {
        // When: Configuration 생성
        Configuration config = debeziumEngineConfig.debeziumConfiguration();

        // Then: 데이터베이스 연결 설정이 올바르게 적용되어야 함
        assertThat(config.getString("database.hostname")).isEqualTo("localhost");
        assertThat(config.getInteger("database.port")).isEqualTo(5432);
        assertThat(config.getString("database.user")).isEqualTo("testuser");
        assertThat(config.getString("database.password")).isEqualTo("testpass");
        assertThat(config.getString("database.dbname")).isEqualTo("testdb");
        assertThat(config.getString("topic.prefix")).isEqualTo("testserver");
    }

    @Test
    @DisplayName("Debezium Configuration이 PostgreSQL 특정 설정을 포함한다")
    void shouldConfigurePostgresqlSettings() {
        // When: Configuration 생성
        Configuration config = debeziumEngineConfig.debeziumConfiguration();

        // Then: PostgreSQL 설정이 올바르게 적용되어야 함
        assertThat(config.getString("plugin.name")).isEqualTo("pgoutput");
        assertThat(config.getString("publication.name")).isEqualTo("deuknet_outbox_publication");
        assertThat(config.getString("slot.name")).isEqualTo("deuknet_outbox_slot");
    }

    @Test
    @DisplayName("Debezium Configuration이 테이블 필터링 설정을 포함한다")
    void shouldConfigureTableFiltering() {
        // When: Configuration 생성
        Configuration config = debeziumEngineConfig.debeziumConfiguration();

        // Then: 테이블 필터링 설정이 올바르게 적용되어야 함
        assertThat(config.getString("schema.include.list")).isEqualTo("public");
        assertThat(config.getString("table.include.list")).isEqualTo("public.outbox_events");
    }

    @Test
    @DisplayName("Debezium Configuration이 Outbox Event Router를 올바르게 설정한다")
    void shouldConfigureOutboxEventRouter() {
        // When: Configuration 생성
        Configuration config = debeziumEngineConfig.debeziumConfiguration();

        // Then: Outbox Event Router 설정이 올바르게 적용되어야 함
        assertThat(config.getString("transforms")).isEqualTo("outbox");
        assertThat(config.getString("transforms.outbox.type"))
                .isEqualTo("io.debezium.transforms.outbox.EventRouter");
        assertThat(config.getString("transforms.outbox.table.expand.json.payload"))
                .isEqualTo("true");
        assertThat(config.getString("transforms.outbox.table.field.event.id"))
                .isEqualTo("id");
        assertThat(config.getString("transforms.outbox.table.field.event.key"))
                .isEqualTo("aggregateid");
        assertThat(config.getString("transforms.outbox.table.field.event.type"))
                .isEqualTo("type");
        assertThat(config.getString("transforms.outbox.table.field.event.payload"))
                .isEqualTo("payload");
    }

    @Test
    @DisplayName("Outbox Event Router가 eventType과 aggregateId를 envelope에 추가하도록 설정한다")
    void shouldConfigureAdditionalPlacementForEventTypeAndAggregateId() {
        // When: Configuration 생성
        Configuration config = debeziumEngineConfig.debeziumConfiguration();

        // Then: additional.placement 설정이 올바르게 적용되어야 함
        // 이 설정이 있어야 DebeziumEventHandler가 eventType과 aggregateId를 추출할 수 있음
        String additionalPlacement = config.getString("transforms.outbox.table.fields.additional.placement");
        assertThat(additionalPlacement).isNotNull();
        assertThat(additionalPlacement).contains("type:envelope:eventType");
        assertThat(additionalPlacement).contains("aggregateid:envelope:aggregateId");
    }

    @Test
    @DisplayName("Outbox Event Router가 올바른 토픽 라우팅 설정을 포함한다")
    void shouldConfigureTopicRouting() {
        // When: Configuration 생성
        Configuration config = debeziumEngineConfig.debeziumConfiguration();

        // Then: 토픽 라우팅 설정이 올바르게 적용되어야 함
        assertThat(config.getString("transforms.outbox.route.topic.replacement"))
                .isEqualTo("outbox.event.${routedByValue}");
    }

    @Test
    @DisplayName("Debezium Configuration이 snapshot 모드를 initial로 설정한다")
    void shouldConfigureSnapshotMode() {
        // When: Configuration 생성
        Configuration config = debeziumEngineConfig.debeziumConfiguration();

        // Then: snapshot.mode가 initial이어야 함
        assertThat(config.getString("snapshot.mode")).isEqualTo("initial");
    }

    @Test
    @DisplayName("offset flush interval이 1000ms로 설정된다")
    void shouldConfigureOffsetFlushInterval() {
        // When: Configuration 생성
        Configuration config = debeziumEngineConfig.debeziumConfiguration();

        // Then: offset.flush.interval.ms가 1000이어야 함
        assertThat(config.getString("offset.flush.interval.ms")).isEqualTo("1000");
    }

    @Test
    @DisplayName("다른 데이터베이스 설정으로도 Configuration을 생성할 수 있다")
    void shouldSupportDifferentDatabaseConfigurations() {
        // Given: 다른 데이터베이스 설정
        DebeziumProperties.Database database = properties.getDatabase();
        database.setHostname("prod-db.example.com");
        database.setPort(5433);
        database.setName("production");
        database.setUsername("produser");
        database.setPassword("prodpass");
        database.setServerName("prodserver");

        // When: Configuration 생성
        Configuration config = debeziumEngineConfig.debeziumConfiguration();

        // Then: 새로운 설정이 반영되어야 함
        assertThat(config.getString("database.hostname")).isEqualTo("prod-db.example.com");
        assertThat(config.getInteger("database.port")).isEqualTo(5433);
        assertThat(config.getString("database.dbname")).isEqualTo("production");
        assertThat(config.getString("database.user")).isEqualTo("produser");
        assertThat(config.getString("topic.prefix")).isEqualTo("prodserver");
    }
}
