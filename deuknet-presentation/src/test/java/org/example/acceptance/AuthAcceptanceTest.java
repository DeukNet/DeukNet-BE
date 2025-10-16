package org.example.acceptance;

import org.example.deuknetapplication.port.out.external.OAuthPort;
import org.example.deuknetdomain.model.command.auth.AuthProvider;
import org.example.deuknetdomain.model.command.auth.OAuthUserInfo;
import org.example.deuknetpresentation.controller.auth.dto.*;
import org.example.seedwork.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthAcceptanceTest extends AbstractTest {

    @MockBean
    private OAuthPort oAuthPort;

    @Test
    void loginAndRefresh() throws Exception {
        OAuthUserInfo mockUserInfo = new OAuthUserInfo(
                "test@example.com",
                "Test User",
                "https://example.com/picture.jpg",
                AuthProvider.GOOGLE
        );
        when(oAuthPort.getUserInfo(anyString(), any(AuthProvider.class)))
                .thenReturn(mockUserInfo);

        OAuthLoginRequest loginReq = new OAuthLoginRequest();
        loginReq.setCode("test-code");
        loginReq.setProvider(AuthProvider.GOOGLE);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/oauth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String json = loginResult.getResponse().getContentAsString();
        int start = json.indexOf("\"refreshToken\":\"") + 16;
        int end = json.indexOf("\"", start);
        String token = json.substring(start, end);

        RefreshTokenRequest refreshReq = new RefreshTokenRequest();
        refreshReq.setRefreshToken(token);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk());
    }
}
