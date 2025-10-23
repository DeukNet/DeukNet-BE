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

        return jwtPort.createTokenPair(userId);
    }
}
