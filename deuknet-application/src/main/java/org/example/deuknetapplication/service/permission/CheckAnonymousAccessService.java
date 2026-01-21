package org.example.deuknetapplication.service.permission;

import org.example.deuknetapplication.port.in.permission.CheckAnonymousAccessUseCase;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.user.User;
import org.example.deuknetdomain.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 익명 권한 확인 서비스
 * 책임: 현재 사용자의 익명 접근 권한 확인
 */
@Service
@Transactional(readOnly = true)
public class CheckAnonymousAccessService implements CheckAnonymousAccessUseCase {

    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;

    public CheckAnonymousAccessService(
            UserRepository userRepository,
            CurrentUserPort currentUserPort
    ) {
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public boolean hasAnonymousAccess() {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(UserNotFoundException::new);

        return user.isCanAccessAnonymous();
    }
}
