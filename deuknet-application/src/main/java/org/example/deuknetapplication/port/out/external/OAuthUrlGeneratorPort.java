package org.example.deuknetapplication.port.out.external;

import org.example.deuknetdomain.domain.auth.AuthProvider;

public interface OAuthUrlGeneratorPort {
    String generateAuthorizationUrl(AuthProvider provider);
}
