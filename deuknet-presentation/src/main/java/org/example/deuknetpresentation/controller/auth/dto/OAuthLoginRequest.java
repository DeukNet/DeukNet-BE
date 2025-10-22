package org.example.deuknetpresentation.controller.auth.dto;

import org.example.deuknetdomain.domain.auth.AuthProvider;

public class OAuthLoginRequest {
    private String code;
    private AuthProvider provider;

    public OAuthLoginRequest() {
    }

    public OAuthLoginRequest(String code, AuthProvider provider) {
        this.code = code;
        this.provider = provider;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }
}
