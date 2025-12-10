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
 * Elasticsearch Document와 동일한 구조를 유지합니다.
 * 작성자/카테고리 상세 정보(username, displayName 등)는 별도 조회합니다.
 *
 * ⚠️ 중요: PostDetailDocument와 필드 구조를 동일하게 유지해야 합니다.
 * 필드 추가/제거 시 Document도 함께 수정하세요.
 */
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDetailProjection extends Projection {

    private final String title;
    private final String content;

    // 작성자 정보 (ID만 저장, 상세 정보는 별도 조회)
    private final UUID authorId;
    private final String authorType;

    // 게시글 메타 정보
    private final String status;
    private final Long viewCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // 연관 데이터 (ID만 저장, 이름은 별도 조회)
    private final UUID categoryId;
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
            @JsonProperty("authorType") String authorType,
            @JsonProperty("status") String status,
            @JsonProperty("viewCount") Long viewCount,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt,
            @JsonProperty("categoryId") UUID categoryId,
            @JsonProperty("commentCount") Long commentCount,
            @JsonProperty("likeCount") Long likeCount,
            @JsonProperty("dislikeCount") Long dislikeCount) {
        super(id);
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorType = authorType;
        this.status = status;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.categoryId = categoryId;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }
}
