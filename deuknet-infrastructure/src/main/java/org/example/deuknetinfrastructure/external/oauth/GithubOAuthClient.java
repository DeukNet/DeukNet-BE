package org.example.deuknetinfrastructure.external.oauth;

import org.example.deuknetdomain.model.command.auth.AuthProvider;
import org.example.deuknetdomain.model.command.auth.OAuthUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class GithubOAuthClient implements OAuthClient {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String tokenUri = "https://github.com/login/oauth/access_token";
    private final String userInfoUri = "https://api.github.com/user";

    public GithubOAuthClient(
            RestTemplate restTemplate,
            @Value("${oauth.github.client-id}") String clientId,
            @Value("${oauth.github.client-secret}") String clientSecret,
            @Value("${oauth.github.redirect-uri}") String redirectUri
    ) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public OAuthUserInfo getUserInfo(String authorizationCode) {
        // 1. Get Access Token
        String accessToken = getAccessToken(authorizationCode);
        
        // 2. Get User Info
        return fetchUserInfo(accessToken);
    }

    private String getAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);
        
        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            throw new IllegalStateException("Failed to get access token from Github");
        }
        
        return (String) response.getBody().get("access_token");
    }

    private OAuthUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                request,
                Map.class
        );
        
        if (response.getBody() == null) {
            throw new IllegalStateException("Failed to get user info from Github");
        }
        
        Map<String, Object> userInfo = response.getBody();
        
        // Github doesn't provide email in basic scope, might need to fetch separately
        String email = (String) userInfo.get("email");
        if (email == null || email.isEmpty()) {
            email = fetchPrimaryEmail(accessToken);
        }
        
        return new OAuthUserInfo(
                email,
                (String) userInfo.get("name"),
                (String) userInfo.get("avatar_url"),
                AuthProvider.GITHUB
        );
    }

    private String fetchPrimaryEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        ResponseEntity<Map[]> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                request,
                Map[].class
        );
        
        if (response.getBody() == null || response.getBody().length == 0) {
            throw new IllegalStateException("Failed to get email from Github");
        }
        
        // Find primary email
        for (Map<String, Object> emailInfo : response.getBody()) {
            if (Boolean.TRUE.equals(emailInfo.get("primary"))) {
                return (String) emailInfo.get("email");
            }
        }
        
        // Fallback to first email
        return (String) response.getBody()[0].get("email");
    }
}
