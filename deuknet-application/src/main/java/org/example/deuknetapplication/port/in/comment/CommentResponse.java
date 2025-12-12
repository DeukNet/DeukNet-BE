package org.example.deuknetapplication.port.in.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.deuknetapplication.port.out.repository.AuthorInfoEnrichable;
import org.example.deuknetapplication.projection.comment.CommentProjection;
import org.example.deuknetdomain.domain.post.AuthorType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 댓글 조회 응답
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse implements AuthorInfoEnrichable {

    private UUID id;
    private UUID postId;
    private String content;

    // 작성자 정보
    private UUID authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String authorAvatarUrl;

    // 대댓글 정보
    private UUID parentCommentId;
    private boolean isReply;

    // 작성자 타입 (REAL/ANONYMOUS)
    private AuthorType authorType;

    // 권한 정보
    private Boolean isAuthor;  // 현재 사용자가 작성자인지 여부

    // 메타 정보
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponse(CommentProjection projection) {
        this.id = projection.getId();
        this.postId = projection.getPostId();
        this.content = projection.getContent();
        this.authorId = projection.getAuthorId();
        this.authorUsername = projection.getAuthorUsername();
        this.authorDisplayName = projection.getAuthorDisplayName();
        this.authorAvatarUrl = projection.getAuthorAvatarUrl();
        this.parentCommentId = projection.getParentCommentId();
        this.isReply = projection.isReply();
        this.authorType = AuthorType.valueOf(projection.getAuthorType());
        this.createdAt = projection.getCreatedAt();
        this.updatedAt = projection.getUpdatedAt();
        this.isAuthor = false; // 기본값, enrichWithUserInfo에서 설정
    }
}
