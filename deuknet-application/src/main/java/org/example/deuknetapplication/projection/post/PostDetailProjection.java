package org.example.deuknetapplication.projection.post;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Projection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 게시글 상세 조회용 Projection
 *
 * 게시글 상세 페이지에 필요한 모든 정보를 포함합니다.
 * 작성자 정보, 카테고리, 반응 등을 함께 조회합니다.
 */
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDetailProjection extends Projection {

    private final String title;
    private final String content;

    // 작성자 정보
    private final UUID authorId;
    private final String authorUsername;
    private final String authorDisplayName;
    private final String authorAvatarUrl;
    private final String authorType;

    // 게시글 메타 정보
    private final String status;
    private final Long viewCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // 연관 데이터
    private final UUID categoryId;
    private final String categoryName;
    private final Long commentCount;
    private final Long likeCount;
    private final Long dislikeCount;

    @Builder
    @JsonCreator
    public PostDetailProjection(
            @JsonProperty("id") UUID id,
            @JsonProperty("title") String title,
            @JsonProperty("content") String content,
            @JsonProperty("authorId") UUID authorId,
            @JsonProperty("authorUsername") String authorUsername,
            @JsonProperty("authorDisplayName") String authorDisplayName,
            @JsonProperty("authorAvatarUrl") String authorAvatarUrl,
            @JsonProperty("authorType") String authorType,
            @JsonProperty("status") String status,
            @JsonProperty("viewCount") Long viewCount,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt,
            @JsonProperty("categoryId") UUID categoryId,
            @JsonProperty("categoryName") String categoryName,
            @JsonProperty("commentCount") Long commentCount,
            @JsonProperty("likeCount") Long likeCount,
            @JsonProperty("dislikeCount") Long dislikeCount) {
        super(id);
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.authorDisplayName = authorDisplayName;
        this.authorAvatarUrl = authorAvatarUrl;
        this.authorType = authorType;
        this.status = status;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }
}
