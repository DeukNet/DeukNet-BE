package org.example.deuknetapplication.service.post;

import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.in.post.SearchPostUseCase;
import org.example.deuknetapplication.port.out.post.PostSearchPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 게시글 검색 Service
 *
 * Elasticsearch를 통한 게시글 검색 기능을 제공합니다.
 * 모든 검색 조건은 AND로 결합됩니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostSearchService implements SearchPostUseCase {

    private final PostSearchPort postSearchPort;

    /**
     * ID로 게시글 조회
     */
    public Optional<PostSearchResponse> findById(UUID postId) {
        return postSearchPort.findById(postId);
    }

    /**
     * 통합 검색 (모든 조건 AND)
     *
     * @param request 검색 조건 (keyword, authorId, categoryId, status 등)
     * @return 검색 결과 리스트
     */
    public List<PostSearchResponse> search(PostSearchRequest request) {
        return postSearchPort.search(request);
    }

    /**
     * 인기 게시글 조회 (좋아요 수 기준)
     */
    public List<PostSearchResponse> findPopularPosts(int page, int size) {
        return postSearchPort.findPopularPosts(page, size);
    }
}
