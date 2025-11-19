package org.example.seedwork;

import org.example.deuknetapplication.port.in.post.CreatePostUseCase;
import org.example.deuknetapplication.port.in.post.DeletePostUseCase;
import org.example.deuknetapplication.port.in.post.PublishPostUseCase;
import org.example.deuknetapplication.port.in.post.UpdatePostUseCase;
import org.example.deuknetinfrastructure.external.messaging.debezium.DebeziumEventHandler;
import org.example.seedwork.cdc.DebeziumTestConfig;
import org.example.seedwork.cdc.TestDebeziumEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * CDC (Change Data Capture) 테스트를 위한 추상 클래스
 *
 * Debezium Embedded Engine을 활성화하고 Testcontainers의 동적 포트를 사용하도록 설정합니다.
 *
 * 주의:
 * - CDC 테스트는 트랜잭션 커밋이 필요하므로 @Transactional 사용 안 함
 * - Outbox 이벤트가 실제로 커밋되어야 Debezium이 감지할 수 있음
 */
@Import(DebeziumTestConfig.class)
@Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
public abstract class AbstractDebeziumIntegrationTest extends AbstractTest {

    @Autowired
    protected CreatePostUseCase createPostUseCase;

    @Autowired
    protected UpdatePostUseCase updatePostUseCase;

    @Autowired
    protected PublishPostUseCase publishPostUseCase;

    @Autowired
    protected DeletePostUseCase deletePostUseCase;

    @Autowired
    private DebeziumEventHandler debeziumEventHandler;

    /**
     * 캡처된 CDC 이벤트 개수 조회
     */
    protected int getCapturedEventCount() {
        if (debeziumEventHandler instanceof TestDebeziumEventHandler testHandler) {
            return testHandler.getCapturedEvents().size();
        }
        return 0;
    }

    /**
     * 캡처된 이벤트 초기화
     */
    protected void clearCapturedEvents() {
        if (debeziumEventHandler instanceof TestDebeziumEventHandler testHandler) {
            testHandler.clear();
        }
    }

    @DynamicPropertySource
    static void configureDebeziumProperties(DynamicPropertyRegistry registry) {
        // Debezium 활성화
        registry.add("debezium.enabled", () -> "true");

        // Debezium 데이터베이스 연결 설정 (Testcontainers 동적 포트 사용)
        // 중요: 고정된 값을 사용하여 모든 CDC 테스트가 같은 Spring Context를 공유하도록 함
        registry.add("debezium.connector-name", () -> "test-deuknet-outbox-connector");
        registry.add("debezium.offset-storage-file-name", () -> "/tmp/test-debezium-offsets.dat");

        // Testcontainers의 동적 포트를 사용 (모든 테스트에서 같은 컨테이너 사용)
        registry.add("debezium.database.hostname", postgres::getHost);
        registry.add("debezium.database.port", postgres::getFirstMappedPort);
        registry.add("debezium.database.name", postgres::getDatabaseName);
        registry.add("debezium.database.username", postgres::getUsername);
        registry.add("debezium.database.password", postgres::getPassword);
        registry.add("debezium.database.server-name", () -> "test-deuknet");
    }
}
