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

@Service
@Transactional
public class RemoveReactionService implements RemoveReactionUseCase {

    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public RemoveReactionService(
            ReactionRepository reactionRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher) {
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public void removeReaction(UUID reactionId) {
        Reaction reaction = reactionRepository.findById(reactionId)
                .orElseThrow(ResourceNotFoundException::new);

        if (!reaction.getUserId().equals(currentUserPort.getCurrentUserId())) {
            throw new OwnerMismatchException();
        }

        // 삭제 전에 targetId 보관 (삭제 후에는 조회 불가)
        UUID targetId = reaction.getTargetId();

        reactionRepository.delete(reaction);

        // Outbox에 저장: PostCountProjection (likeCount만 업데이트)rmsep
        long likeCount = reactionRepository.countByTargetIdAndReactionType(
                targetId, ReactionType.LIKE);

        PostCountProjection projection = PostCountProjection.builder()
                .id(targetId)
                .likeCount(likeCount)
                .build();

        dataChangeEventPublisher.publish("ReactionRemoved", targetId, projection);
    }
}
