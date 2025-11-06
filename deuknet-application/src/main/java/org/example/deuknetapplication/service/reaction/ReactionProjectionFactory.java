package org.example.deuknetapplication.service.reaction;

import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Reaction 관련 Projection 객체 생성 팩토리
 *
 * 책임:
 * - Reaction 이벤트로부터 PostCountProjection 생성
 * - 통계(viewCount, likeCount, dislikeCount) Projection 생성 로직 캡슐화
 *
 * SRP 준수: Reaction Projection 생성이라는 단일 책임만 가짐
 */
@Component
public class ReactionProjectionFactory {

    /**
     * ViewCount 증가 시 PostCountProjection 생성
     *
     * @param postId Post ID
     * @param viewCount 조회수
     * @return PostCountProjection (viewCount만 포함)
     */
    public PostCountProjection createCountProjectionForView(UUID postId, Long viewCount) {
        return PostCountProjection.builder()
                .id(postId)
                .viewCount(viewCount)
                .build();
    }

    /**
     * LikeCount 증가 시 PostCountProjection 생성
     *
     * @param postId Post ID
     * @param likeCount 좋아요 수
     * @return PostCountProjection (likeCount만 포함)
     */
    public PostCountProjection createCountProjectionForLike(UUID postId, Long likeCount) {
        return PostCountProjection.builder()
                .id(postId)
                .likeCount(likeCount)
                .build();
    }

    /**
     * DislikeCount 증가 시 PostCountProjection 생성
     *
     * @param postId Post ID
     * @param dislikeCount 싫어요 수
     * @return PostCountProjection (dislikeCount만 포함)
     */
    public PostCountProjection createCountProjectionForDislike(UUID postId, Long dislikeCount) {
        return PostCountProjection.builder()
                .id(postId)
                .dislikeCount(dislikeCount)
                .build();
    }

    /**
     * 전체 통계 PostCountProjection 생성
     *
     * Post 수정/발행 시 모든 통계를 포함한 Projection을 생성합니다.
     *
     * @param postId Post ID
     * @param commentCount 댓글 수
     * @param likeCount 좋아요 수
     * @param dislikeCount 싫어요 수
     * @param viewCount 조회수
     * @return PostCountProjection (모든 통계 포함)
     */
    public PostCountProjection createCountProjection(
            UUID postId,
            Long commentCount,
            Long likeCount,
            Long dislikeCount,
            Long viewCount
    ) {
        return PostCountProjection.builder()
                .id(postId)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .viewCount(viewCount)
                .build();
    }
}
