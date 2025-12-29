package org.example.deuknetapplication.port.in.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.deuknetdomain.domain.comment.Comment;
import org.example.deuknetdomain.domain.post.AuthorType;
import org.example.deuknetdomain.domain.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 댓글 조회 응답
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

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

    /**
     * Domain 객체와 User 정보로부터 Response 생성 (익명 댓글)
     */
    public static CommentResponse fromAnonymous(Comment comment, boolean isAuthor) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getContent().getValue(),
                null,  // authorId 숨김
                "익명",
                "익명",
                null,  // avatarUrl 숨김
                comment.getParentCommentId().orElse(null),
                comment.isReply(),
                comment.getAuthorType(),
                isAuthor,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    /**
     * Domain 객체와 User 정보로부터 Response 생성 (실명 댓글)
     */
    public static CommentResponse from(Comment comment, User author, boolean isAuthor) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getContent().getValue(),
                author.getId(),
                author.getUsername(),
                author.getDisplayName(),
                author.getAvatarUrl(),
                comment.getParentCommentId().orElse(null),
                comment.isReply(),
                comment.getAuthorType(),
                isAuthor,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
