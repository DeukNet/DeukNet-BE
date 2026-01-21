package org.example.deuknetinfrastructure.data.permission;

import org.example.deuknetdomain.domain.permission.AnonymousPermissionPassword;
import org.springframework.stereotype.Component;

/**
 * Entity-Domain 매퍼
 */
@Component
public class AnonymousPermissionPasswordMapper {

    public AnonymousPermissionPassword toDomain(AnonymousPermissionPasswordEntity entity) {
        if (entity == null) return null;

        return AnonymousPermissionPassword.restore(
                entity.getId(),
                entity.getPassword()
        );
    }

    public AnonymousPermissionPasswordEntity toEntity(AnonymousPermissionPassword domain) {
        if (domain == null) return null;

        return new AnonymousPermissionPasswordEntity(
                domain.getId(),
                domain.getPassword()
        );
    }
}
