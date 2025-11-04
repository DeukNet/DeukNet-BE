package org.example.deuknetapplication.port.in.user;

import java.util.UUID;

public interface UpdateUserProfileUseCase {
    void updateProfile(UUID userId, UpdateUserProfileCommand command);
}
