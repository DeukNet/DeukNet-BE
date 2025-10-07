package org.example.deuknetpresentation.controller.auth;

import org.example.deuknetapplication.port.in.auth.OAuthLoginUseCase;
import org.example.deuknetapplication.port.in.auth.RefreshTokenUseCase;
import org.example.deuknetdomain.model.command.auth.TokenPair;
import org.example.deuknetpresentation.controller.auth.dto.OAuthLoginRequest;
import org.example.deuknetpresentation.controller.auth.dto.RefreshTokenRequest;
import org.example.deuknetpresentation.controller.auth.dto.TokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OAuthLoginUseCase oAuthLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(
            OAuthLoginUseCase oAuthLoginUseCase,
            RefreshTokenUseCase refreshTokenUseCase
    ) {
        this.oAuthLoginUseCase = oAuthLoginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @PostMapping("/oauth/login")
    public ResponseEntity<TokenResponse> oauthLogin(@RequestBody OAuthLoginRequest request) {
        TokenPair tokenPair = oAuthLoginUseCase.login(request.getCode(), request.getProvider());
        
        TokenResponse response = new TokenResponse(
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        TokenPair tokenPair = refreshTokenUseCase.refresh(request.getRefreshToken());
        
        TokenResponse response = new TokenResponse(
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken()
        );
        
        return ResponseEntity.ok(response);
    }
}
