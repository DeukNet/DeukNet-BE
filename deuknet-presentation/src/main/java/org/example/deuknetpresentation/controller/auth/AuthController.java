package org.example.deuknetpresentation.controller.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.example.deuknetapplication.port.in.auth.InitiateOAuthUseCase;
import org.example.deuknetapplication.port.in.auth.OAuthCallbackUseCase;
import org.example.deuknetapplication.port.in.auth.RefreshTokenUseCase;
import org.example.deuknetdomain.domain.auth.AuthProvider;
import org.example.deuknetdomain.domain.auth.TokenPair;
import org.example.deuknetpresentation.controller.auth.dto.RefreshTokenRequest;
import org.example.deuknetpresentation.controller.auth.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final InitiateOAuthUseCase initiateOAuthUseCase;
    private final OAuthCallbackUseCase oAuthCallbackUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @Value("${oauth.frontend-success-url}")
    private String frontendSuccessUrl;

    public AuthController(
            InitiateOAuthUseCase initiateOAuthUseCase,
            OAuthCallbackUseCase oAuthCallbackUseCase,
            RefreshTokenUseCase refreshTokenUseCase
    ) {
        this.initiateOAuthUseCase = initiateOAuthUseCase;
        this.oAuthCallbackUseCase = oAuthCallbackUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @Override
    @GetMapping("/oauth/google")
    public void initiateGoogleOAuth(HttpServletResponse response) throws IOException {
        String authorizationUrl = initiateOAuthUseCase.generateAuthorizationUrl(AuthProvider.GOOGLE);
        response.sendRedirect(authorizationUrl);
    }

    @Override
    @GetMapping("/oauth/callback/google")
    public RedirectView handleGoogleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state
    ) {
        TokenPair tokenPair = oAuthCallbackUseCase.handleCallback(code, state, AuthProvider.GOOGLE);

        String redirectUrl = String.format("%s?accessToken=%s&refreshToken=%s",
                frontendSuccessUrl,
                tokenPair.accessToken(),
                tokenPair.refreshToken()
        );

        return new RedirectView(redirectUrl);
    }

    @Override
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse refresh(@RequestBody RefreshTokenRequest request) {
        TokenPair tokenPair = refreshTokenUseCase.refresh(request.getRefreshToken());
        return TokenResponse.from(tokenPair.accessToken(), tokenPair.refreshToken());
    }
}
