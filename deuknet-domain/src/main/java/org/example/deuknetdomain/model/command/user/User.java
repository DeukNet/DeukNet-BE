package org.example.deuknetdomain.model.command.user;

import java.util.UUID;

public class User {

    private final UUID id;
    private final UUID authCredentialId;
    private final String username;
    private final String displayName;
    private final String bio;
    private final String avatarUrl;

    private User(UUID id, UUID authCredentialId, String username, String displayName, String bio, String avatarUrl) {
        this.id = id;
        this.authCredentialId = authCredentialId;
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    public static User create(UUID authCredentialId, String username, String displayName, String bio, String avatarUrl) {
        return new User(UUID.randomUUID(), authCredentialId, username, displayName, bio, avatarUrl);
    }

    public static User restore(UUID id, UUID authCredentialId, String username, String displayName, String bio, String avatarUrl) {
        return new User(id, authCredentialId, username, displayName, bio, avatarUrl);
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuthCredentialId() {
        return authCredentialId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
