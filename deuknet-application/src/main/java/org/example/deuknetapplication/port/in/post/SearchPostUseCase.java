package org.example.deuknetapplication.port.in.post;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 게시글 검색 UseCase
 */
public interface SearchPostUseCase {

    /**
     * ID로 게시글 조회
     */
    Optional<PostSearchResponse> findById(UUID postId);

    /**
     * 통합 검색 (모든 조건 AND)
     */
    List<PostSearchResponse> search(PostSearchRequest request);

    /**
     * 인기 게시글 조회
     */
    List<PostSearchResponse> findPopularPosts(int page, int size);
}
