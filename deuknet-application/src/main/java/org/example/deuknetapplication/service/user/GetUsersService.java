package org.example.deuknetapplication.service.user;

import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.user.GetUsersUseCase;
import org.example.deuknetapplication.port.in.user.UserResponse;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetdomain.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetUsersService implements GetUsersUseCase {

    private final UserRepository userRepository;

    public GetUsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public PageResponse<UserResponse> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponse> responses = userPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                responses,
                userPage.getTotalElements(),
                page,
                size
        );
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getRole()
        );
    }
}
