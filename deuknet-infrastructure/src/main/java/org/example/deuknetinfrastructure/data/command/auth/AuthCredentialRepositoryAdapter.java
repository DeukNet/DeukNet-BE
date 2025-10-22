package org.example.deuknetinfrastructure.data.command.auth;

import org.example.deuknetapplication.port.out.repository.AuthCredentialRepository;
import org.example.deuknetdomain.common.vo.Email;
import org.example.deuknetdomain.domain.auth.AuthCredential;
import org.example.deuknetdomain.domain.auth.AuthProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuthCredentialRepositoryAdapter implements AuthCredentialRepository {

    private final JpaAuthCredentialRepository jpaAuthCredentialRepository;
    private final AuthCredentialMapper mapper;

    public AuthCredentialRepositoryAdapter(
            JpaAuthCredentialRepository jpaAuthCredentialRepository,
            AuthCredentialMapper mapper
    ) {
        this.jpaAuthCredentialRepository = jpaAuthCredentialRepository;
        this.mapper = mapper;
    }

    @Override
    public AuthCredential save(AuthCredential authCredential) {
        AuthCredentialEntity entity = mapper.toEntity(authCredential);
        AuthCredentialEntity savedEntity = jpaAuthCredentialRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AuthCredential> findByEmailAndProvider(Email email, AuthProvider provider) {
        return jpaAuthCredentialRepository
                .findByEmailAndAuthProvider(email.getValue(), provider)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<AuthCredential> findById(UUID id) {
        return jpaAuthCredentialRepository.findById(id)
                .map(mapper::toDomain);
    }
}
