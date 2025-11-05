package org.example.deuknetapplication.projection.post;

import lombok.Builder;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Projection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 게시글 상세 조회용 Projection
 *
 * 게시글 상세 페이지에 필요한 모든 정보를 포함합니다.
 * 작성자 정보, 카테고리, 반응 등을 함께 조회합니다.
 */
@Getter
public class PostDetailProjection extends Projection {

    private final String title;
    private final String content;

    // 작성자 정보
    private final UUID authorId;
    private final String authorUsername;
    private final String authorDisplayName;
    private final String authorAvatarUrl;

    // 게시글 메타 정보
    private final String status;
    private final Long viewCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // 연관 데이터
    private final List<UUID> categoryIds;
    private final List<String> categoryNames;
    private final Long commentCount;
    private final Long likeCount;
    private final Long dislikeCount;

    @Builder
    public PostDetailProjection(UUID id, String title, String content,
                                UUID authorId, String authorUsername, String authorDisplayName, String authorAvatarUrl,
                                String status, Long viewCount, LocalDateTime createdAt, LocalDateTime updatedAt,
                                List<UUID> categoryIds, List<String> categoryNames, Long commentCount, Long likeCount, Long dislikeCount) {
        super(id);
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.authorDisplayName = authorDisplayName;
        this.authorAvatarUrl = authorAvatarUrl;
        this.status = status;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.categoryIds = categoryIds;
        this.categoryNames = categoryNames;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }
}
