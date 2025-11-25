package org.example.integration.cdc;

import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.external.search.PostSearchPort;
import org.example.deuknetpresentation.controller.post.dto.CreatePostRequest;
import org.example.deuknetpresentation.controller.post.dto.UpdatePostRequest;
import org.example.seedwork.AbstractDebeziumIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
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
 * PostEventHandler 통합 테스트
 *
 * Post 이벤트(POST_CREATED, POST_UPDATED, POST_PUBLISHED, POST_DELETED)가
 * CDC를 통해 Elasticsearch에 제대로 동기화되는지 검증합니다.
 */
@DisplayName("PostEventHandler Integration Test")
class PostEventHandlerIntegrationTest extends AbstractDebeziumIntegrationTest {

    @Autowired
    private PostSearchPort postSearchPort;

    @BeforeEach
    void setUp() {
        clearCapturedEvents();
    }

    @Test
    @WithMockUser
    @DisplayName("POST_CREATED 이벤트 발행 시 PostDetailProjection이 Elasticsearch에 인덱싱된다")
    void postCreated_shouldIndexToElasticsearch() throws Exception {
        // Given: Post 생성 요청
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test Post Title");
        request.setContent("Test Post Content");
        request.setCategoryIds(List.of());

        // When: Post 생성 API 호출 (POST_CREATED 이벤트 발행)
        MvcResult result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String postIdStr = result.getResponse().getContentAsString().replaceAll("\"", "");
        UUID postId = UUID.fromString(postIdStr);

        // Then: Debezium이 이벤트를 캡처
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .pollInterval(500, java.util.concurrent.TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        assertThat(getCapturedEventCount()).isGreaterThanOrEqualTo(2) // Detail + Count
                );

        // And: Elasticsearch에서 Post를 조회할 수 있어야 함
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .pollInterval(500, java.util.concurrent.TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getTitle()).isEqualTo("Test Post Title");
                    assertThat(found.get().getContent()).isEqualTo("Test Post Content");
                    assertThat(found.get().getStatus()).isEqualTo("DRAFT");
                    assertThat(found.get().getViewCount()).isEqualTo(0L);
                    assertThat(found.get().getLikeCount()).isEqualTo(0L);
                    assertThat(found.get().getDislikeCount()).isEqualTo(0L);
                });
    }

    @Test
    @WithMockUser
    @DisplayName("POST_UPDATED 이벤트 발행 시 Elasticsearch의 Post가 업데이트된다")
    void postUpdated_shouldUpdateElasticsearch() throws Exception {
        // Given: Post 생성
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("Original Title");
        createRequest.setContent("Original Content");
        createRequest.setCategoryIds(List.of());

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String postIdStr = createResult.getResponse().getContentAsString().replaceAll("\"", "");
        UUID postId = UUID.fromString(postIdStr);

        // 초기 인덱싱 대기
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                });

        clearCapturedEvents();

        // When: Post 수정 API 호출 (POST_UPDATED 이벤트 발행)
        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated Content");
        updateRequest.setCategoryIds(List.of());

        mockMvc.perform(put("/api/posts/" + postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNoContent());

        // Then: Debezium이 업데이트 이벤트를 캡처
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(getCapturedEventCount()).isGreaterThanOrEqualTo(2) // Detail + Count
                );

        // And: Elasticsearch에서 업데이트된 내용을 조회할 수 있어야 함
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getTitle()).isEqualTo("Updated Title");
                    assertThat(found.get().getContent()).isEqualTo("Updated Content");
                });
    }

    @Test
    @WithMockUser
    @DisplayName("POST_PUBLISHED 이벤트 발행 시 Elasticsearch의 Post 상태가 PUBLISHED로 변경된다")
    void postPublished_shouldUpdateStatusInElasticsearch() throws Exception {
        // Given: Post 생성
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("Draft Post");
        createRequest.setContent("Draft Content");
        createRequest.setCategoryIds(List.of());

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String postIdStr = createResult.getResponse().getContentAsString().replaceAll("\"", "");
        UUID postId = UUID.fromString(postIdStr);

        // 초기 인덱싱 대기
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getStatus()).isEqualTo("DRAFT");
                });

        clearCapturedEvents();

        // When: Post 발행 API 호출 (POST_PUBLISHED 이벤트 발행)
        mockMvc.perform(post("/api/posts/" + postId + "/publish"))
                .andExpect(status().isNoContent());

        // Then: Debezium이 발행 이벤트를 캡처
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(getCapturedEventCount()).isGreaterThanOrEqualTo(1)
                );

        // And: Elasticsearch에서 상태가 PUBLISHED로 변경되어야 함
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getStatus()).isEqualTo("PUBLISHED");
                });
    }

    @Test
    @WithMockUser
    @DisplayName("POST_DELETED 이벤트 발행 시 Elasticsearch에서 Post가 삭제된다")
    void postDeleted_shouldDeleteFromElasticsearch() throws Exception {
        // Given: Post 생성
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("To Be Deleted");
        createRequest.setContent("This will be deleted");
        createRequest.setCategoryIds(List.of());

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String postIdStr = createResult.getResponse().getContentAsString().replaceAll("\"", "");
        UUID postId = UUID.fromString(postIdStr);

        // 초기 인덱싱 대기
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                });

        clearCapturedEvents();

        // When: Post 삭제 API 호출 (POST_DELETED 이벤트 발행)
        mockMvc.perform(delete("/api/posts/" + postId))
                .andExpect(status().isNoContent());

        // Then: Debezium이 삭제 이벤트를 캡처
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(getCapturedEventCount()).isGreaterThanOrEqualTo(1)
                );

        // And: Elasticsearch에서 Post가 삭제되어야 함
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isEmpty();
                });
    }

    @Test
    @WithMockUser
    @DisplayName("POST_CREATED 이벤트에서 PostCountProjection도 함께 인덱싱된다")
    void postCreated_shouldIndexCountProjection() throws Exception {
        // Given: Post 생성 요청
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Count Test Post");
        request.setContent("Testing count projection");
        request.setCategoryIds(List.of());

        // When: Post 생성 API 호출 (POST_CREATED 이벤트 2개 발행: Detail + Count)
        MvcResult result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String postIdStr = result.getResponse().getContentAsString().replaceAll("\"", "");
        UUID postId = UUID.fromString(postIdStr);

        // Then: Debezium이 2개의 이벤트를 캡처 (PostDetailProjection + PostCountProjection)
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(getCapturedEventCount()).isGreaterThanOrEqualTo(2)
                );

        // And: Elasticsearch에서 카운트 필드들이 0으로 초기화되어야 함
        await().atMost(30, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<PostSearchResponse> found = postSearchPort.findById(postId);
                    assertThat(found).isPresent();
                    assertThat(found.get().getViewCount()).isEqualTo(0L);
                    assertThat(found.get().getLikeCount()).isEqualTo(0L);
                    assertThat(found.get().getDislikeCount()).isEqualTo(0L);
                    assertThat(found.get().getCommentCount()).isEqualTo(0L);
                });
    }
}
