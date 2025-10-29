package org.example.integration.comment;

import org.example.deuknetpresentation.controller.comment.dto.CreateCommentRequest;
import org.example.deuknetpresentation.controller.comment.dto.UpdateCommentRequest;
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

class CommentControllerTest extends AbstractTest {

    @Test
    @WithMockUser
    void createComment() throws Exception {
        UUID postId = createPost();

        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("Test Comment");
        req.setParentCommentId(null);

        MvcResult result = mockMvc.perform(post("/api/posts/" + postId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID commentId = UUID.fromString(result.getResponse().getContentAsString().replaceAll("\"", ""));
    }

    @Test
    @WithMockUser
    void updateComment() throws Exception {
        UUID postId = createPost();
        UUID commentId = createComment(postId);

        UpdateCommentRequest req = new UpdateCommentRequest();
        req.setContent("Updated");

        mockMvc.perform(put("/api/posts/" + postId + "/comments/" + commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteComment() throws Exception {
        UUID postId = createPost();
        UUID commentId = createComment(postId);

        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + commentId))
                .andExpect(status().isNoContent());
    }

    private UUID createPost() throws Exception {
        CreatePostRequest req = new CreatePostRequest();
        req.setTitle("Post");
        req.setContent("Content");
        req.setCategoryIds(List.of(UUID.randomUUID()));

        MvcResult r = mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(r.getResponse().getContentAsString().replaceAll("\"", ""));
    }

    private UUID createComment(UUID postId) throws Exception {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("Comment");
        req.setParentCommentId(null);

        MvcResult r = mockMvc.perform(post("/api/posts/" + postId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(r.getResponse().getContentAsString().replaceAll("\"", ""));
    }
}
