package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.in.post.SearchPostUseCase;
import org.example.deuknetapplication.port.in.post.SortType;
import org.example.deuknetapplication.port.out.external.search.PostSearchPort;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
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
        // sortType에 따라 적절한 검색 메서드 호출
            PageResponse<PostSearchResponse> response = switch (request.getSortType()) {
            case RECENT -> postSearchPort.searchByRecent(
                    request.getKeyword(),
                    request.getAuthorId(),
                    request.getCategoryId(),
                    request.getPage(),
                    request.getSize(),
                    request.isIncludeAnonymous()
            );
            case RELEVANCE -> postSearchPort.searchByRelevance(
                    request.getKeyword(),
                    request.getAuthorId(),
                    request.getCategoryId(),
                    request.getPage(),
                    request.getSize(),
                    request.isIncludeAnonymous()
            );
            case POPULAR -> postSearchPort.searchByPopular(
                    request.getKeyword(),
                    request.getAuthorId(),
                    request.getCategoryId(),
                    request.getPage(),
                    request.getSize(),
                    request.isIncludeAnonymous()
            );
        };

        response.getContent().forEach(this::enrichPostResponse);
        return response;
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
        List<PostSearchResponse> results = postSearchPort.findTrendingPosts(size);
        results.forEach(this::enrichPostResponse);
        return results;
    }
}
