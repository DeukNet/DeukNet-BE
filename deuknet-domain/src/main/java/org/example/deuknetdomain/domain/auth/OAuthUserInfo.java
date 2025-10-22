package org.example.deuknetdomain.domain.auth;

public class OAuthUserInfo {
    private final String email;
    private final String name;
    private final String picture;
    private final AuthProvider provider;

    public OAuthUserInfo(String email, String name, String picture, AuthProvider provider) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.provider = provider;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public AuthProvider getProvider() {
        return provider;
    }
}
