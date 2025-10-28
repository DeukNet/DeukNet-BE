package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.post.IncrementViewCountUseCase;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.example.deuknetdomain.domain.post.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Post 조회수 증가 유스케이스 구현체 (진짜 SRP 준수)
 *
 * 단일 책임: Post 조회수 증가 유스케이스의 흐름을 조정
 *
 * 특징:
 * - 권한 검증 없음 (누구나 조회 가능)
 * - PostCountProjection만 업데이트 (조회수만 변경)
 */
@Service
@Transactional
public class IncrementViewCountService implements IncrementViewCountUseCase {

    private final PostRepository postRepository;
    private final PostProjectionFactory projectionFactory;
    private final PostEventPublisher eventPublisher;

    public IncrementViewCountService(
            PostRepository postRepository,
            PostProjectionFactory projectionFactory,
            PostEventPublisher eventPublisher
    ) {
        this.postRepository = postRepository;
        this.projectionFactory = projectionFactory;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void incrementViewCount(UUID postId) {
        // 1. Post 조회
        Post post = getPost(postId);

        // 2. 조회수 증가 (핵심 도메인 로직)
        post.incrementViewCount();
        postRepository.save(post);

        // 3. Projection 생성 (전문 팩토리에 위임)
        PostCountProjection projection = projectionFactory.createCountProjectionForViewCount(
                post.getId(),
                post.getViewCount()
        );

        // 4. 이벤트 발행 (전문 서비스에 위임)
        eventPublisher.publishPostViewCountIncremented(post.getId(), projection);
    }

    /**
     * Post 조회
     */
    private Post getPost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
