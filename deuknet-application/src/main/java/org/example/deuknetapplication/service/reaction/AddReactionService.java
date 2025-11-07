package org.example.deuknetapplication.service.reaction;

import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.reaction.AddReactionUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.example.deuknetdomain.domain.reaction.Reaction;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.reaction.TargetType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Reaction 추가 서비스
 *
 * 책임:
 * - Reaction 생성 (LIKE, DISLIKE, VIEW)
 * - PostCountProjection 업데이트 이벤트 발행
 *
 * VIEW(조회수)도 Reaction으로 통일하여 처리합니다.
 */
@Service
@Transactional
public class AddReactionService implements AddReactionUseCase {

    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;
    private final ReactionProjectionFactory projectionFactory;

    public AddReactionService(
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
    public UUID addReaction(AddReactionCommand command) {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        Reaction reaction = Reaction.create(
                command.reactionType(),
                TargetType.POST,
                command.targetId(),
                currentUserId
        );

        reactionRepository.save(reaction);

        // Reaction 타입별로 적절한 Projection 생성 및 이벤트 발행
        publishReactionEvent(command.targetId(), command.reactionType());

        return reaction.getId();
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

        dataChangeEventPublisher.publish(EventType.REACTION_ADDED, targetId, projection);
    }
}
