package org.example.deuknetapplication.projection.user;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Projection;

import java.util.UUID;

/**
 * 사용자 프로필 조회용 Projection
 *
 * 사용자 프로필 페이지 또는 작성자 정보 표시에 사용됩니다.
 */
@Getter
public class UserProfileProjection extends Projection {

    private final String username;
    private final String displayName;
    private final String bio;
    private final String avatarUrl;

    public UserProfileProjection(UUID id, String username, String displayName, String bio, String avatarUrl) {
        super(id);
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }
}
