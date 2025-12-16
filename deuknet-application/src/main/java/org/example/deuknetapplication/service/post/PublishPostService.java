package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.post.PublishPostUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Post 발행 서비스 (SRP 준수)
 *
 * 책임:
 * - Post 발행 권한 검증
 * - Post 상태를 PUBLIC으로 변경
 * - 발행 이벤트 발행 (Outbox Pattern)
 */
@Service
@Transactional
public class PublishPostService implements PublishPostUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final PostProjectionFactory postProjectionFactory;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public PublishPostService(
            PostRepository postRepository,
            UserRepository userRepository,
            CommentRepository commentRepository,
            ReactionRepository reactionRepository,
            CurrentUserPort currentUserPort,
            PostProjectionFactory postProjectionFactory,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
        this.postProjectionFactory = postProjectionFactory;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public void publishPost(UUID postId) {
        // 1. Post 조회 및 권한 검증
        Post post = getPostAndVerifyOwnership(postId);

        // 2. Post 발행 (상태 변경)
        publishPostAggregate(post);

        // 3. 발행 이벤트 발행
        publishPostPublishedEvent(post);
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
     * Post 발행 (SRP: Post 상태 변경 책임 분리)
     */
    private void publishPostAggregate(Post post) {
        post.publish();
        postRepository.save(post);
    }

    /**
     * PostPublished 이벤트 발행 (SRP: 이벤트 발행 책임 분리)
     */
    private void publishPostPublishedEvent(Post post) {
        // 현재 통계 조회
        long commentCount = commentRepository.countByPostId(post.getId());
        long likeCount = reactionRepository.countByTargetIdAndReactionType(
                post.getId(), ReactionType.LIKE);
        long dislikeCount = reactionRepository.countByTargetIdAndReactionType(
                post.getId(), ReactionType.DISLIKE);
        long viewCount = reactionRepository.countByTargetIdAndReactionType(
                post.getId(), ReactionType.VIEW);

        // PostProjectionFactory를 사용하여 Projection 생성 (일관성 유지)
        PostDetailProjection detailProjection = postProjectionFactory.createDetailProjectionForUpdate(
                post, post.getCategoryId(), commentCount, likeCount, dislikeCount, viewCount
        );

        dataChangeEventPublisher.publish(EventType.POST_PUBLISHED, post.getId(), detailProjection);
    }
}
