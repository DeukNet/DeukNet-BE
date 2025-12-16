package org.example.deuknetdomain.domain.auth;

public record OAuthUserInfo(String email, String name, String picture, AuthProvider provider) {
}
