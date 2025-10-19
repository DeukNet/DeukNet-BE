package org.example.deuknetinfrastructure.external.oauth.exception;

import org.example.deuknetdomain.common.exception.DeukNetException;

/**
 * OAuth Provider로부터 사용자 정보 획득에 실패했을 때 발생하는 예외
 */
public class OAuthUserInfoRetrievalException extends DeukNetException {

    public OAuthUserInfoRetrievalException(String provider) {
        super(500, "OAUTH_USER_INFO_RETRIEVAL_FAILED",
              "Failed to get user info from " + provider);
    }
}
