package org.example.acceptance;

import org.example.deuknetpresentation.controller.comment.dto.CreateCommentRequest;
import org.example.deuknetpresentation.controller.post.dto.*;
import org.example.deuknetpresentation.controller.reaction.dto.AddReactionRequest;
import org.example.seedwork.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostLifecycleTest extends AbstractTest {

    @Test
    @WithMockUser
    void fullLifecycle() throws Exception {
        CreatePostRequest createReq = new CreatePostRequest();
        createReq.setTitle("Post");
        createReq.setContent("Content");
        createReq.setCategoryIds(List.of(UUID.randomUUID()));

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID postId = UUID.fromString(createResult.getResponse().getContentAsString().replaceAll("\"", ""));

        UpdatePostRequest updateReq = new UpdatePostRequest();
        updateReq.setTitle("Updated");
        updateReq.setContent("Updated");
        updateReq.setCategoryIds(List.of(UUID.randomUUID()));

        mockMvc.perform(put("/api/posts/" + postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/posts/" + postId + "/publish"))
                .andExpect(status().isNoContent());

        CreateCommentRequest commentReq = new CreateCommentRequest();
        commentReq.setContent("Comment");
        commentReq.setParentCommentId(null);

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentReq)))
                .andExpect(status().isCreated());

        AddReactionRequest reactionReq = new AddReactionRequest();
        reactionReq.setReactionType("LIKE");

        mockMvc.perform(post("/api/posts/" + postId + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reactionReq)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/posts/" + postId))
                .andExpect(status().isNoContent());
    }
}
