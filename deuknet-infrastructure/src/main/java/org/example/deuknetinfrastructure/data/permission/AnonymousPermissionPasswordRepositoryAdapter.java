package org.example.deuknetinfrastructure.data.permission;

import org.example.deuknetapplication.port.out.repository.AnonymousPermissionPasswordRepository;
import org.example.deuknetdomain.domain.permission.AnonymousPermissionPassword;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 익명 권한 비밀번호 Repository Adapter
 */
@Component
public class AnonymousPermissionPasswordRepositoryAdapter implements AnonymousPermissionPasswordRepository {

    private final JpaAnonymousPermissionPasswordRepository jpaRepository;
    private final AnonymousPermissionPasswordMapper mapper;

    public AnonymousPermissionPasswordRepositoryAdapter(
            JpaAnonymousPermissionPasswordRepository jpaRepository,
            AnonymousPermissionPasswordMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<AnonymousPermissionPassword> findFirst() {
        return jpaRepository.findFirstBy()
                .map(mapper::toDomain);
    }
}
