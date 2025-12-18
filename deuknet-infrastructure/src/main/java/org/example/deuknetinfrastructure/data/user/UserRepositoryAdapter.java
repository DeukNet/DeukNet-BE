package org.example.deuknetinfrastructure.data.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.deuknetapplication.port.out.repository.AuthorInfoEnrichable;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetdomain.domain.post.AuthorType;
import org.example.deuknetdomain.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper mapper;
    private final JPAQueryFactory queryFactory;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository, UserMapper mapper, JPAQueryFactory queryFactory) {
        this.jpaUserRepository = jpaUserRepository;
        this.mapper = mapper;
        this.queryFactory = queryFactory;
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
            // 익명 작성물: authorId를 null로 마스킹
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

    @Override
    public Page<User> findAll(Pageable pageable) {
        QUserEntity user = QUserEntity.userEntity;

        // 전체 카운트 조회
        Long total = queryFactory
                .select(user.count())
                .from(user)
                .fetchOne();

        // 페이징된 결과 조회
        List<UserEntity> entities = queryFactory
                .selectFrom(user)
                .orderBy(user.username.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Domain 객체로 변환
        List<User> users = entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(users, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<User> searchByKeyword(String keyword, Pageable pageable) {
        QUserEntity user = QUserEntity.userEntity;

        // 키워드로 username 또는 displayName 검색
        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        user.username.containsIgnoreCase(keyword)
                                .or(user.displayName.containsIgnoreCase(keyword))
                )
                .fetchOne();

        // 페이징된 결과 조회
        List<UserEntity> entities = queryFactory
                .selectFrom(user)
                .where(
                        user.username.containsIgnoreCase(keyword)
                                .or(user.displayName.containsIgnoreCase(keyword))
                )
                .orderBy(user.username.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Domain 객체로 변환
        List<User> users = entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(users, pageable, total != null ? total : 0L);
    }
}
