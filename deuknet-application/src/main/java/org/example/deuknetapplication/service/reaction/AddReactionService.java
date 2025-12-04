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

import java.util.Optional;
import java.util.UUID;

/**
 * Reaction 추가 서비스
 *
 * 책임:
 * - Reaction 생성 (LIKE, DISLIKE, VIEW)
 * - LIKE/DISLIKE 배타적 처리 (둘 중 하나만 가능)
 * - PostCountProjection 업데이트 이벤트 발행
 *
 * 참고:
 * - VIEW(조회수)는 중복이 허용됩니다 (동시 요청 시 여러 개 생성 가능)
 * - LIKE/DISLIKE는 배타적이므로, 하나를 누르면 반대쪽이 자동으로 삭제됩니다
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

        // VIEW는 중복을 허용하므로, 별도 체크 없이 바로 생성
        if (command.reactionType() == ReactionType.VIEW) {
            Reaction reaction = Reaction.create(
                    command.reactionType(),
                    TargetType.POST,
                    command.targetId(),
                    currentUserId
            );
            reactionRepository.save(reaction);
            publishReactionEvent(command.targetId(), command.reactionType());
            return reaction.getId();
        }

        // LIKE/DISLIKE는 배타적: 반대 반응이 있으면 먼저 삭제
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

        // 같은 타입의 LIKE/DISLIKE가 이미 있는지 확인
        Optional<Reaction> existing = reactionRepository.findByTargetIdAndUserIdAndReactionType(
                command.targetId(),
                currentUserId,
                command.reactionType()
        );

        // 이미 있으면 기존 ID 반환
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        // 없으면 새로 생성
        Reaction reaction = Reaction.create(
                command.reactionType(),
                TargetType.POST,
                command.targetId(),
                currentUserId
        );

        reactionRepository.save(reaction);
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
