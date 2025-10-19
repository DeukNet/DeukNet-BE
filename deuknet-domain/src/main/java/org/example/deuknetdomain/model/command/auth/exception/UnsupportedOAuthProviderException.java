package org.example.deuknetdomain.model.command.auth.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 지원하지 않는 OAuth provider를 사용하려고 할 때 발생하는 예외
 */
public class UnsupportedOAuthProviderException extends DomainException {

    public UnsupportedOAuthProviderException() {
        super(400, "UNSUPPORTED_OAUTH_PROVIDER", "Unsupported OAuth provider");
    }
}
