package org.example.deuknetapplication.usecase.reaction;

import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.reaction.Reaction;
import org.example.deuknetdomain.model.command.reaction.TargetType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AddReactionUseCaseImpl implements AddReactionUseCase {

    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;

    public AddReactionUseCaseImpl(ReactionRepository reactionRepository, CurrentUserPort currentUserPort) {
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
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
    }
}
