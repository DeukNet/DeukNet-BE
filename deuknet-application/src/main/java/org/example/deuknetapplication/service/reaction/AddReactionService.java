package org.example.deuknetapplication.service.reaction;

import org.example.deuknetapplication.event.reaction.ReactionAddedEvent;
import org.example.deuknetapplication.port.in.reaction.AddReactionUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.reaction.Reaction;
import org.example.deuknetdomain.domain.reaction.TargetType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AddReactionService implements AddReactionUseCase {

    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public AddReactionService(
            ReactionRepository reactionRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher) {
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public void addReaction(AddReactionCommand command) {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        Reaction reaction = Reaction.create(
                command.reactionType(),
                TargetType.POST,
                command.targetId(),
                currentUserId
        );

        reactionRepository.save(reaction);

        // Event Sourcing: 발생한 사실(fact)을 이벤트로 기록
        publishReactionAddedEvent(reaction);
    }

    private void publishReactionAddedEvent(Reaction reaction) {
        // Event에는 발생한 사실만 담음 (집계 정보 없음!)
        ReactionAddedEvent event = ReactionAddedEvent.builder()
                .reactionId(reaction.getId())
                .targetId(reaction.getTargetId())
                .reactionType(reaction.getReactionType())
                .userId(reaction.getUserId())
                .occurredAt(LocalDateTime.now())
                .build();

        // Event를 Outbox에 저장하여 Event Handler가 처리하도록 함
        dataChangeEventPublisher.publish("ReactionAdded", reaction.getTargetId(), event);
    }
}
