package org.example.deuknetdomain.domain.user;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Entity;

import java.util.UUID;

@Getter
public class User extends Entity {

    private final UUID authCredentialId;
    private final String username;
    private final String displayName;
    private final String bio;
    private final String avatarUrl;
    private final UserRole role;

    private User(UUID id, UUID authCredentialId, String username, String displayName, String bio, String avatarUrl, UserRole role) {
        super(id);
        this.authCredentialId = authCredentialId;
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.role = role != null ? role : UserRole.USER;
    }

    public static User create(UUID authCredentialId, String username, String displayName, String bio, String avatarUrl) {
        return new User(UUID.randomUUID(), authCredentialId, username, displayName, bio, avatarUrl, UserRole.USER);
    }

    public static User restore(UUID id, UUID authCredentialId, String username, String displayName, String bio, String avatarUrl, UserRole role) {
        return new User(id, authCredentialId, username, displayName, bio, avatarUrl, role);
    }

    public User updateProfile(String displayName, String bio, String avatarUrl) {
        return new User(this.getId(), this.authCredentialId, this.username, displayName, bio, avatarUrl, this.role);
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

}
