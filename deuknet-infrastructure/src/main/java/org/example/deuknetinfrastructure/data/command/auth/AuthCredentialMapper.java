package org.example.deuknetinfrastructure.data.command.auth;

import org.example.deuknetdomain.common.vo.Email;
import org.example.deuknetdomain.domain.auth.AuthCredential;
import org.springframework.stereotype.Component;

@Component
public class AuthCredentialMapper {
    
    public AuthCredential toDomain(AuthCredentialEntity entity) {
        if (entity == null) return null;
        
        return AuthCredential.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getAuthProvider(),
                Email.of(entity.getEmail())
        );
    }
    
    public AuthCredentialEntity toEntity(AuthCredential domain) {
        if (domain == null) return null;
        
        return new AuthCredentialEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getAuthProvider(),
                domain.getEmail().getValue()
        );
    }
}
