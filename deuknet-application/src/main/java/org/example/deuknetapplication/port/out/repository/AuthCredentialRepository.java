package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.common.vo.Email;
import org.example.deuknetdomain.domain.auth.AuthCredential;
import org.example.deuknetdomain.domain.auth.AuthProvider;

import java.util.Optional;
import java.util.UUID;

public interface AuthCredentialRepository {
    AuthCredential save(AuthCredential authCredential);
    Optional<AuthCredential> findByEmailAndProvider(Email email, AuthProvider provider);
    Optional<AuthCredential> findById(UUID id);
}
