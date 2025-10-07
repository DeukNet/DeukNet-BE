package org.example.deuknetapplication.port.out.security;

import java.util.UUID;

public interface JwtPort {
    String generateAccessToken(UUID userId);
    String generateRefreshToken(UUID userId);
    UUID validateToken(String token);
    boolean isRefreshToken(String token);
}
