package org.example.integration.post;

import org.example.deuknetpresentation.controller.post.dto.*;
import org.example.seedwork.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerTest extends AbstractTest {

    @Test
    @WithMockUser
    void createPost() throws Exception {
        CreatePostRequest req = new CreatePostRequest();
        req.setTitle("Test Post");
        req.setContent("Test Content");
        req.setCategoryIds(List.of(UUID.randomUUID()));

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
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

    // incrementViewCount() 테스트 제거
    // 조회수는 POST /api/posts/{postId}/reactions (ReactionType.VIEW)로 처리

    @Test
    @WithMockUser
    void getPostById() throws Exception {
        UUID postId = create();

        mockMvc.perform(get("/api/posts/" + postId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getPostById_notFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/posts/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
