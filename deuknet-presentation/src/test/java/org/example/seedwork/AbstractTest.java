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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.UUID;

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
}
