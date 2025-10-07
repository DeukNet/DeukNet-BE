package org.example.deuknetapplication.port.out.external;

import org.example.deuknetdomain.model.command.auth.AuthProvider;
import org.example.deuknetdomain.model.command.auth.OAuthUserInfo;

public interface OAuthPort {
    OAuthUserInfo getUserInfo(String authorizationCode, AuthProvider provider);
}
