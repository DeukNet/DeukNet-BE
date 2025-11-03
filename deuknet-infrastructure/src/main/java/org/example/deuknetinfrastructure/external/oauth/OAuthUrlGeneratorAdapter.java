package org.example.deuknetinfrastructure.external.oauth;

import org.example.deuknetapplication.port.out.external.OAuthStateManagerPort;
import org.example.deuknetapplication.port.out.external.OAuthUrlGeneratorPort;
import org.example.deuknetdomain.domain.auth.AuthProvider;
import org.example.deuknetdomain.domain.auth.exception.UnsupportedOAuthProviderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuthUrlGeneratorAdapter implements OAuthUrlGeneratorPort {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth.google.scope}")
    private String googleScope;

    private final OAuthStateManagerPort oAuthStateManagerPort;

    public OAuthUrlGeneratorAdapter(OAuthStateManagerPort oAuthStateManagerPort) {
        this.oAuthStateManagerPort = oAuthStateManagerPort;
    }

    @Override
    public String generateAuthorizationUrl(AuthProvider provider) {
        String state = oAuthStateManagerPort.generateState();

        switch (provider) {
            case GOOGLE:
                return generateGoogleAuthUrl(state);
            default:
                throw new UnsupportedOAuthProviderException();
        }
    }

    private String generateGoogleAuthUrl(String state) {
        return UriComponentsBuilder
                .fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", googleScope.replace(",", " "))
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build()
                .toUriString();
    }
}
