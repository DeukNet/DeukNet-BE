package org.example.deuknetapplication.port.in.post;

/**
 * 내가 좋아요 누른 게시글 조회 UseCase
 */
public interface GetMyLikedPostsUseCase {

    /**
     * 현재 로그인한 사용자가 좋아요를 누른 게시글 조회
     */
    PageResponse<PostSearchResponse> getMyLikedPosts(int page, int size);
}
