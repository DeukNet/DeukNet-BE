package org.example.deuknetdomain.model.query.user;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Projection;

import java.util.UUID;

/**
 * 사용자 요약 정보 조회용 Projection
 *
 * 댓글, 게시글 등에서 작성자 정보를 간단히 표시할 때 사용합니다.
 * 최소한의 정보만 포함하여 성능을 최적화합니다.
 */
@Getter
public class UserSummaryProjection extends Projection {

    private final String username;
    private final String displayName;
    private final String avatarUrl;

    public UserSummaryProjection(UUID id, String username, String displayName, String avatarUrl) {
        super(id);
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }
}
