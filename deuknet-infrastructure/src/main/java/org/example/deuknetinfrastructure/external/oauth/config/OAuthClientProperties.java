package org.example.deuknetinfrastructure.external.oauth.config;

public record OAuthClientProperties(String clientId, String clientSecret, String redirectUri, String tokenUri,
                                    String userInfoUri) {
}
