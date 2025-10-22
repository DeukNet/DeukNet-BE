package org.example.deuknetdomain.model.query.post;

import lombok.Builder;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Projection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 게시글 목록 조회용 Projection
 *
 * 게시글 목록을 보여줄 때 필요한 최소한의 정보만 포함합니다.
 * 성능 최적화를 위해 필요한 필드만 선택적으로 조회할 수 있습니다.
 */
@Getter
public class PostSummaryProjection extends Projection {

    private final String title;
    private final UUID authorId;
    private final String authorDisplayName;
    private final String status;
    private final Long viewCount;
    private final Long commentCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public PostSummaryProjection(UUID id, String title, UUID authorId, String authorDisplayName,
                                 String status, Long viewCount, Long commentCount,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.title = title;
        this.authorId = authorId;
        this.authorDisplayName = authorDisplayName;
        this.status = status;
        this.viewCount = viewCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
