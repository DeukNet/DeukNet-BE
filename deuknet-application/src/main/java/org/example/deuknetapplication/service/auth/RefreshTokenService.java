package org.example.deuknetapplication.service.auth;

import org.example.deuknetapplication.port.in.auth.RefreshTokenUseCase;
import org.example.deuknetapplication.port.out.security.JwtPort;
import org.example.deuknetdomain.domain.auth.exception.InvalidRefreshTokenException;
import org.example.deuknetdomain.domain.auth.TokenPair;
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
        if (!jwtPort.isRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        UUID userId = jwtPort.validateToken(refreshToken);

        // 새로운 access token만 생성하고, refresh token은 재발급하지 않음
        String newAccessToken = jwtPort.generateAccessToken(userId);
        return new TokenPair(newAccessToken, refreshToken);
    }
}
