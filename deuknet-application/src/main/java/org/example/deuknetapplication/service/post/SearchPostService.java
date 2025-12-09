package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.in.post.SearchPostUseCase;
import org.example.deuknetapplication.port.out.external.search.PostSearchPort;
import org.example.deuknetapplication.port.out.repository.UserRepository;
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

    public SearchPostService(PostSearchPort postSearchPort, UserRepository userRepository) {
        this.postSearchPort = postSearchPort;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<PostSearchResponse> findById(UUID postId) {
        Optional<PostSearchResponse> response = postSearchPort.findById(postId);
        response.ifPresent(this::enrichWithUserInfo);
        return response;
    }

    @Override
    public PageResponse<PostSearchResponse> search(PostSearchRequest request) {
        PageResponse<PostSearchResponse> response = postSearchPort.search(request);
        response.getContent().forEach(this::enrichWithUserInfo);
        return response;
    }

    @Override
    public PageResponse<PostSearchResponse> findPopularPosts(int page, int size, UUID categoryId, String keyword) {
        PageResponse<PostSearchResponse> response = postSearchPort.findPopularPosts(page, size, categoryId, keyword);
        response.getContent().forEach(this::enrichWithUserInfo);
        return response;
    }

    @Override
    public List<String> suggestKeywords(String prefix, int size) {
        return postSearchPort.suggestKeywords(prefix, size);
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
