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
        if (!jwtPort.isRefreshToken(refreshToken)) {
            throw new org.example.deuknetdomain.model.command.auth.exception.InvalidRefreshTokenException();
        }
        
        UUID userId = jwtPort.validateToken(refreshToken);
        
        String newAccessToken = jwtPort.generateAccessToken(userId);
        String newRefreshToken = jwtPort.generateRefreshToken(userId);
        
        return new TokenPair(newAccessToken, newRefreshToken);
    }
}
