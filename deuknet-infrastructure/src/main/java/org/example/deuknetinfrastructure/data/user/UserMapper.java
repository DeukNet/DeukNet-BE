package org.example.deuknetinfrastructure.data.user;

import org.example.deuknetdomain.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public User toDomain(UserEntity entity) {
        if (entity == null) return null;

        return User.restore(
                entity.getId(),
                entity.getAuthCredentialId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getBio(),
                entity.getAvatarUrl(),
                entity.getRole(),
                entity.isCanAccessAnonymous()
        );
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) return null;

        return new UserEntity(
                domain.getId(),
                domain.getAuthCredentialId(),
                domain.getUsername(),
                domain.getDisplayName(),
                domain.getBio(),
                domain.getAvatarUrl(),
                domain.getRole(),
                domain.isCanAccessAnonymous()
        );
    }
}
