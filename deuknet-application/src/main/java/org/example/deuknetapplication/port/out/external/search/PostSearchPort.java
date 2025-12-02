package org.example.deuknetapplication.port.out.external.search;

import org.example.deuknetapplication.port.in.post.PageResponse;
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
     * @return 페이지네이션된 검색 결과
     */
    PageResponse<PostSearchResponse> search(PostSearchRequest request);

    /**
     * 인기 게시글 조회
     */
    PageResponse<PostSearchResponse> findPopularPosts(int page, int size, UUID categoryId, String keyword);

    /**
     * 검색어 자동완성 제안
     *
     * @param prefix 사용자가 입력한 검색어 (부분 문자열)
     * @param size 최대 제안 개수
     * @return 제안된 검색어 목록
     */
    List<String> suggestKeywords(String prefix, int size);
}
