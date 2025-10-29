package org.example.deuknetinfrastructure.external.oauth;

import org.example.deuknetdomain.domain.auth.OAuthUserInfo;
import org.example.deuknetinfrastructure.external.oauth.config.OAuthClientProperties;
import org.example.deuknetinfrastructure.external.oauth.dto.OAuthTokenResponse;
import org.example.deuknetinfrastructure.external.oauth.exception.OAuthTokenRetrievalException;
import org.example.deuknetinfrastructure.external.oauth.exception.OAuthUserInfoRetrievalException;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractOAuthClient implements OAuthClient {

    protected final RestTemplate restTemplate;
    protected final OAuthClientProperties properties;

    protected AbstractOAuthClient(RestTemplate restTemplate, OAuthClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public OAuthUserInfo getUserInfo(String authorizationCode) {
        String accessToken = getAccessToken(authorizationCode);
        return fetchUserInfo(accessToken);
    }

    protected String getAccessToken(String authorizationCode) {
        try {
            HttpHeaders headers = createTokenRequestHeaders();
            MultiValueMap<String, String> params = createTokenRequestParams(authorizationCode);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<OAuthTokenResponse> response = restTemplate.postForEntity(
                    properties.getTokenUri(),
                    request,
                    OAuthTokenResponse.class
            );

            if (response.getBody() == null || response.getBody().getAccessToken() == null) {
                throw new OAuthTokenRetrievalException(getProviderName());
            }

            return response.getBody().getAccessToken();
        } catch (RestClientException e) {
            throw new OAuthTokenRetrievalException(getProviderName(), e);
        }
    }

    protected HttpHeaders createTokenRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    protected MultiValueMap<String, String> createTokenRequestParams(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", properties.getClientId());
        params.add("client_secret", properties.getClientSecret());
        params.add("redirect_uri", properties.getRedirectUri());
        params.add("grant_type", "authorization_code");
        return params;
    }

    protected <T> T fetchUserInfoResponse(String accessToken, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<T> response = restTemplate.exchange(
                    properties.getUserInfoUri(),
                    HttpMethod.GET,
                    request,
                    responseType
            );

            if (response.getBody() == null) {
                throw new OAuthUserInfoRetrievalException(getProviderName());
            }

            return response.getBody();
        } catch (RestClientException e) {
            throw new OAuthUserInfoRetrievalException(getProviderName(), e);
        }
    }

    protected abstract OAuthUserInfo fetchUserInfo(String accessToken);

    protected abstract String getProviderName();
}
