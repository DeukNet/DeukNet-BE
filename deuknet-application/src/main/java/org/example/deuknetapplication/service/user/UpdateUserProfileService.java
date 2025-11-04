package org.example.deuknetapplication.service.user;

import org.example.deuknetapplication.port.in.user.UpdateUserProfileCommand;
import org.example.deuknetapplication.port.in.user.UpdateUserProfileUseCase;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetdomain.domain.user.User;
import org.example.deuknetdomain.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UpdateUserProfileService implements UpdateUserProfileUseCase {

    private final UserRepository userRepository;

    public UpdateUserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void updateProfile(UUID userId, UpdateUserProfileCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        User updatedUser = user.updateProfile(
                command.getDisplayName(),
                command.getBio(),
                command.getAvatarUrl()
        );

        userRepository.save(updatedUser);
    }
}
