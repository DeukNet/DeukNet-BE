package org.example.deuknetinfrastructure.external.oauth.exception;

import org.example.deuknetdomain.common.exception.DeukNetException;

/**
 * OAuth Access Token 획득에 실패했을 때 발생하는 예외
 */
public class OAuthTokenRetrievalException extends DeukNetException {

    public OAuthTokenRetrievalException(String provider) {
        super(500, "OAUTH_TOKEN_RETRIEVAL_FAILED",
              "Failed to get access token from " + provider);
    }

    public OAuthTokenRetrievalException(String provider, Throwable cause) {
        super(500, "OAUTH_TOKEN_RETRIEVAL_FAILED",
              "Failed to get access token from " + provider + ": " + cause.getMessage());
    }
}
