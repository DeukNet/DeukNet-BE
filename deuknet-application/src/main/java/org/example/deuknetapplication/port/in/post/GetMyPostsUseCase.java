package org.example.deuknetapplication.port.in.post;

/**
 * 내 게시글 조회 UseCase
 */
public interface GetMyPostsUseCase {
    /**
     * 현재 사용자의 게시글 조회 (익명 포함)
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 게시글 검색 결과
     */
    PageResponse<PostSearchResponse> getMyPosts(int page, int size);
}
