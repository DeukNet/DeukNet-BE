package org.example.deuknetapplication.port.in.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.post.AuthorType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 게시글 검색 응답
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchResponse {

    private UUID id;
    private String title;
    private String content;
    private UUID authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String authorType;
    private String status;
    private UUID categoryId;
    private String categoryName;
    private Long viewCount;
    private Long commentCount;
    private Long likeCount;
    private Long dislikeCount;
    private Boolean hasUserLiked;
    private Boolean hasUserDisliked;
    private UUID userLikeReactionId;      // 사용자가 누른 LIKE reaction ID (취소용)
    private UUID userDislikeReactionId;   // 사용자가 누른 DISLIKE reaction ID (취소용)
    private Boolean isAuthor;             // 현재 사용자가 작성자인지 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostSearchResponse(PostDetailProjection projection) {
        this.id = projection.getId();
        this.title = projection.getTitle();
        this.content = projection.getContent();
        this.authorType = projection.getAuthorType();
        this.authorId = projection.getAuthorId();
        this.authorUsername = projection.getAuthorUsername();
        this.authorDisplayName = projection.getAuthorDisplayName();
        this.status = projection.getStatus();
        this.categoryId = projection.getCategoryId();
        this.categoryName = projection.getCategoryName();
        this.viewCount = projection.getViewCount();
        this.commentCount = projection.getCommentCount();
        this.likeCount = projection.getLikeCount();
        this.dislikeCount = projection.getDislikeCount();
        this.createdAt = projection.getCreatedAt();
        this.updatedAt = projection.getUpdatedAt();
    }
}
