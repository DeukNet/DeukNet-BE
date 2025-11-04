package org.example.deuknetpresentation.controller.user;

import org.example.deuknetapplication.port.in.user.UpdateUserProfileCommand;
import org.example.deuknetapplication.port.in.user.UpdateUserProfileUseCase;
import org.example.deuknetpresentation.controller.user.dto.UpdateUserProfileRequest;
import org.example.deuknetpresentation.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController implements UserApi {

    private final UpdateUserProfileUseCase updateUserProfileUseCase;

    public UserController(UpdateUserProfileUseCase updateUserProfileUseCase) {
        this.updateUserProfileUseCase = updateUserProfileUseCase;
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(UpdateUserProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID userId = principal.getUserId();

        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                request.getDisplayName(),
                request.getBio(),
                request.getAvatarUrl()
        );
        updateUserProfileUseCase.updateProfile(userId, command);
    }
}
