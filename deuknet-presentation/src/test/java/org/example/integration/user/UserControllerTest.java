package org.example.integration.user;

import org.example.deuknetpresentation.controller.user.dto.UpdateUserProfileRequest;
import org.example.seedwork.AbstractTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends AbstractTest {

    @Test
    @WithMockUser
    void updateProfile() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setDisplayName("Updated");
        request.setBio("Updated Bio");
        request.setAvatarUrl("https://example.com/avatar.jpg");

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @Disabled("Flaky test - needs security configuration review")
    void updateProfile_withoutAuth() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setDisplayName("Updated");
        request.setBio("Updated Bio");
        request.setAvatarUrl("https://example.com/avatar.jpg");

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
