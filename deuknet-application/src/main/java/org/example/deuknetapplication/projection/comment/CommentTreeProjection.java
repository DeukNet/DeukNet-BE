package org.example.deuknetapplication.projection.comment;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Projection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 댓글 트리 구조 조회용 Projection
 *
 * 댓글과 대댓글을 계층 구조로 표현할 때 사용합니다.
 * 대댓글 목록을 포함하여 한 번에 조회합니다.
 */
@Getter
public class CommentTreeProjection extends Projection {

    private final UUID postId;
    private final String content;

    // 작성자 정보
    private final UUID authorId;
    private final String authorUsername;
    private final String authorDisplayName;
    private final String authorAvatarUrl;

    // 대댓글 목록
    private final List<CommentProjection> replies;

    // 메타 정보
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CommentTreeProjection(UUID id, UUID postId, String content,
                                 UUID authorId, String authorUsername, String authorDisplayName, String authorAvatarUrl,
                                 List<CommentProjection> replies,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.postId = postId;
        this.content = content;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.authorDisplayName = authorDisplayName;
        this.authorAvatarUrl = authorAvatarUrl;
        this.replies = replies;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
