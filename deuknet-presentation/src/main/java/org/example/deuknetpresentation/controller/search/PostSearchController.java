package org.example.deuknetpresentation.controller.search;

import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.PostDetailSearchResponse;
import org.example.deuknetapplication.service.search.PostDetailSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Post 검색 컨트롤러
 *
 * Elasticsearch를 활용한 게시글 검색 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/search/posts")
@RequiredArgsConstructor
public class PostSearchController {

    private final PostDetailSearchService postDetailSearchService;

    /**
     * ID로 게시글 조회
     *
     * GET /api/search/posts/{id}
     *
     * @return 200 OK (게시글 존재) / 404 NOT FOUND (게시글 없음)
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PostDetailSearchResponse> findById(@PathVariable UUID id) {
        Optional<PostDetailSearchResponse> result = postDetailSearchService.findById(id);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 통합 게시글 검색 (모든 필터 AND 조합)
     *
     * GET /api/search/posts?keyword=검색어&authorId=...&categoryId=...&status=PUBLISHED&page=0&size=20&sortField=createdAt&sortOrder=desc
     *
     * 파라미터:
     * - keyword: 제목/내용 검색어 (optional)
     * - authorId: 작성자 필터 (optional)
     * - categoryId: 카테고리 필터 (optional)
     * - status: 상태 필터 (DRAFT, PUBLISHED, DELETED) (optional)
     * - page: 페이지 번호 (default: 0)
     * - size: 페이지 크기 (default: 20, max: 100)
     * - sortField: 정렬 필드 (createdAt, viewCount, likeCount, commentCount) (default: createdAt)
     * - sortOrder: 정렬 순서 (asc, desc) (default: desc)
     *
     * 예시:
     * - 키워드만: /api/search/posts?keyword=Spring
     * - 작성자 필터: /api/search/posts?keyword=Spring&authorId=123e4567-e89b-12d3-a456-426614174000
     * - 복합 필터: /api/search/posts?keyword=Java&categoryId=...&status=PUBLISHED&sortField=viewCount&sortOrder=desc
     * - 인기글: /api/search/posts?status=PUBLISHED&sortField=likeCount&sortOrder=desc
     * - 최신글: /api/search/posts?status=PUBLISHED&sortField=createdAt&sortOrder=desc
     *
     * @return 200 OK
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PostDetailSearchResponse>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        // size 제한 (너무 큰 값 방지)
        if (size > 100) {
            size = 100;
        }

        List<PostDetailSearchResponse> results = postDetailSearchService.searchWithFilters(
                keyword, authorId, categoryId, status, page, size, sortField, sortOrder
        );
        return ResponseEntity.ok(results);
    }

    /**
     * 인기 게시글 조회 (Shortcut)
     *
     * GET /api/search/posts/popular?page=0&size=20
     *
     * 동일한 결과: /api/search/posts?status=PUBLISHED&sortField=likeCount&sortOrder=desc
     *
     * @return 200 OK
     */
    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PostDetailSearchResponse>> findPopularPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (size > 100) {
            size = 100;
        }
        List<PostDetailSearchResponse> results = postDetailSearchService.searchWithFilters(
                null, null, null, "PUBLISHED", page, size, "likeCount", "desc"
        );
        return ResponseEntity.ok(results);
    }

    /**
     * 최신 게시글 조회 (Shortcut)
     *
     * GET /api/search/posts/recent?page=0&size=20
     *
     * 동일한 결과: /api/search/posts?status=PUBLISHED&sortField=createdAt&sortOrder=desc
     *
     * @return 200 OK
     */
    @GetMapping("/recent")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PostDetailSearchResponse>> findRecentPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (size > 100) {
            size = 100;
        }
        List<PostDetailSearchResponse> results = postDetailSearchService.searchWithFilters(
                null, null, null, "PUBLISHED", page, size, "createdAt", "desc"
        );
        return ResponseEntity.ok(results);
    }

    /**
     * 트렌딩 게시글 조회 (조회수 + 좋아요 기준) (Shortcut)
     *
     * GET /api/search/posts/trending?page=0&size=20
     *
     * 동일한 결과: /api/search/posts?status=PUBLISHED&sortField=viewCount&sortOrder=desc
     *
     * @return 200 OK
     */
    @GetMapping("/trending")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PostDetailSearchResponse>> findTrendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (size > 100) {
            size = 100;
        }
        List<PostDetailSearchResponse> results = postDetailSearchService.searchWithFilters(
                null, null, null, "PUBLISHED", page, size, "viewCount", "desc"
        );
        return ResponseEntity.ok(results);
    }
}
