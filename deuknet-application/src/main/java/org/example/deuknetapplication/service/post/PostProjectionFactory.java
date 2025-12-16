package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.post.Post;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Post 관련 Projection 객체 생성 팩토리
 *
 * 책임:
 * - Post 엔티티로부터 Projection 객체 생성
 * - Projection 생성 로직 캡슐화
 *
 * SRP 준수: Projection 객체 생성이라는 단일 책임만 가짐
 *
 * ⚠️ 중요: PostDetailProjection과 PostDetailDocument는 동일한 필드 구조를 유지합니다.
 */
@Component
public class PostProjectionFactory {

    /**
     * Post 생성 시 PostDetailProjection 생성
     *
     * @param post Post 엔티티
     * @param categoryId 카테고리 ID
     * @return PostDetailProjection (카운트 초기값 0)
     */
    public PostDetailProjection createDetailProjectionForCreation(
            Post post,
            UUID categoryId
    ) {
        LocalDateTime now = LocalDateTime.now();

        return PostDetailProjection.builder()
                .id(post.getId())
                .title(post.getTitle().getValue())
                .content(post.getContent().getValue())
                .authorId(post.getAuthorId())
                .authorType(post.getAuthorType().name())
                .status(post.getStatus().name())
                .thumbnailImageUrl(post.getThumbnailImageUrl())
                .viewCount(0L)
                .createdAt(now)
                .updatedAt(now)
                .categoryId(categoryId)
                .commentCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .build();
    }

    /**
     * Post 수정 시 PostDetailProjection 생성
     *
     * @param post Post 엔티티
     * @param categoryId 카테고리 ID
     * @param commentCount 댓글 수
     * @param likeCount 좋아요 수
     * @param dislikeCount 싫어요 수
     * @param viewCount 조회수
     * @return PostDetailProjection
     */
    public PostDetailProjection createDetailProjectionForUpdate(
            Post post,
            UUID categoryId,
            Long commentCount,
            Long likeCount,
            Long dislikeCount,
            Long viewCount
    ) {
        return PostDetailProjection.builder()
                .id(post.getId())
                .title(post.getTitle().getValue())
                .content(post.getContent().getValue())
                .authorId(post.getAuthorId())
                .authorType(post.getAuthorType().name())
                .status(post.getStatus().name())
                .thumbnailImageUrl(post.getThumbnailImageUrl())
                .viewCount(viewCount)
                .createdAt(post.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .categoryId(categoryId)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .build();
    }
}
