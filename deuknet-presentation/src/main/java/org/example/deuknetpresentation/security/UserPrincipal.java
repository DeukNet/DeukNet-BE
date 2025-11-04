package org.example.deuknetpresentation.security;

import java.util.UUID;

public class UserPrincipal {
    private final UUID userId;

    public UserPrincipal(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
