package org.example.deuknetinfrastructure.data.command.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.deuknetdomain.domain.auth.AuthProvider;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "auth_credentials")
public class AuthCredentialEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    public AuthCredentialEntity() {
    }

    public AuthCredentialEntity(UUID id, UUID userId, AuthProvider authProvider, String email) {
        this.id = id;
        this.userId = userId;
        this.authProvider = authProvider;
        this.email = email;
    }

}
