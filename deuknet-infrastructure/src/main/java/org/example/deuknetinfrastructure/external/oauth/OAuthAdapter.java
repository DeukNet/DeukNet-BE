package org.example.deuknetinfrastructure.external.oauth;

import org.example.deuknetapplication.port.out.external.OAuthPort;
import org.example.deuknetdomain.model.command.auth.AuthProvider;
import org.example.deuknetdomain.model.command.auth.OAuthUserInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OAuthAdapter implements OAuthPort {

    private final Map<AuthProvider, OAuthClient> oauthClients = new HashMap<>();

    public OAuthAdapter(GoogleOAuthClient googleOAuthClient, GithubOAuthClient githubOAuthClient) {
        oauthClients.put(AuthProvider.GOOGLE, googleOAuthClient);
        oauthClients.put(AuthProvider.GITHUB, githubOAuthClient);
    }

    @Override
    public OAuthUserInfo getUserInfo(String authorizationCode, AuthProvider provider) {
        OAuthClient client = oauthClients.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }
        return client.getUserInfo(authorizationCode);
    }
}
