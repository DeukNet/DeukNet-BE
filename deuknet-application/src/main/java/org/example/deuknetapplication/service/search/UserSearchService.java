package org.example.deuknetapplication.service.search;

import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.UserSearchResponse;
import org.example.deuknetapplication.port.out.search.UserSearchPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User 검색 서비스
 *
 * Elasticsearch를 활용한 사용자 검색 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class UserSearchService {

    private final UserSearchPort userSearchPort;

    public Optional<UserSearchResponse> findById(UUID id) {
        return userSearchPort.findById(id);
    }

    public Optional<UserSearchResponse> findByUsername(String username) {
        return userSearchPort.findByUsername(username);
    }

    public List<UserSearchResponse> searchByDisplayName(String displayName, int page, int size) {
        return userSearchPort.searchByDisplayName(displayName, page, size);
    }

    public List<UserSearchResponse> searchByKeyword(String keyword, int page, int size) {
        return userSearchPort.searchByKeyword(keyword, page, size);
    }

    public List<UserSearchResponse> findActiveUsersByPostCount(int page, int size) {
        return userSearchPort.findActiveUsersByPostCount(page, size);
    }

    public List<UserSearchResponse> findPopularUsers(int page, int size) {
        return userSearchPort.findPopularUsers(page, size);
    }

    public List<UserSearchResponse> findRecentUsers(int page, int size) {
        return userSearchPort.findRecentUsers(page, size);
    }

    public List<UserSearchResponse> searchWithFilters(
            String keyword,
            Long minPostCount,
            Long minFollowerCount,
            int page,
            int size,
            String sortField,
            String sortOrder
    ) {
        return userSearchPort.searchWithFilters(
                keyword, minPostCount, minFollowerCount, page, size, sortField, sortOrder
        );
    }
}
