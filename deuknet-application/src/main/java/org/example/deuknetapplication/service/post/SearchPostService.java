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
    public Optional<PostSearchResponse> findById(UUID postId) {
        Optional<PostSearchResponse> response = postSearchPort.findById(postId);
        response.ifPresent(this::enrichWithUserInfo);
        return response;
    }

    @Override
    public PageResponse<PostSearchResponse> search(PostSearchRequest request) {
        // sortType에 따라 적절한 검색 메서드 호출
        PageResponse<PostSearchResponse> response = switch (request.getSortType()) {
            case POPULAR -> postSearchPort.searchByPopular(
                    request.getKeyword(),
                    request.getAuthorId(),
                    request.getCategoryId(),
                    request.getPage(),
                    request.getSize()
            );
            case RECENT -> {
                // 검색어가 있으면 관련성 검색, 없으면 최신순 검색
                if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                    yield postSearchPort.searchByRelevance(
                            request.getKeyword(),
                            request.getAuthorId(),
                            request.getCategoryId(),
                            request.getPage(),
                            request.getSize()
                    );
                } else {
                    yield postSearchPort.searchByRecent(
                            request.getKeyword(),
                            request.getAuthorId(),
                            request.getCategoryId(),
                            request.getPage(),
                            request.getSize()
                    );
                }
            }
        };

        response.getContent().forEach(this::enrichWithUserInfo);
        return response;
    }

    @Override
    public List<String> suggestKeywords(String prefix, int size) {
        return postSearchPort.suggestKeywords(prefix, size);
    }

    @Override
    public PageResponse<PostSearchResponse> findFeaturedPosts(UUID categoryId, int page, int size) {
        PageResponse<PostSearchResponse> response = postSearchPort.findFeaturedPosts(categoryId, page, size);
        response.getContent().forEach(this::enrichWithUserInfo);
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

        PageResponse<PostSearchResponse> response = search(request);
        return response;
    }

    /**
     * 익명 여부에 따라 User 정보를 조회하여 설정
     * ANONYMOUS인 경우: "익명"으로 설정
     * REAL인 경우: PostgreSQL에서 User 조회하여 설정
     */
    private void enrichWithUserInfo(PostSearchResponse response) {
        if ("ANONYMOUS".equals(response.getAuthorType())) {
            // 익명 게시물은 User 정보 숨김
            response.setAuthorId(null);
            response.setAuthorUsername("익명");
            response.setAuthorDisplayName("익명");
        } else {
            // 실명 게시물은 User 조회
            userRepository.findById(response.getAuthorId()).ifPresent(user -> {
                response.setAuthorUsername(user.getUsername());
                response.setAuthorDisplayName(user.getDisplayName());
            });
        }
    }
}
