package org.example.deuknetapplication.service.reaction;

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
    public UUID addReaction(AddReactionCommand command) {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        Reaction reaction = Reaction.create(
                command.reactionType(),
                TargetType.POST,
                command.targetId(),
                currentUserId
        );

        reactionRepository.save(reaction);

        // Outbox에 저장: PostCountProjection (likeCount만 업데이트)
        long likeCount = reactionRepository.countByTargetIdAndReactionType(
                command.targetId(), ReactionType.LIKE);

        PostCountProjection projection = PostCountProjection.builder()
                .id(command.targetId())
                .likeCount(likeCount)
                .build();

        dataChangeEventPublisher.publish("ReactionAdded", command.targetId(), projection);

        return reaction.getId();
    }
}
