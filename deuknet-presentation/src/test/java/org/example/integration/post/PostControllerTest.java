package org.example.integration.post;

import org.example.deuknetinfrastructure.outbox.OutboxEvent;
import org.example.deuknetinfrastructure.outbox.OutboxEventRepository;
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

        // Then - Outbox 이벤트 생성 검증
        List<OutboxEvent> outboxEvents = outboxEventRepository.findByAggregateId(postId);

        assertThat(outboxEvents).hasSize(1);

        OutboxEvent outboxEvent = outboxEvents.get(0);
        assertThat(outboxEvent.getEventType()).isEqualTo("PostCreated");
        assertThat(outboxEvent.getPayloadType()).isEqualTo("org.example.deuknetdomain.model.query.post.PostDetailProjection");
        assertThat(outboxEvent.getAggregateId()).isEqualTo(postId);
        assertThat(outboxEvent.getStatus().name()).isEqualTo("PENDING");
        assertThat(outboxEvent.getPayload()).isNotNull();

        String payload = outboxEvent.getPayload();
        assertThat(payload).contains("\"title\":\"Test Post\"");
        assertThat(payload).contains("\"content\":\"Test Content\"");
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
