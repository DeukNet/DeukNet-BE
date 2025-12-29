package org.example.deuknetapplication.service.user;

import org.example.deuknetapplication.port.in.user.GetCurrentUserUseCase;
import org.example.deuknetapplication.port.in.user.UserResponse;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;

    public GetCurrentUserService(UserRepository userRepository, CurrentUserPort currentUserPort) {
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public UserResponse getCurrentUser() {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponse.from(user);
    }
}
