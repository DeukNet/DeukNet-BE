package org.example.deuknetapplication.port.in.user;

import org.example.deuknetapplication.port.in.post.PageResponse;

public interface GetUsersUseCase {
    PageResponse<UserResponse> getUsers(int page, int size);
}
