package org.example.deuknetapplication.service.reaction;

import org.example.deuknetapplication.port.in.reaction.AddReactionUseCase;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.reaction.Reaction;
import org.example.deuknetdomain.domain.reaction.TargetType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AddReactionService implements AddReactionUseCase {

    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;

    public AddReactionService(ReactionRepository reactionRepository, CurrentUserPort currentUserPort) {
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
