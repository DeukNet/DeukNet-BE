package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.post.DeletePostUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.post.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Post 삭제 서비스 (SRP 준수)
 *
 * 책임:
 * - Post 삭제 권한 검증
 * - Post 논리 삭제 (Soft Delete)
 * - 삭제 이벤트 발행 (Outbox Pattern)
 */
@Service
@Transactional
public class DeletePostService implements DeletePostUseCase {

    private final PostRepository postRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public DeletePostService(
            PostRepository postRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.postRepository = postRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public void deletePost(UUID postId) {
        // 1. Post 조회 및 권한 검증
        Post post = getPostAndVerifyOwnership(postId);

        // 2. Post 삭제 (Soft Delete)
        deletePostAggregate(post);

        // 3. 삭제 이벤트 발행
        publishPostDeletedEvent(post.getId());
    }

    /**
     * Post 조회 및 소유권 검증 (SRP: 권한 검증 책임 분리)
     */
    private Post getPostAndVerifyOwnership(UUID postId) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(ResourceNotFoundException::new);

        if (!post.getAuthorId().equals(currentUserId)) {
            throw new OwnerMismatchException();
        }

        return post;
    }

    /**
     * Post 삭제 (SRP: Post 삭제 책임 분리)
     *
     * Soft Delete를 수행합니다.
     */
    private void deletePostAggregate(Post post) {
        post.delete();
        postRepository.save(post);
    }

    /**
     * PostDeleted 이벤트 발행 (SRP: 이벤트 발행 책임 분리)
     *
     * Projection을 삭제하도록 이벤트를 발행합니다.
     */
    private void publishPostDeletedEvent(UUID postId) {
        dataChangeEventPublisher.publish(EventType.POST_DELETED, postId);
    }
}
