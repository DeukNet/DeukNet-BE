package org.example.deuknetapplication.port.in.user;

import org.example.deuknetapplication.port.in.post.PageResponse;

public interface SearchUsersUseCase {
    PageResponse<UserResponse> searchUsers(String keyword, int page, int size);
}
