package org.example.deuknetapplication.service.reaction;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.reaction.RemoveReactionUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostCountProjection;
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
 * - PostCountProjection 업데이트 이벤트 발행
 */
@Service
@Transactional
public class RemoveReactionService implements RemoveReactionUseCase {

    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;
    private final ReactionProjectionFactory projectionFactory;

    public RemoveReactionService(
            ReactionRepository reactionRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher,
            ReactionProjectionFactory projectionFactory) {
        this.reactionRepository = reactionRepository;
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
     * Reaction 타입에 따라 적절한 PostCountProjection 생성 및 이벤트 발행
     */
    private void publishReactionEvent(UUID targetId, ReactionType reactionType) {
        PostCountProjection projection = switch (reactionType) {
            case VIEW -> {
                long viewCount = reactionRepository.countByTargetIdAndReactionType(targetId, ReactionType.VIEW);
                yield projectionFactory.createCountProjectionForView(targetId, viewCount);
            }
            case LIKE -> {
                long likeCount = reactionRepository.countByTargetIdAndReactionType(targetId, ReactionType.LIKE);
                yield projectionFactory.createCountProjectionForLike(targetId, likeCount);
            }
            case DISLIKE -> {
                long dislikeCount = reactionRepository.countByTargetIdAndReactionType(targetId, ReactionType.DISLIKE);
                yield projectionFactory.createCountProjectionForDislike(targetId, dislikeCount);
            }
        };

        dataChangeEventPublisher.publish("ReactionRemoved", targetId, projection);
    }
}
