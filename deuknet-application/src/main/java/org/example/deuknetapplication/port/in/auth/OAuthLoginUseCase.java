package org.example.deuknetapplication.port.in.auth;

import org.example.deuknetdomain.domain.auth.AuthProvider;
import org.example.deuknetdomain.domain.auth.TokenPair;

public interface OAuthLoginUseCase {
    TokenPair login(String authorizationCode, AuthProvider provider);
}
