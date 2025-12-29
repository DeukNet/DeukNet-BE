package org.example.deuknetapplication.port.in.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.deuknetapplication.port.out.repository.AuthorInfoEnrichable;
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
public class PostSearchResponse implements AuthorInfoEnrichable {

    private UUID id;
    private String title;
    private String content;
    private UUID authorId;
    private String authorUsername;
    private String authorDisplayName;
    private AuthorType authorType;
    private String status;
    private String thumbnailImageUrl;
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

    /**
     * @deprecated Use {@link #fromProjection(PostDetailProjection)} instead
     */
    @Deprecated
    public PostSearchResponse(PostDetailProjection projection) {
        this.id = projection.getId();
        this.title = projection.getTitle();
        this.content = projection.getContent();
        this.authorType = AuthorType.valueOf(projection.getAuthorType());
        this.authorId = projection.getAuthorId();
        // authorUsername, authorDisplayName, categoryName은 별도 조회
        this.status = projection.getStatus();
        this.thumbnailImageUrl = projection.getThumbnailImageUrl();
        this.categoryId = projection.getCategoryId();
        this.viewCount = projection.getViewCount();
        this.commentCount = projection.getCommentCount();
        this.likeCount = projection.getLikeCount();
        this.dislikeCount = projection.getDislikeCount();
        this.createdAt = projection.getCreatedAt();
        this.updatedAt = projection.getUpdatedAt();
    }

    /**
     * Projection 객체로부터 Response 생성
     */
    public static PostSearchResponse fromProjection(PostDetailProjection projection) {
        PostSearchResponse response = new PostSearchResponse();
        response.id = projection.getId();
        response.title = projection.getTitle();
        response.content = projection.getContent();
        response.authorType = AuthorType.valueOf(projection.getAuthorType());
        response.authorId = projection.getAuthorId();
        // authorUsername, authorDisplayName, categoryName은 별도 조회
        response.status = projection.getStatus();
        response.thumbnailImageUrl = projection.getThumbnailImageUrl();
        response.categoryId = projection.getCategoryId();
        response.viewCount = projection.getViewCount();
        response.commentCount = projection.getCommentCount();
        response.likeCount = projection.getLikeCount();
        response.dislikeCount = projection.getDislikeCount();
        response.createdAt = projection.getCreatedAt();
        response.updatedAt = projection.getUpdatedAt();
        return response;
    }
}
