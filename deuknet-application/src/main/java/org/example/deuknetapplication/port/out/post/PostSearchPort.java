package org.example.deuknetapplication.port.out.post;

import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 게시글 검색 Port (Elasticsearch)
 */
public interface PostSearchPort {

    /**
     * ID로 게시글 조회
     */
    Optional<PostSearchResponse> findById(UUID id);

    /**
     * 통합 검색 (모든 조건 AND)
     *
     * @param request 검색 조건 (keyword, authorId, categoryId, status 등)
     * @return 검색 결과 리스트
     */
    List<PostSearchResponse> search(PostSearchRequest request);

    /**
     * 인기 게시글 조회
     */
    List<PostSearchResponse> findPopularPosts(int page, int size);
}
