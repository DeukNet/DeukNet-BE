package org.example.deuknetapplication.port.in.auth;

import org.example.deuknetdomain.domain.auth.AuthProvider;
import org.example.deuknetdomain.domain.auth.TokenPair;

public interface OAuthCallbackUseCase {
    TokenPair handleCallback(String code, String state, AuthProvider provider);
}
