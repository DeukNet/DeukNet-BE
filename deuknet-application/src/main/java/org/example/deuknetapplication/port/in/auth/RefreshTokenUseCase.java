package org.example.deuknetapplication.port.in.auth;

import org.example.deuknetdomain.domain.auth.TokenPair;

public interface RefreshTokenUseCase {
    TokenPair refresh(String refreshToken);
}
