package org.example.integration.cdc;

import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.external.search.PostSearchPort;
import org.example.deuknetpresentation.controller.post.dto.CreatePostRequest;
import org.example.deuknetpresentation.controller.reaction.dto.AddReactionRequest;
import org.example.seedwork.AbstractDebeziumIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ReactionEventHandler 통합 테스트
 *
 * Reaction 이벤트(REACTION_ADDED, REACTION_REMOVED)가
 * CDC를 통해 Elasticsearch의 카운트 필드를 올바르게 업데이트하는지 검증합니다.
 */
@DisplayName("ReactionEventHandler Integration Test")
class ReactionCDCEventHandlerIntegrationTest extends AbstractDebeziumIntegrationTest {

    @Autowired
    private PostSearchPort postSearchPort;

    @BeforeEach
    void setUp() {
        clearCapturedEvents();
    }

    @Test
    @WithMockUser
    @DisplayName("REACTION_ADDED(LIKE) 이벤트 발행 시 Elasticsearch의 likeCount가 증가한다")
    void reactionAdded_like_shouldIncreaseLikeCount() throws Exception {
        // Given: Post 생성
        UUID postId = createTestPost("Like Test Post");

        // 초기 인덱싱 대기 및 초기 카운트 확인
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getLikeCount()).isEqualTo(0L);
                });

        clearCapturedEvents();

        // When: Like 추가 API 호출 (REACTION_ADDED 이벤트 발행)
        AddReactionRequest request = new AddReactionRequest("LIKE");
        mockMvc.perform(post("/api/posts/" + postId + "/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then: Debezium이 이벤트를 캡처
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(getCapturedEventCount()).isGreaterThanOrEqualTo(1)
                );

        // And: Elasticsearch에서 likeCount가 1로 증가해야 함
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getLikeCount()).isEqualTo(1L);
                });
    }

    @Test
    @WithMockUser
    @DisplayName("REACTION_ADDED(DISLIKE) 이벤트 발행 시 Elasticsearch의 dislikeCount가 증가한다")
    void reactionAdded_dislike_shouldIncreaseDislikeCount() throws Exception {
        // Given: Post 생성
        UUID postId = createTestPost("Dislike Test Post");

        // 초기 인덱싱 대기
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getDislikeCount()).isEqualTo(0L);
                });

        clearCapturedEvents();

        // When: Dislike 추가 API 호출 (REACTION_ADDED 이벤트 발행)
        AddReactionRequest request = new AddReactionRequest("DISLIKE");
        mockMvc.perform(post("/api/posts/" + postId + "/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then: Debezium이 이벤트를 캡처
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(getCapturedEventCount()).isGreaterThanOrEqualTo(1)
                );

        // And: Elasticsearch에서 dislikeCount가 1로 증가해야 함
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getDislikeCount()).isEqualTo(1L);
                });
    }

    @Test
    @WithMockUser
    @DisplayName("REACTION_ADDED(VIEW) 이벤트 발행 시 Elasticsearch의 viewCount가 증가한다")
    void reactionAdded_view_shouldIncreaseViewCount() throws Exception {
        // Given: Post 생성
        UUID postId = createTestPost("View Test Post");

        // 초기 인덱싱 대기
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getViewCount()).isEqualTo(0L);
                });

        clearCapturedEvents();

        // When: View 추가 API 호출 (REACTION_ADDED 이벤트 발행)
        AddReactionRequest request = new AddReactionRequest("VIEW");
        mockMvc.perform(post("/api/posts/" + postId + "/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then: Debezium이 이벤트를 캡처
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(getCapturedEventCount()).isGreaterThanOrEqualTo(1)
                );

        // And: Elasticsearch에서 viewCount가 1로 증가해야 함
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getViewCount()).isEqualTo(1L);
                });
    }

    @Test
    @WithMockUser
    @DisplayName("REACTION_REMOVED(LIKE) 이벤트 발행 시 Elasticsearch의 likeCount가 감소한다")
    void reactionRemoved_like_shouldDecreaseLikeCount() throws Exception {
        // Given: Post 생성 및 Like 추가
        UUID postId = createTestPost("Remove Like Test Post");

        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                });

        // Like 추가
        AddReactionRequest addRequest = new AddReactionRequest("LIKE");
        MvcResult addResult = mockMvc.perform(post("/api/posts/" + postId + "/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String reactionIdStr = addResult.getResponse().getContentAsString().replaceAll("\"", "");
        UUID reactionId = UUID.fromString(reactionIdStr);

        // likeCount가 1이 될 때까지 대기
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getLikeCount()).isEqualTo(1L);
                });

        clearCapturedEvents();

        // When: Like 제거 API 호출 (REACTION_REMOVED 이벤트 발행)
        mockMvc.perform(delete("/api/posts/" + postId + "/reactions/" + reactionId))
                .andExpect(status().isNoContent());

        // Then: Debezium이 이벤트를 캡처
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(getCapturedEventCount()).isGreaterThanOrEqualTo(1)
                );

        // And: Elasticsearch에서 likeCount가 0으로 감소해야 함
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getLikeCount()).isEqualTo(0L);
                });
    }

    @Test
    @WithMockUser
    @Disabled("Flaky test - CDC timing dependent")
    @DisplayName("여러 Reaction 추가 시 각 카운트가 독립적으로 증가한다")
    void multipleReactions_shouldUpdateCountsIndependently() throws Exception {
        // Given: Post 생성
        UUID postId = createTestPost("Multiple Reactions Test Post");

        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS).pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                });

        clearCapturedEvents();

        // When: Like, Dislike, View 각각 추가
        mockMvc.perform(post("/api/posts/" + postId + "/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddReactionRequest("LIKE"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/posts/" + postId + "/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddReactionRequest("DISLIKE"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/posts/" + postId + "/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddReactionRequest("VIEW"))))
                .andExpect(status().isCreated());

        // Then: 3개의 이벤트가 캡처되어야 함
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS)
                .pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(getCapturedEventCount()).isGreaterThanOrEqualTo(3)
                );

        // And: 각 카운트가 1씩 증가해야 함
        await().atMost(90, java.util.concurrent.TimeUnit.SECONDS)
                .pollInterval(2, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getLikeCount()).isEqualTo(1L);
                    assertThat(found.get().getDislikeCount()).isEqualTo(1L);
                    assertThat(found.get().getViewCount()).isEqualTo(1L);
                });
    }

    /**
     * 테스트용 Post 생성 헬퍼 메서드
     */
    private UUID createTestPost(String title) throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle(title);
        request.setContent("Test Content for Reaction");
        request.setCategoryIds(List.of());

        MvcResult result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String postIdStr = result.getResponse().getContentAsString().replaceAll("\"", "");
        return UUID.fromString(postIdStr);
    }
}
