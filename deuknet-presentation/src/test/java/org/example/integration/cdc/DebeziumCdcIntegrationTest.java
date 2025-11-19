package org.example.integration.cdc;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.deuknetinfrastructure.external.messaging.outbox.OutboxEvent;
import org.example.deuknetinfrastructure.external.messaging.outbox.OutboxEventRepository;
import org.example.deuknetpresentation.controller.post.dto.CreatePostRequest;
import org.example.seedwork.AbstractDebeziumIntegrationTest;
import org.example.seedwork.cdc.TestDebeziumEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Outbox Pattern & CDC 통합 테스트
 *
 * 검증 내용:
 * 1. Post 생성 시 Outbox 테이블에 이벤트 저장
 * 2. Debezium이 CDC를 통해 이벤트를 감지하고 처리
 * 3. DebeziumEventHandler가 이벤트를 올바르게 수신
 *
 * 주의: CDC 테스트는 트랜잭션 커밋이 필요하므로 @Transactional 사용 안 함
 */
class DebeziumCdcIntegrationTest extends AbstractDebeziumIntegrationTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private TestDebeziumEventHandler testEventHandler;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 캡처된 이벤트 초기화
        testEventHandler.clear();
    }

    @Test
    @WithMockUser
    void postCreation_shouldStoreEventsInOutboxTable() throws Exception {
        // Given: Post 생성 요청 준비
        String testTitle = "CDC Test Post";
        String testContent = "This post event should be stored in outbox table";

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle(testTitle);
        request.setContent(testContent);
        request.setCategoryIds(List.of(UUID.randomUUID()));

        // When: Post 생성 API 호출
        MvcResult result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String postId = result.getResponse().getContentAsString().replaceAll("\"", "");

        // Then: Outbox 테이블에 이벤트가 저장되었는지 확인
        List<OutboxEvent> outboxEvents = outboxEventRepository.findByAggregateid(postId);

        assertThat(outboxEvents).isNotEmpty();

        // PostCreated 이벤트 확인 (PostDetailProjection)
        OutboxEvent detailEvent = outboxEvents.stream()
                .filter(e -> e.getPayload().contains("title"))
                .findFirst()
                .orElse(null);

        assertThat(detailEvent).isNotNull();
        assertThat(detailEvent.getType()).isEqualTo("PostCreated");
        assertThat(detailEvent.getAggregateid()).isEqualTo(postId);
        assertThat(detailEvent.getAggregatetype()).isEqualTo("PostDetail");  // Projection 타입
        assertThat(detailEvent.getPayload()).contains(testTitle);
        assertThat(detailEvent.getPayload()).contains(testContent);

        // PostCountProjection 이벤트 확인
        OutboxEvent countEvent = outboxEvents.stream()
                .filter(e -> e.getPayload().contains("viewCount"))
                .findFirst()
                .orElse(null);

        assertThat(countEvent).isNotNull();
        assertThat(countEvent.getType()).isEqualTo("PostCreated");

        outboxEvents.forEach(e -> System.out.println("   - " + e.getType() + " (" + e.getAggregatetype() + ")"));
    }

    @Test
    @WithMockUser
    void postPublication_shouldStorePublishedEventInOutbox() throws Exception {
        // Given: Post 생성
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("Test Post for Publication");
        createRequest.setContent("This will be published");
        createRequest.setCategoryIds(List.of(UUID.randomUUID()));

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String postId = createResult.getResponse().getContentAsString().replaceAll("\"", "");

        // 기존 이벤트 카운트
        int creationEventCount = outboxEventRepository.findByAggregateid(postId).size();

        // When: Post 발행
        mockMvc.perform(post("/api/posts/" + postId + "/publish"))
                .andExpect(status().isNoContent());

        // Then: PostPublished 이벤트가 Outbox에 저장되었는지 확인
        List<OutboxEvent> allEvents = outboxEventRepository.findByAggregateid(postId);

        assertThat(allEvents.size()).isGreaterThan(creationEventCount);

        // PostPublished 이벤트 찾기
        boolean hasPublishedEvent = allEvents.stream()
                .anyMatch(e -> "PostPublished".equals(e.getType()));

        assertThat(hasPublishedEvent).isTrue();

        System.out.println("✅ Post 발행 Outbox 테스트 성공!");
        System.out.println("📝 PostPublished 이벤트가 Outbox에 저장되었습니다.");
    }

    @Test
    @WithMockUser
    void multiplePosts_shouldAllStoreEventsInOutbox() throws Exception {
        // Given: 여러 Post 생성
        int postCount = 3;
        String[] postIds = new String[postCount];

        for (int i = 0; i < postCount; i++) {
            CreatePostRequest request = new CreatePostRequest();
            request.setTitle("Outbox Test Post " + (i + 1));
            request.setContent("Content " + (i + 1));
            request.setCategoryIds(List.of(UUID.randomUUID()));

            MvcResult result = mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            postIds[i] = result.getResponse().getContentAsString().replaceAll("\"", "");
        }

        // Then: 모든 Post의 이벤트가 Outbox에 저장되었는지 확인
        for (String postId : postIds) {
            List<OutboxEvent> events = outboxEventRepository.findByAggregateid(postId);
            assertThat(events).isNotEmpty();
            assertThat(events.stream().anyMatch(e -> "PostCreated".equals(e.getType()))).isTrue();
        }

        System.out.println("✅ 다중 Post Outbox 테스트 성공!");
        System.out.println("📊 " + postCount + "개의 Post 이벤트가 모두 Outbox에 저장되었습니다.");
    }

    @Test
    @WithMockUser
    void outboxEvent_shouldFollowDebeziumStandardFormat() throws Exception {
        // Given & When: Post 생성
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Debezium Standard Format Test");
        request.setContent("Testing outbox event format");
        request.setCategoryIds(List.of(UUID.randomUUID()));

        MvcResult result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String postId = result.getResponse().getContentAsString().replaceAll("\"", "");

        // Then: Debezium 표준 형식 검증
        List<OutboxEvent> events = outboxEventRepository.findByAggregateid(postId);
        OutboxEvent event = events.get(0);

        // Debezium Outbox Event Router 필수 필드 검증
        assertThat(event.getId()).isNotNull();
        assertThat(event.getAggregateid()).isEqualTo(postId);  // aggregate_id
        assertThat(event.getAggregatetype()).isNotNull();       // aggregate_type
        assertThat(event.getType()).isNotNull();                // type (event name)
        assertThat(event.getPayload()).isNotNull();             // payload (JSON)
        assertThat(event.getTimestamp()).isNotNull();           // timestamp (epoch millis)

        System.out.println("✅ Debezium 표준 형식 검증 성공!");
        System.out.println("📋 이벤트 세부 정보:");
        System.out.println("   - ID: " + event.getId());
        System.out.println("   - Aggregate ID: " + event.getAggregateid());
        System.out.println("   - Aggregate Type: " + event.getAggregatetype());
        System.out.println("   - Event Type: " + event.getType());
        System.out.println("   - Timestamp: " + event.getTimestamp());
        System.out.println("   - Payload (첫 100자): " + event.getPayload().substring(0, Math.min(100, event.getPayload().length())));
    }

    // ========== CDC 통합 테스트 (Debezium Embedded Engine) ==========

    @Test
    @WithMockUser
    void postCreation_shouldPublishCdcEventThroughDebezium() throws Exception {
        // Given: Post 생성 요청 준비
        String testTitle = "CDC Integration Test";
        String testContent = "This event should be captured by Debezium";

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle(testTitle);
        request.setContent(testContent);
        request.setCategoryIds(List.of(UUID.randomUUID()));

        // When: Post 생성 API 호출
        MvcResult result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String postId = result.getResponse().getContentAsString().replaceAll("\"", "");

        // Debug: Outbox 테이블 확인
        List<OutboxEvent> outboxEvents = outboxEventRepository.findByAggregateid(postId);
        System.out.println("🔍 Outbox 테이블에 저장된 이벤트 수: " + outboxEvents.size());
        outboxEvents.forEach(e -> {
            System.out.println("   - Type: " + e.getType() + ", AggregateType: " + e.getAggregatetype());
        });

        // Then: Debezium이 CDC 이벤트를 처리할 때까지 대기 (최대 10초)
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<TestDebeziumEventHandler.CapturedEvent> events = testEventHandler.getCapturedEvents();
                    System.out.println("📡 Debezium이 캡처한 이벤트 수: " + events.size());
                    assertThat(events).isNotEmpty();
                });

        // CDC 이벤트 검증
        List<TestDebeziumEventHandler.CapturedEvent> capturedEvents = testEventHandler.getCapturedEvents();

        System.out.println("🔍 캡처된 CDC 이벤트 수: " + capturedEvents.size());
        capturedEvents.forEach(e -> {
            System.out.println("   - Key: " + e.getKey());
            System.out.println("   - Value (첫 200자): " + e.getValue().substring(0, Math.min(200, e.getValue().length())));
        });

        // Outbox Event Router가 변환한 이벤트 검증
        // 변환 후: Kafka Connect JSON 형식으로 payload가 직접 들어옴
        boolean hasPostDetailEvent = capturedEvents.stream()
                .anyMatch(e -> e.getValue().contains("title") && e.getValue().contains(testTitle));

        assertThat(hasPostDetailEvent)
                .withFailMessage("PostDetail 이벤트가 Debezium을 통해 캡처되지 않았습니다")
                .isTrue();

        // PostDetail 이벤트 찾기 (title 필드가 있는 이벤트)
        TestDebeziumEventHandler.CapturedEvent postDetailEvent = capturedEvents.stream()
                .filter(e -> e.getValue().contains("title"))
                .findFirst()
                .orElseThrow();

        // Debezium Outbox Event Router 변환 후 Kafka Connect JSON 형식 검증
        JsonNode eventJson = objectMapper.readTree(postDetailEvent.getValue());

        // Kafka Connect JSON 형식: {schema: {...}, payload: {...}}
        assertThat(eventJson.has("payload")).isTrue();
        JsonNode envelope = eventJson.get("payload");

        // Outbox Event Router가 expand.json.payload=true로 설정되어
        // envelope.payload 안에 실제 Post 데이터가 있고, eventType과 aggregateId가 envelope 레벨에 추가됨
        assertThat(envelope.has("eventType")).isTrue();
        assertThat(envelope.has("aggregateId")).isTrue();
        assertThat(envelope.has("payload")).isTrue();

        JsonNode payload = envelope.get("payload");

        // Payload 안에 실제 Post 데이터 확인
        assertThat(payload.get("id").asText()).isEqualTo(postId);
        assertThat(payload.get("title").asText()).isEqualTo(testTitle);
        assertThat(payload.get("content").asText()).isEqualTo(testContent);

        System.out.println("✅ CDC 통합 테스트 성공!");
        System.out.println("📡 Debezium이 PostgreSQL WAL에서 이벤트를 감지하고 처리했습니다");
        System.out.println("📝 Post ID: " + postId);
    }

    @Test
    @WithMockUser
    void postPublication_shouldPublishCdcEventForPublishedStatus() throws Exception {
        // Given: Post 생성
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("Post for Publication CDC Test");
        createRequest.setContent("This will be published via CDC");
        createRequest.setCategoryIds(List.of(UUID.randomUUID()));

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String postId = createResult.getResponse().getContentAsString().replaceAll("\"", "");

        // 생성 이벤트가 모두 처리될 때까지 대기 (PostDetail + PostCount = 최소 2개)
        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> testEventHandler.getCapturedEvents().size() >= 2);

        // 생성 이벤트 클리어
        testEventHandler.clear();

        // When: Post 발행
        mockMvc.perform(post("/api/posts/" + postId + "/publish"))
                .andExpect(status().isNoContent());

        // Debug: Outbox 테이블 확인
        List<OutboxEvent> publishOutboxEvents = outboxEventRepository.findByAggregateid(postId);
        System.out.println("🔍 Publish 후 Outbox 테이블에 저장된 이벤트 수: " + publishOutboxEvents.size());
        publishOutboxEvents.forEach(e -> {
            System.out.println("   - Type: " + e.getType() + ", AggregateType: " + e.getAggregatetype());
        });

        // Then: PostPublished 이벤트가 CDC를 통해 처리되었는지 확인
        // 해당 Post ID에 대한 PostPublished 이벤트가 도착할 때까지 대기
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<TestDebeziumEventHandler.CapturedEvent> events = testEventHandler.getCapturedEvents();
                    System.out.println("📡 현재 캡처된 이벤트 수: " + events.size());
                    events.forEach(e -> {
                        System.out.println("   - Key: " + e.getKey());
                        System.out.println("   - Value (첫 300자): " + e.getValue().substring(0, Math.min(300, e.getValue().length())));
                    });
                    // 특정 Post ID에 대한 PostPublished 이벤트가 있는지 확인
                    boolean hasPublishedEvent = events.stream()
                            .anyMatch(e -> e.getValue().contains(postId) &&
                                    (e.getValue().contains("PostPublished") || e.getValue().contains("PUBLISHED")));
                    assertThat(hasPublishedEvent)
                            .withFailMessage("PostPublished 이벤트가 도착하지 않았습니다. Post ID: " + postId)
                            .isTrue();
                });

        List<TestDebeziumEventHandler.CapturedEvent> publishEvents = testEventHandler.getCapturedEvents();

        // Debezium Outbox Event Router 변환 후 이벤트 검증
        // 변환 후에는 Kafka Connect JSON 형식으로 payload가 직접 들어옴
        // PostPublished는 status 정보를 담고 있으므로 "PUBLISHED" 또는 "PostPublished"를 확인
        // 해당 Post ID에 대한 이벤트만 필터링하여 테스트 간 격리 보장
        TestDebeziumEventHandler.CapturedEvent publishedEvent = publishEvents.stream()
                .filter(e -> e.getValue().contains(postId) &&
                            (e.getValue().contains("PUBLISHED") ||
                            e.getValue().contains("PostPublished") ||
                            e.getValue().contains("\"eventType\":\"PostPublished\"")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("PostPublished 이벤트를 찾을 수 없습니다. Post ID: " + postId + ", 캡처된 이벤트 수: " + publishEvents.size()));

        JsonNode eventJson = objectMapper.readTree(publishedEvent.getValue());

        // Kafka Connect JSON 형식인지 확인
        if (eventJson.has("payload")) {
            // 변환된 형식
            JsonNode payload = eventJson.get("payload");
            System.out.println("✓ 변환된 이벤트 형식 (Kafka Connect JSON)");
            System.out.println("   - Payload: " + payload.toPrettyString());
        } else if (eventJson.has("after")) {
            // 변환되지 않은 원본 CDC 형식
            JsonNode after = eventJson.get("after");
            System.out.println("✓ 원본 CDC 이벤트 형식");
            assertThat(after.get("type").asText()).isEqualTo("PostPublished");
            assertThat(after.get("aggregateid").asText()).isEqualTo(postId);
        } else {
            throw new AssertionError("알 수 없는 이벤트 형식입니다: " + eventJson.toPrettyString());
        }

        System.out.println("✅ Post 발행 CDC 테스트 성공!");
        System.out.println("📡 PostPublished 이벤트가 Debezium을 통해 처리되었습니다");
    }

    @Test
    @WithMockUser
    void multiplePosts_shouldCaptureAllCdcEvents() throws Exception {
        // Given: 여러 Post 생성
        int postCount = 3;
        String[] postIds = new String[postCount];

        for (int i = 0; i < postCount; i++) {
            CreatePostRequest request = new CreatePostRequest();
            request.setTitle("CDC Multi-Post Test " + (i + 1));
            request.setContent("Content " + (i + 1));
            request.setCategoryIds(List.of(UUID.randomUUID()));

            MvcResult result = mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            postIds[i] = result.getResponse().getContentAsString().replaceAll("\"", "");
        }

        // Then: 모든 Post의 CDC 이벤트가 캡처되었는지 확인
        // 각 Post는 2개의 이벤트 생성 (PostDetailProjection + PostCountProjection)
        int expectedMinEvents = postCount * 2;
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<TestDebeziumEventHandler.CapturedEvent> events = testEventHandler.getCapturedEvents();
                    assertThat(events.size()).isGreaterThanOrEqualTo(expectedMinEvents);
                });

        List<TestDebeziumEventHandler.CapturedEvent> allEvents = testEventHandler.getCapturedEvents();

        // 각 PostId에 대한 이벤트가 있는지 확인
        for (String postId : postIds) {
            final String currentPostId = postId;
            boolean hasEventForPost = allEvents.stream()
                    .anyMatch(e -> e.getValue().contains(currentPostId));

            assertThat(hasEventForPost)
                    .withFailMessage("Post ID " + postId + "에 대한 CDC 이벤트가 없습니다")
                    .isTrue();
        }

        System.out.println("✅ 다중 Post CDC 테스트 성공!");
        System.out.println("📊 " + postCount + "개의 Post에 대한 " + allEvents.size() + "개의 CDC 이벤트가 캡처되었습니다");
    }
}
