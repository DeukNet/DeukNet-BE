package org.example.deuknetpresentation.controller.search;

import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.UserSearchResponse;
import org.example.deuknetapplication.service.search.UserSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User 검색 컨트롤러
 *
 * Elasticsearch를 활용한 사용자 검색 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/search/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchService userSearchService;

    /**
     * ID로 사용자 조회
     *
     * GET /api/search/users/{id}
     *
     * @return 200 OK (사용자 존재) / 404 NOT FOUND (사용자 없음)
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserSearchResponse> findById(@PathVariable UUID id) {
        Optional<UserSearchResponse> result = userSearchService.findById(id);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * username으로 사용자 조회
     *
     * GET /api/search/users/by-username/{username}
     *
     * @return 200 OK (사용자 존재) / 404 NOT FOUND (사용자 없음)
     */
    @GetMapping("/by-username/{username}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserSearchResponse> findByUsername(@PathVariable String username) {
        Optional<UserSearchResponse> result = userSearchService.findByUsername(username);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * displayName으로 사용자 검색
     *
     * GET /api/search/users/by-display-name?name=표시이름&page=0&size=20
     *
     * @return 200 OK
     */
    @GetMapping("/by-display-name")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserSearchResponse>> searchByDisplayName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<UserSearchResponse> results = userSearchService.searchByDisplayName(name, page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * 통합 사용자 검색 (모든 필터 AND 조합)
     *
     * GET /api/search/users?keyword=검색어&minPostCount=10&minFollowerCount=100&page=0&size=20&sortField=followerCount&sortOrder=desc
     *
     * 파라미터:
     * - keyword: displayName/username/bio 검색어 (optional)
     * - minPostCount: 최소 게시글 수 필터 (optional)
     * - minFollowerCount: 최소 팔로워 수 필터 (optional)
     * - page: 페이지 번호 (default: 0)
     * - size: 페이지 크기 (default: 20, max: 100)
     * - sortField: 정렬 필드 (createdAt, postCount, followerCount) (default: followerCount)
     * - sortOrder: 정렬 순서 (asc, desc) (default: desc)
     *
     * 예시:
     * - 키워드만: /api/search/users?keyword=John
     * - 활동적인 사용자: /api/search/users?minPostCount=50&sortField=postCount&sortOrder=desc
     * - 인기 사용자: /api/search/users?minFollowerCount=1000&sortField=followerCount&sortOrder=desc
     * - 복합 필터: /api/search/users?keyword=developer&minPostCount=10&minFollowerCount=100
     *
     * @return 200 OK
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long minPostCount,
            @RequestParam(required = false) Long minFollowerCount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "followerCount") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        // size 제한 (너무 큰 값 방지)
        if (size > 100) {
            size = 100;
        }

        List<UserSearchResponse> results = userSearchService.searchWithFilters(
                keyword, minPostCount, minFollowerCount, page, size, sortField, sortOrder
        );
        return ResponseEntity.ok(results);
    }

    /**
     * 활동적인 사용자 조회 (Shortcut)
     *
     * GET /api/search/users/active?page=0&size=20
     *
     * 동일한 결과: /api/search/users?sortField=postCount&sortOrder=desc
     *
     * @return 200 OK
     */
    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserSearchResponse>> findActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (size > 100) {
            size = 100;
        }
        List<UserSearchResponse> results = userSearchService.searchWithFilters(
                null, null, null, page, size, "postCount", "desc"
        );
        return ResponseEntity.ok(results);
    }

    /**
     * 인기 사용자 조회 (Shortcut)
     *
     * GET /api/search/users/popular?page=0&size=20
     *
     * 동일한 결과: /api/search/users?sortField=followerCount&sortOrder=desc
     *
     * @return 200 OK
     */
    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserSearchResponse>> findPopularUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (size > 100) {
            size = 100;
        }
        List<UserSearchResponse> results = userSearchService.searchWithFilters(
                null, null, null, page, size, "followerCount", "desc"
        );
        return ResponseEntity.ok(results);
    }

    /**
     * 최근 가입 사용자 조회 (Shortcut)
     *
     * GET /api/search/users/recent?page=0&size=20
     *
     * 동일한 결과: /api/search/users?sortField=createdAt&sortOrder=desc
     *
     * @return 200 OK
     */
    @GetMapping("/recent")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserSearchResponse>> findRecentUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (size > 100) {
            size = 100;
        }
        List<UserSearchResponse> results = userSearchService.searchWithFilters(
                null, null, null, page, size, "createdAt", "desc"
        );
        return ResponseEntity.ok(results);
    }
}
