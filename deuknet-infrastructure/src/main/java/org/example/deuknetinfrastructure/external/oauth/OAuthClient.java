package org.example.deuknetinfrastructure.external.oauth;

import org.example.deuknetdomain.model.command.auth.OAuthUserInfo;

public interface OAuthClient {
    OAuthUserInfo getUserInfo(String authorizationCode);
}
