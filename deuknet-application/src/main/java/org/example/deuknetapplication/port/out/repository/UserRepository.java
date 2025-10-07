package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.model.command.user.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findByAuthCredentialId(UUID authCredentialId);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
}
