package org.example.deuknetinfrastructure.data.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.deuknetdomain.domain.user.UserRole;

import java.util.UUID;

@Setter
@Getter
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    public UserEntity() {
    }

    public UserEntity(UUID id, UUID authCredentialId, String username,
                      String displayName, String bio, String avatarUrl, UserRole role) {
        this.id = id;
        this.authCredentialId = authCredentialId;
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.role = role != null ? role : UserRole.USER;
    }
}
