package org.example.deuknetdomain.model.command.user;

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

    private User(UUID id, UUID authCredentialId, String username, String displayName, String bio, String avatarUrl) {
        super(id);
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

}
