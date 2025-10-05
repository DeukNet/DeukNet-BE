package org.example.deuknetinfrastructure.persistence.mapper;

import org.example.deuknetdomain.model.command.user.User;
import org.example.deuknetinfrastructure.persistence.entity.UserEntity;

public class UserMapper {
    
    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;
        
        return User.restore(
                entity.getId(),
                entity.getAuthCredentialId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getBio(),
                entity.getAvatarUrl()
        );
    }
    
    public static UserEntity toEntity(User domain) {
        if (domain == null) return null;
        
        return new UserEntity(
                domain.getId(),
                domain.getAuthCredentialId(),
                domain.getUsername(),
                domain.getDisplayName(),
                domain.getBio(),
                domain.getAvatarUrl()
        );
    }
}
