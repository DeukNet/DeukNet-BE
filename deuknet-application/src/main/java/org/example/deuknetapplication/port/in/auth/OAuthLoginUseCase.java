package org.example.deuknetapplication.port.in.auth;

import org.example.deuknetdomain.model.command.auth.AuthProvider;
import org.example.deuknetdomain.model.command.auth.TokenPair;

public interface OAuthLoginUseCase {
    TokenPair login(String authorizationCode, AuthProvider provider);
}
