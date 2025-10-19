package org.example.deuknetinfrastructure.external.oauth.exception;

import org.example.deuknetdomain.common.exception.DeukNetException;

/**
 * OAuth Provider로부터 이메일 획득에 실패했을 때 발생하는 예외
 */
public class OAuthEmailRetrievalException extends DeukNetException {

    public OAuthEmailRetrievalException(String provider) {
        super(500, "OAUTH_EMAIL_RETRIEVAL_FAILED",
              "Failed to get email from " + provider);
    }
}
