package org.example.integration.post;

import org.example.deuknetpresentation.controller.post.dto.*;
import org.example.deuknetpresentation.controller.reaction.dto.AddReactionRequest;
import org.example.seedwork.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReactionControllerTest extends AbstractTest {

    @Test
    @WithMockUser
    void addReaction() throws Exception {
        UUID postId = createPost();

        AddReactionRequest req = new AddReactionRequest();
        req.setReactionType("LIKE");

        mockMvc.perform(post("/api/posts/" + postId + "/reactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
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
}
