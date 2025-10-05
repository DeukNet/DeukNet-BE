package org.example.deuknetinfrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "auth_credential_id", nullable = false, columnDefinition = "UUID")
    private UUID authCredentialId;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "display_name", length = 100)
    private String displayName;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    public UserEntity() {
    }

    public UserEntity(UUID id, UUID authCredentialId, String username, 
                      String displayName, String bio, String avatarUrl) {
        this.id = id;
        this.authCredentialId = authCredentialId;
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAuthCredentialId() {
        return authCredentialId;
    }

    public void setAuthCredentialId(UUID authCredentialId) {
        this.authCredentialId = authCredentialId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
