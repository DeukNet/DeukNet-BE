package org.example.deuknetapplication.service.auth;

import org.example.deuknetapplication.port.in.auth.RefreshTokenUseCase;
import org.example.deuknetapplication.port.out.security.JwtPort;
import org.example.deuknetdomain.model.command.auth.TokenPair;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    private final JwtPort jwtPort;

    public RefreshTokenService(JwtPort jwtPort) {
        this.jwtPort = jwtPort;
    }

    @Override
    public TokenPair refresh(String refreshToken) {
        // 1. Refresh Token 검증
        if (!jwtPort.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        // 2. UserId 추출
        UUID userId = jwtPort.validateToken(refreshToken);
        
        // 3. 새로운 토큰 쌍 생성
        String newAccessToken = jwtPort.generateAccessToken(userId);
        String newRefreshToken = jwtPort.generateRefreshToken(userId);
        
        return new TokenPair(newAccessToken, newRefreshToken);
    }
}
