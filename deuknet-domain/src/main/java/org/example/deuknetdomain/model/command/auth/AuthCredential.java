package org.example.deuknetdomain.model.command.auth;

import java.util.UUID;
import org.example.deuknetdomain.common.vo.Email;

public class AuthCredential {

    private final UUID id;
    private final UUID userId;
    private final AuthProvider authProvider;
    private final Email email;

    private AuthCredential(UUID id, UUID userId, AuthProvider authProvider, Email email) {
        this.id = id;
        this.userId = userId;
        this.authProvider = authProvider;
        this.email = email;
    }

    public static AuthCredential create(UUID userId, AuthProvider authProvider, Email email) {
        return new AuthCredential(UUID.randomUUID(), userId, authProvider, email);
    }

    public static AuthCredential restore(UUID id, UUID userId, AuthProvider authProvider, Email email) {
        return new AuthCredential(id, userId, authProvider, email);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public Email getEmail() {
        return email;
    }
}
