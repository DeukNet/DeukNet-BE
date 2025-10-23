package org.example.deuknetapplication.service.reaction;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.event.reaction.ReactionRemovedEvent;
import org.example.deuknetapplication.port.in.reaction.RemoveReactionUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.reaction.Reaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

        // 삭제 전에 정보 보관 (삭제 후에는 조회 불가)
        UUID targetId = reaction.getTargetId();
        var reactionType = reaction.getReactionType();
        UUID userId = reaction.getUserId();

        reactionRepository.delete(reaction);

        // Event Sourcing: 발생한 사실(fact)을 이벤트로 기록
        publishReactionRemovedEvent(reactionId, targetId, reactionType, userId);
    }

    private void publishReactionRemovedEvent(UUID reactionId, UUID targetId,
                                            org.example.deuknetdomain.domain.reaction.ReactionType reactionType,
                                            UUID userId) {
        // Event에는 발생한 사실만 담음 (집계 정보 없음!)
        ReactionRemovedEvent event = ReactionRemovedEvent.builder()
                .reactionId(reactionId)
                .targetId(targetId)
                .reactionType(reactionType)
                .userId(userId)
                .occurredAt(LocalDateTime.now())
                .build();

        // Event를 Outbox에 저장하여 Event Handler가 처리하도록 함
        dataChangeEventPublisher.publish("ReactionRemoved", targetId, event);
    }
}
