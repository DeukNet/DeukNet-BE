package org.example.deuknetapplication.port.out.security;

import org.example.deuknetdomain.domain.auth.TokenPair;

import java.util.UUID;

public interface JwtPort {
    String generateAccessToken(UUID userId);
    String generateRefreshToken(UUID userId);
    UUID validateToken(String token);
    boolean isRefreshToken(String token);
    TokenPair createTokenPair(UUID userId);
}
