package org.example.deuknetinfrastructure.persistence.mapper;

import org.example.deuknetdomain.common.vo.Email;
import org.example.deuknetdomain.model.command.auth.AuthCredential;
import org.example.deuknetdomain.model.command.auth.AuthProvider;
import org.example.deuknetinfrastructure.persistence.entity.AuthCredentialEntity;

public class AuthCredentialMapper {
    
    public static AuthCredential toDomain(AuthCredentialEntity entity) {
        if (entity == null) return null;
        
        return AuthCredential.restore(
                entity.getId(),
                entity.getUserId(),
                AuthProvider.valueOf(entity.getAuthProvider()),
                Email.of(entity.getEmail())
        );
    }
    
    public static AuthCredentialEntity toEntity(AuthCredential domain) {
        if (domain == null) return null;
        
        return new AuthCredentialEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getAuthProvider().name(),
                domain.getEmail().getValue()
        );
    }
}
