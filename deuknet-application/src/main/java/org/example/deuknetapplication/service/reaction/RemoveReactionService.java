package org.example.deuknetapplication.service.reaction;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.reaction.RemoveReactionUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetapplication.service.post.PostProjectionFactory;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.reaction.Reaction;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Reaction 삭제 서비스
 *
 * 책임:
 * - Reaction 삭제 (LIKE, DISLIKE, VIEW)
 * - PostDetailProjection 업데이트 이벤트 발행
 */
@Service
@Transactional
public class RemoveReactionService implements RemoveReactionUseCase {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;
    private final PostProjectionFactory projectionFactory;

    public RemoveReactionService(
            ReactionRepository reactionRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher,
            PostProjectionFactory projectionFactory) {
        this.reactionRepository = reactionRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
        this.projectionFactory = projectionFactory;
    }

    @Override
    public void removeReaction(UUID reactionId) {
        Reaction reaction = reactionRepository.findById(reactionId)
                .orElseThrow(ResourceNotFoundException::new);

        if (!reaction.getUserId().equals(currentUserPort.getCurrentUserId())) {
            throw new OwnerMismatchException();
        }

        // 삭제 전에 정보 보관 (삭제 후에는 조회 불가)
        UUID targetId = reaction.getTargetId();
        ReactionType reactionType = reaction.getReactionType();

        reactionRepository.delete(reaction);

        // Reaction 타입별로 적절한 Projection 생성 및 이벤트 발행
        publishReactionEvent(targetId, reactionType);
    }

    /**
     * PostDetailProjection 생성 및 이벤트 발행
     * Reaction 변경 시 전체 통계를 업데이트
     */
    private void publishReactionEvent(UUID targetId, ReactionType reactionType) {
        // Post 조회
        Post post = postRepository.findById(targetId)
                .orElseThrow(ResourceNotFoundException::new);

        // 모든 통계 조회
        long commentCount = commentRepository.countByPostId(targetId);
        long likeCount = reactionRepository.countByTargetIdAndReactionType(targetId, ReactionType.LIKE);
        long dislikeCount = reactionRepository.countByTargetIdAndReactionType(targetId, ReactionType.DISLIKE);
        long viewCount = reactionRepository.countByTargetIdAndReactionType(targetId, ReactionType.VIEW);

        // PostDetailProjection 생성
        PostDetailProjection projection = projectionFactory.createDetailProjectionForUpdate(
                post,
                post.getCategoryId(),
                commentCount,
                likeCount,
                dislikeCount,
                viewCount
        );

        dataChangeEventPublisher.publish(EventType.REACTION_REMOVED, targetId, projection);
    }
}
