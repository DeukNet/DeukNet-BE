package org.example.deuknetinfrastructure.data.user;

import org.example.deuknetapplication.port.out.repository.AuthorInfoEnrichable;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetdomain.domain.post.AuthorType;
import org.example.deuknetdomain.domain.user.User;
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

    @Override
    public void enrichWithUserInfo(AuthorInfoEnrichable response) {
        if (AuthorType.ANONYMOUS.equals(response.getAuthorType())) {
            // Post용: authorId 유지 (프론트엔드에서 isAuthor 체크용)
            response.setAuthorUsername("익명");
            response.setAuthorDisplayName("익명");
        } else if (AuthorType.REAL.equals(response.getAuthorType())) {
            // 실명 작성물은 User 조회
            findById(response.getAuthorId()).ifPresent(user -> {
                response.setAuthorUsername(user.getUsername());
                response.setAuthorDisplayName(user.getDisplayName());
            });
        }
    }

    @Override
    public void enrichWithUserInfoForComment(AuthorInfoEnrichable response) {
        if (AuthorType.ANONYMOUS.equals(response.getAuthorType())) {
            // Comment용: authorId를 null로 설정
            response.setAuthorId(null);
            response.setAuthorUsername("익명");
            response.setAuthorDisplayName("익명");
        } else if (AuthorType.REAL.equals(response.getAuthorType())) {
            // 실명 작성물은 User 조회
            findById(response.getAuthorId()).ifPresent(user -> {
                response.setAuthorUsername(user.getUsername());
                response.setAuthorDisplayName(user.getDisplayName());
            });
        }
    }
}
