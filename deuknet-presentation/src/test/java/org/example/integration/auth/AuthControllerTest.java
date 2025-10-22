package org.example.integration.auth;

import org.example.deuknetapplication.port.out.external.OAuthPort;
import org.example.deuknetdomain.domain.auth.AuthProvider;
import org.example.deuknetdomain.domain.auth.OAuthUserInfo;
import org.example.deuknetpresentation.controller.auth.dto.*;
import org.example.seedwork.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends AbstractTest {

    @MockBean
    private OAuthPort oAuthPort;

    @Test
    void oauthLogin_Success() throws Exception {
        OAuthUserInfo mockUserInfo = new OAuthUserInfo(
                "test@example.com",
                "Test User",
                "https://example.com/picture.jpg",
                AuthProvider.GOOGLE
        );
        when(oAuthPort.getUserInfo(anyString(), any(AuthProvider.class)))
                .thenReturn(mockUserInfo);

        OAuthLoginRequest req = new OAuthLoginRequest();
        req.setCode("test-code");
        req.setProvider(AuthProvider.GOOGLE);

        mockMvc.perform(post("/api/auth/oauth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void refreshToken_Success() throws Exception {
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

        String loginResponse = mockMvc.perform(post("/api/auth/oauth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = loginResponse.substring(
                loginResponse.indexOf("\"refreshToken\":\"") + 16,
                loginResponse.indexOf("\"", loginResponse.indexOf("\"refreshToken\":\"") + 16)
        );

        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
}
