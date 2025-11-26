package org.example.deuknetpresentation.controller.user;

import org.example.deuknetapplication.port.in.user.GetCurrentUserUseCase;
import org.example.deuknetapplication.port.in.user.GetUserByIdUseCase;
import org.example.deuknetapplication.port.in.user.UpdateUserProfileCommand;
import org.example.deuknetapplication.port.in.user.UpdateUserProfileUseCase;
import org.example.deuknetapplication.port.in.user.UserResponse;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetpresentation.controller.user.dto.UpdateUserProfileRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController implements UserApi {

    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final CurrentUserPort currentUserPort;

    public UserController(UpdateUserProfileUseCase updateUserProfileUseCase,
                          GetCurrentUserUseCase getCurrentUserUseCase,
                          GetUserByIdUseCase getUserByIdUseCase,
                          CurrentUserPort currentUserPort) {
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public UserResponse getCurrentUser() {
        return getCurrentUserUseCase.getCurrentUser();
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        return getUserByIdUseCase.getUserById(userId);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(UpdateUserProfileRequest request) {
        UUID userId = currentUserPort.getCurrentUserId();

        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                request.getDisplayName(),
                request.getBio(),
                request.getAvatarUrl()
        );
        updateUserProfileUseCase.updateProfile(userId, command);
    }
}
