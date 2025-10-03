package org.example.deuknetdomain.model.command.user;

import java.util.UUID;
import lombok.Getter;

@Getter
public class User {

    private final UUID id;
    private final UUID authCredentialId;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;

    private User(UUID id, UUID authCredentialId, String username, 
                 String displayName, String bio, String avatarUrl) {
        this.id = id;
        this.authCredentialId = authCredentialId;
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    public static User create(UUID authCredentialId, String username) {
        validateUsername(username);
        return new User(
            UUID.randomUUID(), 
            authCredentialId, 
            username,
            username,  
            "",
            ""
        );
    }

    public static User restore(UUID id, UUID authCredentialId, String username,
                               String displayName, String bio, String avatarUrl) {
        return new User(id, authCredentialId, username, displayName, bio, avatarUrl);
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("Username must be between 3 and 20 characters");
        }
    }

    public void changeUsername(String username) {
        validateUsername(username);
        this.username = username;
    }

    public void updateProfile(String displayName, String bio, String avatarUrl) {
        this.displayName = displayName != null ? displayName : this.displayName;
        this.bio = bio != null ? bio : this.bio;
        this.avatarUrl = avatarUrl != null ? avatarUrl : this.avatarUrl;
    }

    public void updateDisplayName(String displayName) {
        if (displayName != null && !displayName.isBlank()) {
            this.displayName = displayName;
        }
    }

    public void updateBio(String bio) {
        this.bio = bio != null ? bio : "";
    }

    public void updateAvatar(String avatarUrl) {
        this.avatarUrl = avatarUrl != null ? avatarUrl : "";
    }
}
