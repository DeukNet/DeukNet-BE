package org.example.integration.post;

import org.example.deuknetinfrastructure.external.messaging.outbox.OutboxEvent;
import org.example.deuknetinfrastructure.external.messaging.outbox.OutboxEventRepository;
import org.example.deuknetpresentation.controller.post.dto.*;
import org.example.seedwork.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerTest extends AbstractTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    @WithMockUser
    void createPost() throws Exception {
        // Given
        CreatePostRequest req = new CreatePostRequest();
        req.setTitle("Test Post");
        req.setContent("Test Content");
        req.setCategoryIds(List.of(UUID.randomUUID()));

        // When
        MvcResult result = mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID postId = UUID.fromString(result.getResponse().getContentAsString().replaceAll("\"", ""));

        // Then - Outbox 이벤트 생성 검증 (Detail + Count 2개)
        List<OutboxEvent> outboxEvents = outboxEventRepository.findByAggregateId(postId);

        assertThat(outboxEvents).hasSize(2);

        // PostDetailProjection 이벤트 검증
        OutboxEvent detailEvent = outboxEvents.stream()
                .filter(e -> e.getPayloadType().contains("PostDetailProjection"))
                .findFirst()
                .orElseThrow();

        assertThat(detailEvent.getEventType()).isEqualTo("PostCreated");
        assertThat(detailEvent.getAggregateId()).isEqualTo(postId);
        assertThat(detailEvent.getStatus().name()).isEqualTo("PENDING");
        assertThat(detailEvent.getPayload()).contains("\"title\":\"Test Post\"");
        assertThat(detailEvent.getPayload()).contains("\"content\":\"Test Content\"");

        // PostCountProjection 이벤트 검증
        OutboxEvent countEvent = outboxEvents.stream()
                .filter(e -> e.getPayloadType().contains("PostCountProjection"))
                .findFirst()
                .orElseThrow();

        assertThat(countEvent.getEventType()).isEqualTo("PostCreated");
        assertThat(countEvent.getAggregateId()).isEqualTo(postId);
        assertThat(countEvent.getStatus().name()).isEqualTo("PENDING");
        assertThat(countEvent.getPayload()).contains("\"commentCount\":0");
        assertThat(countEvent.getPayload()).contains("\"likeCount\":0");
    }

    @Test
    @WithMockUser
    void updatePost() throws Exception {
        UUID id = create();

        UpdatePostRequest req = new UpdatePostRequest();
        req.setTitle("Updated");
        req.setContent("Updated content");
        req.setCategoryIds(List.of(UUID.randomUUID()));

        mockMvc.perform(put("/api/posts/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void publishPost() throws Exception {
        UUID id = create();

        mockMvc.perform(post("/api/posts/" + id + "/publish"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deletePost() throws Exception {
        UUID id = create();

        mockMvc.perform(delete("/api/posts/" + id))
                .andExpect(status().isNoContent());
    }

    private UUID create() throws Exception {
        CreatePostRequest req = new CreatePostRequest();
        req.setTitle("Test");
        req.setContent("Content");
        req.setCategoryIds(List.of(UUID.randomUUID()));

        MvcResult r = mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(r.getResponse().getContentAsString().replaceAll("\"", ""));
    }
}
