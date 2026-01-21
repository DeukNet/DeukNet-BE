package org.example.deuknetapplication.service.permission;

import org.example.deuknetapplication.port.in.permission.RequestAnonymousAccessCommand;
import org.example.deuknetapplication.port.in.permission.RequestAnonymousAccessUseCase;
import org.example.deuknetapplication.port.out.repository.AnonymousPermissionPasswordRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.permission.AnonymousPermissionPassword;
import org.example.deuknetdomain.domain.permission.exception.InvalidPermissionPasswordException;
import org.example.deuknetdomain.domain.permission.exception.PermissionPasswordNotFoundException;
import org.example.deuknetdomain.domain.user.User;
import org.example.deuknetdomain.domain.user.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 익명 권한 신청 서비스
 * 책임: 비밀번호 검증 후 사용자에게 익명 접근 권한 부여
 */
@Service
@Transactional
public class RequestAnonymousAccessService implements RequestAnonymousAccessUseCase {

    private static final Logger log = LoggerFactory.getLogger(RequestAnonymousAccessService.class);

    private final AnonymousPermissionPasswordRepository passwordRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;

    public RequestAnonymousAccessService(
            AnonymousPermissionPasswordRepository passwordRepository,
            UserRepository userRepository,
            CurrentUserPort currentUserPort
    ) {
        this.passwordRepository = passwordRepository;
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void requestAnonymousAccess(RequestAnonymousAccessCommand command) {
        // 1. 현재 사용자 조회
        UUID currentUserId = currentUserPort.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 이미 권한이 있는 경우 바로 반환
        if (user.isCanAccessAnonymous()) {
            log.info("[ANONYMOUS_ACCESS_ALREADY_GRANTED] userId={}, username={}",
                    user.getId(), user.getUsername());
            return;
        }

        // 3. 비밀번호 조회
        AnonymousPermissionPassword password = passwordRepository.findFirst()
                .orElseThrow(PermissionPasswordNotFoundException::new);

        // 4. 비밀번호 검증
        if (!password.matches(command.getPassword())) {
            log.warn("[INVALID_PERMISSION_PASSWORD] userId={}, username={}",
                    user.getId(), user.getUsername());
            throw new InvalidPermissionPasswordException();
        }

        // 5. 권한 부여 및 저장
        User updatedUser = user.grantAnonymousAccess();
        userRepository.save(updatedUser);

        log.info("[ANONYMOUS_ACCESS_GRANTED] userId={}, username={}",
                user.getId(), user.getUsername());
    }
}
