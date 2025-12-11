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
     * sortType: RECENT(최신순), POPULAR(인기순)
     * 항상 PUBLISHED 상태만 조회
     *
     * @param request 검색 조건 (keyword, authorId, categoryId, sortType 등)
     * @return 페이지네이션된 검색 결과
     * @deprecated Service 레이어에서 sortType별 메서드를 직접 호출하도록 변경됨
     */
    @Deprecated
    PageResponse<PostSearchResponse> search(PostSearchRequest request);

    /**
     * 인기순 검색
     * 인기도 = (likeCount * 3 + viewCount * 1) * timeDecay
     *
     * @param keyword 검색 키워드
     * @param authorId 작성자 ID
     * @param categoryId 카테고리 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 인기순 정렬된 검색 결과
     */
    PageResponse<PostSearchResponse> searchByPopular(String keyword, UUID authorId, UUID categoryId, int page, int size);

    /**
     * 관련성 검색 (검색어 기반 스코어 정렬)
     * minScore 1.5 이상만 반환
     *
     * @param keyword 검색 키워드
     * @param authorId 작성자 ID
     * @param categoryId 카테고리 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 관련성 높은 순으로 정렬된 검색 결과
     */
    PageResponse<PostSearchResponse> searchByRelevance(String keyword, UUID authorId, UUID categoryId, int page, int size);

    /**
     * 최신순 검색 (createdAt 내림차순)
     *
     * @param keyword 검색 키워드
     * @param authorId 작성자 ID
     * @param categoryId 카테고리 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 최신순 정렬된 검색 결과
     */
    PageResponse<PostSearchResponse> searchByRecent(String keyword, UUID authorId, UUID categoryId, int page, int size);

    /**
     * 검색어 자동완성 제안
     *
     * @param prefix 사용자가 입력한 검색어 (부분 문자열)
     * @param size 최대 제안 개수
     * @return 제안된 검색어 목록
     */
    List<String> suggestKeywords(String prefix, int size);

    /**
     * 카테고리별 개념글 조회 (좋아요 많은 글 상위 20개)
     *
     * @param categoryId 카테고리 ID (null이면 전체)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 개념글 목록
     */
    PageResponse<PostSearchResponse> findFeaturedPosts(UUID categoryId, int page, int size);
}
