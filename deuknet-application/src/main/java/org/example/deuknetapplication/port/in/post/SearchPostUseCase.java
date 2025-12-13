package org.example.deuknetapplication.port.in.post;

import java.util.List;
import java.util.UUID;

/**
 * 게시글 검색 UseCase
 */
public interface SearchPostUseCase {

    /**
     * 통합 검색 (모든 조건 AND)
     * sortType: RECENT(최신순), POPULAR(인기순)
     */
    PageResponse<PostSearchResponse> search(PostSearchRequest request);

    /**
     * 검색어 자동완성 제안
     */
    List<String> suggestKeywords(String prefix, int size);

    /**
     * 카테고리별 개념글 조회 (좋아요 많은 글 상위 20개)
     */
    PageResponse<PostSearchResponse> findFeaturedPosts(UUID categoryId, int page, int size);

    /**
     * 현재 로그인한 사용자의 게시글 조회 (익명 게시글 포함)
     */
    PageResponse<PostSearchResponse> findMyPosts(int page, int size);

    /**
     * 실시간 검색어 (실검) Top 10
     */
    List<PostSearchResponse> findTrendingPosts(int size);
}
