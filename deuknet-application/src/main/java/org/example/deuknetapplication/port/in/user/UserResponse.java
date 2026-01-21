package org.example.deuknetapplication.port.in.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.deuknetdomain.domain.user.User;
import org.example.deuknetdomain.domain.user.UserRole;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private UserRole role;
    private boolean canAccessAnonymous;

    /**
     * Domain 객체로부터 Response 생성
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getRole(),
                user.isCanAccessAnonymous()
        );
    }
}
