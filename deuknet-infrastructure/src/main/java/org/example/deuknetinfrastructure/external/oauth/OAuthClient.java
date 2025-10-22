package org.example.deuknetinfrastructure.external.oauth;

import org.example.deuknetdomain.domain.auth.OAuthUserInfo;

public interface OAuthClient {
    OAuthUserInfo getUserInfo(String authorizationCode);
}
