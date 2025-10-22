package org.example.deuknetinfrastructure.external.oauth;

import org.example.deuknetdomain.domain.auth.AuthProvider;
import org.example.deuknetdomain.domain.auth.OAuthUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class GoogleOAuthClient implements OAuthClient {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String tokenUri = "https://oauth2.googleapis.com/token";
    private final String userInfoUri = "https://www.googleapis.com/oauth2/v2/userinfo";

    public GoogleOAuthClient(
            RestTemplate restTemplate,
            @Value("${oauth.google.client-id}") String clientId,
            @Value("${oauth.google.client-secret}") String clientSecret,
            @Value("${oauth.google.redirect-uri}") String redirectUri
    ) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public OAuthUserInfo getUserInfo(String authorizationCode) {
        String accessToken = getAccessToken(authorizationCode);
        
        return fetchUserInfo(accessToken);
    }

    private String getAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);
        
        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            throw new org.example.deuknetinfrastructure.external.oauth.exception.OAuthTokenRetrievalException("Google");
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
            throw new org.example.deuknetinfrastructure.external.oauth.exception.OAuthUserInfoRetrievalException("Google");
        }
        
        Map<String, Object> userInfo = response.getBody();
        
        return new OAuthUserInfo(
                (String) userInfo.get("email"),
                (String) userInfo.get("name"),
                (String) userInfo.get("picture"),
                AuthProvider.GOOGLE
        );
    }
}
