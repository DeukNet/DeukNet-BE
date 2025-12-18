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
    @WithMockUser(authorities = "ADMIN")
    void getAllCategories() throws Exception {
        // Given: Create some test categories
        create("카테고리1");
        create("카테고리2");
        create("카테고리3");

        // When & Then: Get all categories (authentication not required for GET)
        mockMvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAllCategories_withoutAuth() throws Exception {
        // When & Then: Get all categories without authentication should also work
        mockMvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createCategory() throws Exception {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setName("Tech");
        req.setParentCategoryId(null);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateCategory() throws Exception {
        UUID id = create("Original");

        UpdateCategoryRequest req = new UpdateCategoryRequest("Updated description", "https://example.com/thumbnail.jpg");

        mockMvc.perform(put("/api/categories/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteCategory() throws Exception {
        UUID id = create("ToDelete");

        mockMvc.perform(delete("/api/categories/" + id))
                .andExpect(status().isNoContent());
    }

    private UUID create(String name) throws Exception {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setName(name);
        req.setParentCategoryId(null);

        MvcResult r = mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(r.getResponse().getContentAsString().replaceAll("\"", ""));
    }
}
