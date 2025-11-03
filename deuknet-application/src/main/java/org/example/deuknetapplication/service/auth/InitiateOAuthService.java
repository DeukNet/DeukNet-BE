package org.example.deuknetapplication.service.auth;

import org.example.deuknetapplication.port.in.auth.InitiateOAuthUseCase;
import org.example.deuknetapplication.port.out.external.OAuthUrlGeneratorPort;
import org.example.deuknetdomain.domain.auth.AuthProvider;
import org.springframework.stereotype.Service;

@Service
public class InitiateOAuthService implements InitiateOAuthUseCase {

    private final OAuthUrlGeneratorPort oAuthUrlGeneratorPort;

    public InitiateOAuthService(OAuthUrlGeneratorPort oAuthUrlGeneratorPort) {
        this.oAuthUrlGeneratorPort = oAuthUrlGeneratorPort;
    }

    @Override
    public String generateAuthorizationUrl(AuthProvider provider) {
        return oAuthUrlGeneratorPort.generateAuthorizationUrl(provider);
    }
}
