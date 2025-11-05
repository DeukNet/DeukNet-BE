package org.example.deuknetapplication.port.in.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.deuknetapplication.projection.post.PostDetailProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 게시글 검색 응답
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchResponse {

    private UUID id;
    private String title;
    private String content;
    private UUID authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String status;
    private List<UUID> categoryIds;
    private List<String> categoryNames;
    private Long viewCount;
    private Long commentCount;
    private Long likeCount;
    private Long dislikeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostSearchResponse(PostDetailProjection projection) {
        this.id = projection.getId();
        this.title = projection.getTitle();
        this.content = projection.getContent();
        this.authorId = projection.getAuthorId();
        this.authorUsername = projection.getAuthorUsername();
        this.authorDisplayName = projection.getAuthorDisplayName();
        this.status = projection.getStatus();
        this.categoryIds = projection.getCategoryIds();
        this.categoryNames = projection.getCategoryNames();
        this.viewCount = projection.getViewCount();
        this.commentCount = projection.getCommentCount();
        this.likeCount = projection.getLikeCount();
        this.dislikeCount = projection.getDislikeCount();
        this.createdAt = projection.getCreatedAt();
        this.updatedAt = projection.getUpdatedAt();
    }
}
