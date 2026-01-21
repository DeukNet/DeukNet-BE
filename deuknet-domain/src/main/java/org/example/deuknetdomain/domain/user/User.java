package org.example.deuknetdomain.domain.user;

import com.github.f4b6a3.uuid.UuidCreator;
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
    private final boolean canAccessAnonymous;

    private User(UUID id, UUID authCredentialId, String username, String displayName, String bio, String avatarUrl, UserRole role, boolean canAccessAnonymous) {
        super(id);
        this.authCredentialId = authCredentialId;
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.role = role != null ? role : UserRole.USER;
        this.canAccessAnonymous = canAccessAnonymous;
    }

    public static User create(UUID authCredentialId, String username, String displayName, String bio, String avatarUrl) {
        return new User(UuidCreator.getTimeOrderedEpoch(), authCredentialId, username, displayName, bio, avatarUrl, UserRole.USER, false);
    }

    public static User restore(UUID id, UUID authCredentialId, String username, String displayName, String bio, String avatarUrl, UserRole role, boolean canAccessAnonymous) {
        return new User(id, authCredentialId, username, displayName, bio, avatarUrl, role, canAccessAnonymous);
    }

    public User updateProfile(String displayName, String bio, String avatarUrl) {
        return new User(this.getId(), this.authCredentialId, this.username, displayName, bio, avatarUrl, this.role, this.canAccessAnonymous);
    }

    /**
     * 익명 접근 권한 부여
     *
     * @return 익명 접근 권한이 부여된 새 User 객체
     */
    public User grantAnonymousAccess() {
        return new User(this.getId(), this.authCredentialId, this.username, this.displayName, this.bio, this.avatarUrl, this.role, true);
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

}
