package org.example.deuknetinfrastructure.data.command.user;

import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetdomain.model.command.user.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper mapper;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository, UserMapper mapper) {
        this.jpaUserRepository = jpaUserRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        UserEntity savedEntity = jpaUserRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findByAuthCredentialId(UUID authCredentialId) {
        return jpaUserRepository.findByAuthCredentialId(authCredentialId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username)
                .map(mapper::toDomain);
    }
}
