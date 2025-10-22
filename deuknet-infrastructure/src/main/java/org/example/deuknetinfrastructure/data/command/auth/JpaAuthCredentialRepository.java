package org.example.deuknetinfrastructure.data.command.auth;

import org.example.deuknetdomain.domain.auth.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaAuthCredentialRepository extends JpaRepository<AuthCredentialEntity, UUID> {
    Optional<AuthCredentialEntity> findByEmailAndAuthProvider(String email, AuthProvider authProvider);
}
