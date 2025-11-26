package org.example.deuknetapplication.port.in.user;

import java.util.UUID;

public interface GetUserByIdUseCase {
    UserResponse getUserById(UUID userId);
}
