package org.example.deuknetpresentation.controller.search;

import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.ReactionCountSearchResponse;
import org.example.deuknetapplication.service.search.ReactionCountSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ReactionCount 검색 컨트롤러
 *
 * Elasticsearch를 활용한 리액션 집계 조회 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/search/reactions")
@RequiredArgsConstructor
public class ReactionCountSearchController {

    private final ReactionCountSearchService reactionCountSearchService;

    /**
     * targetId로 리액션 카운트 조회
     *
     * GET /api/search/reactions/{targetId}
     *
     * @return 200 OK (리액션 존재) / 404 NOT FOUND (리액션 없음)
     */
    @GetMapping("/{targetId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReactionCountSearchResponse> findByTargetId(@PathVariable UUID targetId) {
        Optional<ReactionCountSearchResponse> result = reactionCountSearchService.findByTargetId(targetId);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 여러 targetId의 리액션 카운트를 일괄 조회
     *
     * POST /api/search/reactions/batch
     * Body: ["uuid1", "uuid2", ...]
     *
     * @return 200 OK
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ReactionCountSearchResponse>> findByTargetIds(@RequestBody List<UUID> targetIds) {
        List<ReactionCountSearchResponse> results = reactionCountSearchService.findByTargetIds(targetIds);
        return ResponseEntity.ok(results);
    }

    /**
     * 좋아요가 많은 컨텐츠 조회
     *
     * GET /api/search/reactions/most-liked?page=0&size=20
     */
    @GetMapping("/most-liked")
    public ResponseEntity<List<ReactionCountSearchResponse>> findMostLiked(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<ReactionCountSearchResponse> results = reactionCountSearchService.findMostLiked(page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * 전체 리액션이 많은 컨텐츠 조회
     *
     * GET /api/search/reactions/most-reacted?page=0&size=20
     */
    @GetMapping("/most-reacted")
    public ResponseEntity<List<ReactionCountSearchResponse>> findMostReacted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<ReactionCountSearchResponse> results = reactionCountSearchService.findMostReacted(page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * 최소 좋아요 수 이상의 컨텐츠 조회
     *
     * GET /api/search/reactions/by-min-likes?minLikes=100&page=0&size=20
     */
    @GetMapping("/by-min-likes")
    public ResponseEntity<List<ReactionCountSearchResponse>> findByMinLikeCount(
            @RequestParam long minLikes,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<ReactionCountSearchResponse> results = reactionCountSearchService.findByMinLikeCount(minLikes, page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * 최소 전체 리액션 수 이상의 컨텐츠 조회
     *
     * GET /api/search/reactions/by-min-total?minTotal=100&page=0&size=20
     */
    @GetMapping("/by-min-total")
    public ResponseEntity<List<ReactionCountSearchResponse>> findByMinTotalCount(
            @RequestParam long minTotal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<ReactionCountSearchResponse> results = reactionCountSearchService.findByMinTotalCount(minTotal, page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * 최근 업데이트된 리액션 카운트 조회
     *
     * GET /api/search/reactions/recently-updated?page=0&size=20
     */
    @GetMapping("/recently-updated")
    public ResponseEntity<List<ReactionCountSearchResponse>> findRecentlyUpdated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<ReactionCountSearchResponse> results = reactionCountSearchService.findRecentlyUpdated(page, size);
        return ResponseEntity.ok(results);
    }

    /**
     * 전체 리액션 카운트 집계
     *
     * GET /api/search/reactions/total-count
     */
    @GetMapping("/total-count")
    public ResponseEntity<Long> getTotalReactionCount() {
        long totalCount = reactionCountSearchService.getTotalReactionCount();
        return ResponseEntity.ok(totalCount);
    }
}
