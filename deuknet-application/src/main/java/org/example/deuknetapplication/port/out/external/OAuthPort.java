package org.example.deuknetapplication.port.out.external;

import org.example.deuknetdomain.domain.auth.AuthProvider;
import org.example.deuknetdomain.domain.auth.OAuthUserInfo;

public interface OAuthPort {
    OAuthUserInfo getUserInfo(String authorizationCode, AuthProvider provider);
}
