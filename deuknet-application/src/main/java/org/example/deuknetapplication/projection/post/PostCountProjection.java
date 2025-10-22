package org.example.deuknetapplication.projection.post;

import lombok.Builder;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Projection;

import java.util.UUID;

/**
 * 게시글 통계 정보 Projection
 *
 * 게시글의 집계 데이터(댓글 수, 좋아요 수 등)만 포함합니다.
 * 이 정보는 자주 변경되므로 별도로 관리하여 효율적으로 업데이트합니다.
 */
@Getter
public class PostCountProjection extends Projection {

    private final Long commentCount;
    private final Long likeCount;
    private final Long viewCount;

    @Builder
    public PostCountProjection(UUID id, Long commentCount, Long likeCount, Long viewCount) {
        super(id);
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
    }
}
