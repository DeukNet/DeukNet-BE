package org.example.deuknetapplication.port.out.search;

import org.example.deuknetapplication.dto.search.UserSearchResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 검색 Port
 */
public interface UserSearchPort {
    Optional<UserSearchResponse> findById(UUID id);
    Optional<UserSearchResponse> findByUsername(String username);
    List<UserSearchResponse> searchByDisplayName(String displayName, int page, int size);
    List<UserSearchResponse> searchByKeyword(String keyword, int page, int size);
    List<UserSearchResponse> findActiveUsersByPostCount(int page, int size);
    List<UserSearchResponse> findPopularUsers(int page, int size);
    List<UserSearchResponse> findRecentUsers(int page, int size);
    List<UserSearchResponse> searchWithFilters(String keyword, Long minPostCount, Long minFollowerCount, int page, int size, String sortField, String sortOrder);
}
