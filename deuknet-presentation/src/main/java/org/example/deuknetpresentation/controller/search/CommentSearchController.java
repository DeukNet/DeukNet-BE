package org.example.deuknetpresentation.controller.search;

import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.CommentSearchResponse;
import org.example.deuknetapplication.service.search.CommentSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Comment 검색 컨트롤러
 *
 * Elasticsearch를 활용한 댓글 검색 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/search/comments")
@RequiredArgsConstructor
public class CommentSearchController {

    private final CommentSearchService commentSearchService;

    /**
     * ID로 댓글 조회
     *
     * GET /api/search/comments/{id}
     *
     * @return 200 OK (댓글 존재) / 404 NOT FOUND (댓글 없음)
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CommentSearchResponse> findById(@PathVariable UUID id) {
        Optional<CommentSearchResponse> result = commentSearchService.findById(id);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 게시글별 댓글 조회
     *
     * GET /api/search/comments/by-post/{postId}?page=0&size=20
     *
     * @return 200 OK
     */
    @GetMapping("/by-post/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<CommentSearchResponse>> findByPostId(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<CommentSearchResponse> results = commentSearchService.findByPostId(postId, page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * 게시글의 최상위 댓글만 조회
     *
     * GET /api/search/comments/by-post/{postId}/top-level?page=0&size=20
     */
    @GetMapping("/by-post/{postId}/top-level")
    public ResponseEntity<List<CommentSearchResponse>> findTopLevelCommentsByPostId(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<CommentSearchResponse> results = commentSearchService.findTopLevelCommentsByPostId(postId, page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * 특정 댓글의 대댓글 조회
     *
     * GET /api/search/comments/{parentCommentId}/replies?page=0&size=20
     */
    @GetMapping("/{parentCommentId}/replies")
    public ResponseEntity<List<CommentSearchResponse>> findRepliesByParentId(
            @PathVariable UUID parentCommentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<CommentSearchResponse> results = commentSearchService.findRepliesByParentId(parentCommentId, page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * 작성자별 댓글 조회
     *
     * GET /api/search/comments/by-author/{authorId}?page=0&size=20
     */
    @GetMapping("/by-author/{authorId}")
    public ResponseEntity<List<CommentSearchResponse>> findByAuthorId(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<CommentSearchResponse> results = commentSearchService.findByAuthorId(authorId, page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * 댓글 내용으로 검색
     *
     * GET /api/search/comments?keyword=검색어&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<List<CommentSearchResponse>> searchByContent(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<CommentSearchResponse> results = commentSearchService.searchByContent(keyword, page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * 복합 검색 (키워드 + 필터)
     *
     * GET /api/search/comments/advanced?keyword=검색어&postId=...&authorId=...&isReply=false&page=0&size=20
     */
    @GetMapping("/advanced")
    public ResponseEntity<List<CommentSearchResponse>> searchWithFilters(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID postId,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) Boolean isReply,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<CommentSearchResponse> results = commentSearchService.searchWithFilters(
                keyword, postId, authorId, isReply, page, size
        );
        return ResponseEntity.ok(results);
    }

    /**
     * 게시글의 댓글 수 조회
     *
     * GET /api/search/comments/by-post/{postId}/count
     */
    @GetMapping("/by-post/{postId}/count")
    public ResponseEntity<Long> countByPostId(@PathVariable UUID postId) {
        long count = commentSearchService.countByPostId(postId);
        return ResponseEntity.ok(count);
    }
}
