package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Post 관련 이벤트 발행 서비스
 *
 * 책임:
 * - Post 관련 이벤트 발행 로직 캡슐화
 * - 이벤트 타입별 발행 메서드 제공
 *
 * SRP 준수: Post 이벤트 발행이라는 단일 책임만 가짐
 */
@Component
public class PostEventPublisher {

    private final DataChangeEventPublisher dataChangeEventPublisher;

    public PostEventPublisher(DataChangeEventPublisher dataChangeEventPublisher) {
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    /**
     * PostCreated 이벤트 발행
     *
     * Detail Projection과 Count Projection 두 개의 이벤트를 발행합니다.
     *
     * @param postId Post ID
     * @param detailProjection Detail Projection
     * @param countProjection Count Projection
     */
    public void publishPostCreated(
            UUID postId,
            PostDetailProjection detailProjection,
            PostCountProjection countProjection
    ) {
        dataChangeEventPublisher.publish("PostCreated", postId, detailProjection);
        dataChangeEventPublisher.publish("PostCreated", postId, countProjection);
    }

    /**
     * PostUpdated 이벤트 발행
     *
     * @param postId Post ID
     * @param detailProjection Detail Projection
     * @param countProjection Count Projection
     */
    public void publishPostUpdated(
            UUID postId,
            PostDetailProjection detailProjection,
            PostCountProjection countProjection
    ) {
        dataChangeEventPublisher.publish("PostUpdated", postId, detailProjection);
        dataChangeEventPublisher.publish("PostUpdated", postId, countProjection);
    }

    /**
     * PostDeleted 이벤트 발행
     *
     * @param postId Post ID
     * @param detailProjection Detail Projection
     */
    public void publishPostDeleted(UUID postId, PostDetailProjection detailProjection) {
        dataChangeEventPublisher.publish("PostDeleted", postId, detailProjection);
    }

    /**
     * PostPublished 이벤트 발행
     *
     * @param postId Post ID
     * @param detailProjection Detail Projection
     */
    public void publishPostPublished(UUID postId, PostDetailProjection detailProjection) {
        dataChangeEventPublisher.publish("PostPublished", postId, detailProjection);
    }

    /**
     * PostViewCountIncremented 이벤트 발행
     *
     * @param postId Post ID
     * @param countProjection Count Projection (viewCount만 포함)
     */
    public void publishPostViewCountIncremented(UUID postId, PostCountProjection countProjection) {
        dataChangeEventPublisher.publish("PostViewCountIncremented", postId, countProjection);
    }
}
