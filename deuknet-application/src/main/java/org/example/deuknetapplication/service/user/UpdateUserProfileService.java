package org.example.deuknetapplication.service.user;

import org.example.deuknetapplication.port.in.user.UpdateUserProfileCommand;
import org.example.deuknetapplication.port.in.user.UpdateUserProfileUseCase;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.user.User;
import org.example.deuknetdomain.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 사용자 프로필 업데이트 Service
 * <br>
 * CurrentUserPort를 통해 현재 사용자 ID를 조회합니다.
 */
@Service
@Transactional
public class UpdateUserProfileService implements UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;

    public UpdateUserProfileService(
            UserRepository userRepository,
            CurrentUserPort currentUserPort
    ) {
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void updateProfile(UpdateUserProfileCommand command) {
        // CurrentUserPort로 현재 사용자 ID 조회
        UUID currentUserId = currentUserPort.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(UserNotFoundException::new);

        User updatedUser = user.updateProfile(
                command.getDisplayName(),
                command.getBio(),
                command.getAvatarUrl()
        );

        userRepository.save(updatedUser);
    }
}
