package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.in.post.SearchPostUseCase;
import org.example.deuknetapplication.port.in.post.SortType;
import org.example.deuknetapplication.port.out.external.search.PostSearchPort;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.post.AuthorType;
import org.example.deuknetdomain.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 게시글 검색 유스케이스 구현
 * SRP: PostSearchPort로 위임하여 검색 로직 처리
 */
@Service
public class SearchPostService implements SearchPostUseCase {

    private static final Logger log = LoggerFactory.getLogger(SearchPostService.class);

    private final PostSearchPort postSearchPort;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;

    public SearchPostService(PostSearchPort postSearchPort, UserRepository userRepository, CurrentUserPort currentUserPort) {
        this.postSearchPort = postSearchPort;
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public PageResponse<PostSearchResponse> search(PostSearchRequest request) {
        // 익명 조회 권한 확인 (비인증 사용자, 본인/다른 사람 게시물 구분)
        boolean includeAnonymous = resolveIncludeAnonymous(request.isIncludeAnonymous(), request.getAuthorId());

        // sortType에 따라 적절한 검색 메서드 호출
        PageResponse<PostSearchResponse> response = switch (request.getSortType()) {
            case RECENT -> postSearchPort.searchByRecent(
                    request.getKeyword(),
                    request.getAuthorId(),
                    request.getCategoryId(),
                    request.getPage(),
                    request.getSize(),
                    includeAnonymous
            );
            case RELEVANCE -> postSearchPort.searchByRelevance(
                    request.getKeyword(),
                    request.getAuthorId(),
                    request.getCategoryId(),
                    request.getPage(),
                    request.getSize(),
                    includeAnonymous
            );
            case POPULAR -> postSearchPort.searchByPopular(
                    request.getKeyword(),
                    request.getAuthorId(),
                    request.getCategoryId(),
                    request.getPage(),
                    request.getSize(),
                    includeAnonymous
            );
        };

        response.getContent().forEach(this::enrichPostResponse);
        return response;
    }

    /**
     * 익명 게시물 조회 권한 확인
     * - 비인증 사용자: 항상 익명 제외
     * - 인증 사용자 + 본인 게시물 조회: 권한 있으면 익명 포함
     * - 인증 사용자 + 다른 사람 게시물 조회: 익명 제외
     * - 전체 조회: 권한 있으면 익명 포함
     */
    private boolean resolveIncludeAnonymous(boolean requestedIncludeAnonymous, UUID authorId) {
        log.debug("[ANONYMOUS_ACCESS] requestedIncludeAnonymous={}, authorId={}", requestedIncludeAnonymous, authorId);

        if (!requestedIncludeAnonymous) {
            log.debug("[ANONYMOUS_ACCESS] Request explicitly excludes anonymous posts");
            return false;
        }

        try {
            UUID currentUserId = currentUserPort.getCurrentUserId();
            log.debug("[ANONYMOUS_ACCESS] Authenticated user: currentUserId={}", currentUserId);

            // 다른 사용자의 게시물 조회 시 익명 제외
            if (authorId != null && !authorId.equals(currentUserId)) {
                log.debug("[ANONYMOUS_ACCESS] Viewing other user's posts: authorId={}, currentUserId={} -> exclude anonymous", authorId, currentUserId);
                return false;
            }

            // 전체 조회 또는 본인 게시물 조회 시 권한 확인
            User user = userRepository.findById(currentUserId).orElse(null);
            boolean hasPermission = user != null && user.isCanAccessAnonymous();
            log.debug("[ANONYMOUS_ACCESS] userId={}, canAccessAnonymous={}", currentUserId, hasPermission);
            return hasPermission;

        } catch (Exception e) {
            // 비인증 사용자는 익명 조회 불가
            log.debug("[ANONYMOUS_ACCESS] Unauthenticated user -> exclude anonymous");
            return false;
        }
    }

    /**
     * 현재 사용자의 익명 조회 권한 확인
     */
    private boolean hasAnonymousAccessPermission() {
        try {
            UUID currentUserId = currentUserPort.getCurrentUserId();
            log.debug("[ANONYMOUS_ACCESS] Checking permission for userId={}", currentUserId);

            User user = userRepository.findById(currentUserId).orElse(null);

            if (user == null) {
                log.warn("[ANONYMOUS_ACCESS] User not found: userId={}", currentUserId);
                return false;
            }

            boolean canAccess = user.isCanAccessAnonymous();
            log.debug("[ANONYMOUS_ACCESS] userId={}, canAccessAnonymous={}", currentUserId, canAccess);
            return canAccess;
        } catch (Exception e) {
            // 비인증 사용자는 익명 조회 불가
            log.debug("[ANONYMOUS_ACCESS] Unauthenticated user or error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 게시물 응답 enrichment (isAuthor 체크 → User 정보 설정)
     * isAuthor는 마스킹 전에 체크해야 함
     */
    private void enrichPostResponse(PostSearchResponse response) {
        // 1. 인증된 사용자인 경우 isAuthor 체크 (마스킹 전)
        try {
            UUID currentUserId = currentUserPort.getCurrentUserId();
            response.setIsAuthor(
                    Optional.ofNullable(response.getAuthorId())
                            .map(authorId -> authorId.equals(currentUserId))
                            .orElse(false)
            );
        } catch (Exception e) {
            // 인증되지 않은 사용자
            response.setIsAuthor(false);
        }

        // 2. User 정보 enrichment (익명이면 authorId를 null로 마스킹)
        userRepository.enrichWithUserInfo(response);
    }

    @Override
    public List<String> suggestKeywords(String prefix, int size) {
        return postSearchPort.suggestKeywords(prefix, size);
    }

    @Override
    public PageResponse<PostSearchResponse> findFeaturedPosts(UUID categoryId, int page, int size) {
        // 개념글은 익명 게시물 제외 (실명만 표시)
        PageResponse<PostSearchResponse> response = postSearchPort.findFeaturedPosts(categoryId, page, size, false);
        response.getContent().forEach(this::enrichPostResponse);
        return response;
    }

    @Override
    public PageResponse<PostSearchResponse> findMyPosts(int page, int size) {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        PostSearchRequest request = PostSearchRequest.builder()
                .authorId(currentUserId)
                .sortType(SortType.RECENT)
                .page(page)
                .size(size)
                .build();

        return search(request);
    }

    @Override
    public List<PostSearchResponse> findTrendingPosts(int size) {
        // 더 많이 조회해서 필터링 후에도 충분한 결과 보장
        List<PostSearchResponse> results = postSearchPort.findTrendingPosts(size * 2);

        // 익명 조회 권한이 없으면 익명 게시물 필터링
        boolean canAccessAnonymous = hasAnonymousAccessPermission();

        return results.stream()
                .filter(post -> canAccessAnonymous || !AuthorType.ANONYMOUS.equals(post.getAuthorType()))
                .limit(size)
                .peek(this::enrichPostResponse)
                .toList();
    }
}
