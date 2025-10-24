package org.example.deuknetinfrastructure.data.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByAuthCredentialId(UUID authCredentialId);
    Optional<UserEntity> findByUsername(String username);
}
