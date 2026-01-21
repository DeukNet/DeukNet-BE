package org.example.seedwork;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

@SpringBootTest(classes = DeuknetApplication.class)
@AutoConfigureMockMvc
@Transactional
@Import(TestSecurityConfig.class)
public abstract class AbstractTest {

    protected static final PostgreSQLContainer<?> postgres = TestPostgreSQLContainer.getInstance();
    protected static final GenericContainer<?> elasticsearch = TestElasticsearchContainer.getInstance();

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
        registry.add("spring.elasticsearch.uris",
            () -> "http://" + elasticsearch.getHost() + ":" + elasticsearch.getMappedPort(9200));
        registry.add("spring.data.elasticsearch.repositories.enabled", () -> "true");

        // Debezium 기본 비활성화 (CDC 테스트에서만 활성화)
        registry.add("debezium.enabled", () -> "false");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected DataSource dataSource;

    @BeforeEach
    void setUpTestUser() {
        // 테스트용 User 생성 (ADMIN 권한으로 설정)
        User testUser = User.restore(
                TestSecurityConfig.TEST_USER_ID,
                UUID.randomUUID(),
                "testuser",
                "Test User",
                "Test bio",
                "https://example.com/avatar.jpg",
                org.example.deuknetdomain.domain.user.UserRole.ADMIN,
                true  // canAccessAnonymous
        );
        userRepository.save(testUser);
    }

    /**
     * Debezium Publication 생성 헬퍼 메서드
     *
     * CDC 테스트를 위해 PostgreSQL Publication을 생성합니다.
     * outbox_events 테이블이 생성된 후에 호출해야 합니다.
     */
    protected void createDebeziumPublication() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Publication 생성 (이미 존재하면 무시)
            stmt.execute(
                "DO $$ " +
                "BEGIN " +
                "   IF NOT EXISTS (SELECT 1 FROM pg_publication WHERE pubname = 'deuknet_outbox_publication') THEN " +
                "       CREATE PUBLICATION deuknet_outbox_publication FOR TABLE outbox_events; " +
                "   END IF; " +
                "END $$;"
            );

            System.out.println("✅ PostgreSQL Publication 'deuknet_outbox_publication' created");

        } catch (Exception e) {
            System.err.println("⚠️  Failed to create publication: " + e.getMessage());
            throw new RuntimeException("Failed to create Debezium publication", e);
        }
    }
}
