package org.example.integration.category;

import org.example.deuknetpresentation.controller.category.dto.*;
import org.example.seedwork.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest extends AbstractTest {

    @Test
    @WithMockUser
    void createCategory() throws Exception {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setName("Tech");
        req.setParentCategoryId(null);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void updateCategory() throws Exception {
        UUID id = create("Original");

        UpdateCategoryRequest req = new UpdateCategoryRequest();
        req.setName("Updated");

        mockMvc.perform(put("/api/categories/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deleteCategory() throws Exception {
        UUID id = create("ToDelete");

        mockMvc.perform(delete("/api/categories/" + id))
                .andExpect(status().isOk());
    }

    private UUID create(String name) throws Exception {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setName(name);
        req.setParentCategoryId(null);

        MvcResult r = mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        return UUID.fromString(r.getResponse().getContentAsString().replaceAll("\"", ""));
    }
}
