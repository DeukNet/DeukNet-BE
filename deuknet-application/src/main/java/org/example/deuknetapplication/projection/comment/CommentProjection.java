package org.example.deuknetapplication.projection.comment;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Projection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 댓글 조회용 Projection
 *
 * 댓글 목록 및 상세 조회에 사용됩니다.
 * 작성자 정보와 함께 조회하여 추가 쿼리를 방지합니다.
 */
@Getter
public class CommentProjection extends Projection {

    private final UUID postId;
    private final String content;

    // 작성자 정보
    private final UUID authorId;
    private final String authorUsername;
    private final String authorDisplayName;
    private final String authorAvatarUrl;

    // 대댓글 정보
    private final UUID parentCommentId;
    private final boolean isReply;

    // 작성자 타입 (REAL/ANONYMOUS)
    private final String authorType;

    // 메타 정보
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CommentProjection(UUID id, UUID postId, String content,
                             UUID authorId, String authorUsername, String authorDisplayName, String authorAvatarUrl,
                             UUID parentCommentId, boolean isReply, String authorType,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.postId = postId;
        this.content = content;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.authorDisplayName = authorDisplayName;
        this.authorAvatarUrl = authorAvatarUrl;
        this.parentCommentId = parentCommentId;
        this.isReply = isReply;
        this.authorType = authorType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
