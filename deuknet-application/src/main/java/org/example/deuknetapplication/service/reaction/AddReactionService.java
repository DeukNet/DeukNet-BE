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
 * - LIKE/DISLIKE 배타적 처리 (둘 중 하나만 가능)
 * - PostCountProjection 업데이트 이벤트 발행
 *
 * VIEW(조회수)도 Reaction으로 통일하여 처리합니다.
 * LIKE와 DISLIKE는 배타적이므로, 하나를 누르면 반대쪽이 자동으로 삭제됩니다.
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
        // 항상 현재 인증된 사용자의 ID 사용 (보안)
        UUID currentUserId = currentUserPort.getCurrentUserId();

        // 모든 Reaction 타입에 대해 중복 체크
        boolean alreadyReacted = reactionRepository.existsByTargetIdAndUserIdAndReactionType(
                command.targetId(),
                currentUserId,
                command.reactionType()
        );

        if (alreadyReacted) {
            throw new org.example.deuknetdomain.domain.reaction.exception.DuplicateReactionException();
        }

        // LIKE/DISLIKE는 배타적: 반대 반응이 있으면 먼저 삭제 (VIEW는 해당 없음)
        if (command.reactionType() == ReactionType.LIKE || command.reactionType() == ReactionType.DISLIKE) {
            ReactionType oppositeType = command.reactionType() == ReactionType.LIKE
                ? ReactionType.DISLIKE
                : ReactionType.LIKE;

            reactionRepository.findByTargetIdAndUserIdAndReactionType(
                    command.targetId(),
                    currentUserId,
                    oppositeType
            ).ifPresent(existingReaction -> {
                reactionRepository.delete(existingReaction);
                // 반대 반응 삭제 이벤트 발행
                publishReactionEvent(command.targetId(), oppositeType);
            });
        }

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
