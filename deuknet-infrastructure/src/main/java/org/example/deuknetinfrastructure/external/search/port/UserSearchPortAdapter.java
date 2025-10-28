package org.example.deuknetinfrastructure.external.search.port;

import co.elastic.clients.elasticsearch._types.SortOrder;
import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.UserSearchResponse;
import org.example.deuknetapplication.port.out.search.UserSearchPort;
import org.example.deuknetinfrastructure.external.search.adapter.UserSearchAdapter;
import org.example.deuknetinfrastructure.external.search.document.UserDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 사용자 검색 Port Adapter
 */
@Component
@RequiredArgsConstructor
public class UserSearchPortAdapter implements UserSearchPort {

    private final UserSearchAdapter userSearchAdapter;

    @Override
    public Optional<UserSearchResponse> findById(UUID id) {
        return userSearchAdapter.findById(id).map(this::toResponse);
    }

    @Override
    public Optional<UserSearchResponse> findByUsername(String username) {
        return userSearchAdapter.findByUsername(username).map(this::toResponse);
    }

    @Override
    public List<UserSearchResponse> searchByDisplayName(String displayName, int page, int size) {
        return userSearchAdapter.searchByDisplayName(displayName, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSearchResponse> searchByKeyword(String keyword, int page, int size) {
        return userSearchAdapter.searchByKeyword(keyword, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSearchResponse> findActiveUsersByPostCount(int page, int size) {
        return userSearchAdapter.findActiveUsersByPostCount(page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSearchResponse> findPopularUsers(int page, int size) {
        return userSearchAdapter.findPopularUsers(page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSearchResponse> findRecentUsers(int page, int size) {
        return userSearchAdapter.findRecentUsers(page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSearchResponse> searchWithFilters(String keyword, Long minPostCount, Long minFollowerCount,
                                                      int page, int size, String sortField, String sortOrder) {
        SortOrder order = "asc".equalsIgnoreCase(sortOrder) ? SortOrder.Asc : SortOrder.Desc;
        return userSearchAdapter.searchWithFilters(keyword, minPostCount, minFollowerCount, page, size, sortField, order)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private UserSearchResponse toResponse(UserDocument document) {
        return new UserSearchResponse(
                document.getId(),
                document.getUsername(),
                document.getDisplayName(),
                document.getBio(),
                document.getAvatarUrl(),
                document.getPostCount(),
                document.getCommentCount(),
                document.getFollowerCount(),
                0L, // followingCount is not in UserDocument
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
