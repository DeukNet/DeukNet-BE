package org.example.seedwork;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetdomain.domain.user.User;
import org.example.seedwork.security.TestSecurityConfig;
import org.example.deuknetinfrastructure.DeuknetApplication;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@SpringBootTest(classes = DeuknetApplication.class)
@AutoConfigureMockMvc
@Transactional
@Import(TestSecurityConfig.class)
public abstract class AbstractTest {

    protected static final PostgreSQLContainer<?> postgres = TestPostgreSQLContainer.getInstance();
    protected static final ElasticsearchContainer elasticsearch = TestElasticsearchContainer.getInstance();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL 설정
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "false");

        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "10");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "2");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "30000");
        registry.add("spring.datasource.hikari.idle-timeout", () -> "600000");
        registry.add("spring.datasource.hikari.max-lifetime", () -> "1800000");

        // Elasticsearch 설정
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
        registry.add("spring.data.elasticsearch.repositories.enabled", () -> "false");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @BeforeEach
    void setUpTestUser() {
        // 테스트용 User 생성
        User testUser = User.restore(
                TestSecurityConfig.TEST_USER_ID,
                UUID.randomUUID(),
                "testuser",
                "Test User",
                "Test bio",
                "https://example.com/avatar.jpg"
        );
        userRepository.save(testUser);
    }

    /**
     * Elasticsearch CDC 동기화 대기 및 검증 유틸리티
     *
     * Outbox -> Debezium -> Kafka -> Elasticsearch Sink Connector 흐름이
     * 완료될 때까지 대기하며 조건을 검증합니다.
     *
     * @param condition 검증할 조건 (Supplier로 반복 실행 가능)
     * @param timeoutSeconds 최대 대기 시간 (초)
     */
    protected void awaitElasticsearchSync(Runnable condition, int timeoutSeconds) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(timeoutSeconds))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> condition.run());
    }

    /**
     * 기본 10초 타임아웃으로 Elasticsearch 동기화 대기
     */
    protected void awaitElasticsearchSync(Runnable condition) {
        awaitElasticsearchSync(condition, 10);
    }
}
