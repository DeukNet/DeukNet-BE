package org.example.deuknetapplication.port.in.auth;

import org.example.deuknetdomain.domain.auth.AuthProvider;

public interface InitiateOAuthUseCase {
    String generateAuthorizationUrl(AuthProvider provider);
}
