package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.user.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Post 관련 Projection 객체 생성 팩토리
 *
 * 책임:
 * - Post 엔티티로부터 Projection 객체 생성
 * - Projection 생성 로직 캡슐화
 *
 * SRP 준수: Projection 객체 생성이라는 단일 책임만 가짐
 */
@Component
public class PostProjectionFactory {

    /**
     * Post 생성 시 PostDetailProjection 생성
     *
     * @param post Post 엔티티
     * @param author 작성자 정보
     * @param categoryIds 카테고리 ID 목록
     * @return PostDetailProjection
     */
    public PostDetailProjection createDetailProjectionForCreation(
            Post post,
            User author,
            List<UUID> categoryIds
    ) {
        LocalDateTime now = LocalDateTime.now();

        return PostDetailProjection.builder()
                .id(post.getId())
                .title(post.getTitle().getValue())
                .content(post.getContent().getValue())
                .authorId(post.getAuthorId())
                .authorUsername(author.getUsername())
                .authorDisplayName(author.getDisplayName())
                .authorAvatarUrl(author.getAvatarUrl())
                .status(post.getStatus().name())
                .viewCount(0L)  // 초기값 0 (Reaction으로 관리)
                .createdAt(now)
                .updatedAt(now)
                .categoryIds(categoryIds)
                .commentCount(0L)
                .likeCount(0L)
                .build();
    }

    /**
     * Post 수정 시 PostDetailProjection 생성
     *
     * @param post Post 엔티티
     * @param author 작성자 정보
     * @param categoryIds 카테고리 ID 목록
     * @param commentCount 댓글 수
     * @param likeCount 좋아요 수
     * @param viewCount 조회수
     * @return PostDetailProjection
     */
    public PostDetailProjection createDetailProjectionForUpdate(
            Post post,
            User author,
            List<UUID> categoryIds,
            Long commentCount,
            Long likeCount,
            Long viewCount
    ) {
        return PostDetailProjection.builder()
                .id(post.getId())
                .title(post.getTitle().getValue())
                .content(post.getContent().getValue())
                .authorId(post.getAuthorId())
                .authorUsername(author.getUsername())
                .authorDisplayName(author.getDisplayName())
                .authorAvatarUrl(author.getAvatarUrl())
                .status(post.getStatus().name())
                .viewCount(viewCount)  // Reaction에서 집계된 값
                .createdAt(post.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .categoryIds(categoryIds)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .build();
    }

    /**
     * Post 생성 시 PostCountProjection 생성
     *
     * @param postId Post ID
     * @return PostCountProjection (초기값 0)
     */
    public PostCountProjection createCountProjectionForCreation(UUID postId) {
        return PostCountProjection.builder()
                .id(postId)
                .commentCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .viewCount(0L)
                .build();
    }

    // createCountProjection() 제거 - ReactionProjectionFactory로 이동
}
