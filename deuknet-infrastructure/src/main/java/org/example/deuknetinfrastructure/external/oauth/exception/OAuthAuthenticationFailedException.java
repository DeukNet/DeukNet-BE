package org.example.deuknetinfrastructure.external.oauth.exception;

import org.example.deuknetdomain.common.exception.DeukNetException;

/**
 * OAuth 인증이 실패했을 때 발생하는 예외
 * 외부 OAuth Provider와의 통신 중 발생하는 오류를 처리합니다.
 */
public class OAuthAuthenticationFailedException extends DeukNetException {

    public OAuthAuthenticationFailedException(String provider, String reason) {
        super(500, "OAUTH_AUTHENTICATION_FAILED",
              "OAuth authentication failed for provider: " + provider + ". Reason: " + reason);
    }
}
