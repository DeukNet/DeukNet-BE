package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.post.IncrementViewCountUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.example.deuknetdomain.domain.post.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Post 조회수 증가 서비스 (SRP 준수)
 *
 * 책임:
 * - Post 조회수 증가
 * - PostCountProjection 이벤트 발행 (Outbox Pattern)
 *
 * 특징:
 * - 권한 검증 없음 (누구나 조회 가능)
 * - PostCountProjection만 업데이트 (PostDetailProjection은 업데이트하지 않음)
 */
@Service
@Transactional
public class IncrementViewCountService implements IncrementViewCountUseCase {

    private final PostRepository postRepository;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public IncrementViewCountService(
            PostRepository postRepository,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.postRepository = postRepository;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public void incrementViewCount(UUID postId) {
        // 1. Post 조회
        Post post = getPost(postId);

        // 2. 조회수 증가
        incrementPostViewCount(post);

        // 3. PostCountProjection 이벤트 발행
        publishViewCountIncrementedEvent(post);
    }

    /**
     * Post 조회 (SRP: Post 조회 책임 분리)
     */
    private Post getPost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    /**
     * Post 조회수 증가 (SRP: 조회수 증가 책임 분리)
     */
    private void incrementPostViewCount(Post post) {
        post.incrementViewCount();
        postRepository.save(post);
    }

    /**
     * PostViewCountIncremented 이벤트 발행 (SRP: 이벤트 발행 책임 분리)
     *
     * PostCountProjection만 업데이트합니다.
     * PostDetailProjection은 업데이트하지 않습니다 (조회수 증가 시마다 전체 업데이트는 비효율적).
     */
    private void publishViewCountIncrementedEvent(Post post) {
        PostCountProjection projection = PostCountProjection.builder()
                .id(post.getId())
                .commentCount(null)  // 변경되지 않음
                .likeCount(null)     // 변경되지 않음
                .viewCount(post.getViewCount())  // 조회수만 업데이트
                .build();

        dataChangeEventPublisher.publish("PostViewCountIncremented", post.getId(), projection);
    }
}
