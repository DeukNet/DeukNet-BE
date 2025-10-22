package org.example.deuknetpresentation.controller.auth;

import org.example.deuknetapplication.port.in.auth.OAuthLoginUseCase;
import org.example.deuknetapplication.port.in.auth.RefreshTokenUseCase;
import org.example.deuknetdomain.domain.auth.TokenPair;
import org.example.deuknetpresentation.controller.auth.dto.OAuthLoginRequest;
import org.example.deuknetpresentation.controller.auth.dto.RefreshTokenRequest;
import org.example.deuknetpresentation.controller.auth.dto.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final OAuthLoginUseCase oAuthLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(
            OAuthLoginUseCase oAuthLoginUseCase,
            RefreshTokenUseCase refreshTokenUseCase
    ) {
        this.oAuthLoginUseCase = oAuthLoginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @Override
    @PostMapping("/oauth/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse oauthLogin(@RequestBody OAuthLoginRequest request) {
        TokenPair tokenPair = oAuthLoginUseCase.login(request.getCode(), request.getProvider());
        return TokenResponse.from(tokenPair.getAccessToken(), tokenPair.getRefreshToken());
    }

    @Override
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse refresh(@RequestBody RefreshTokenRequest request) {
        TokenPair tokenPair = refreshTokenUseCase.refresh(request.getRefreshToken());
        return TokenResponse.from(tokenPair.getAccessToken(), tokenPair.getRefreshToken());
    }
}
