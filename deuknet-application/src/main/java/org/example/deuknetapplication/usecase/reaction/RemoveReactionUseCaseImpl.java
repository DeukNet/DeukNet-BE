package org.example.deuknetapplication.usecase.reaction;

import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.reaction.Reaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class RemoveReactionUseCaseImpl implements RemoveReactionUseCase {

    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;

    public RemoveReactionUseCaseImpl(ReactionRepository reactionRepository, CurrentUserPort currentUserPort) {
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void removeReaction(UUID reactionId) {
        Reaction reaction = reactionRepository.findById(reactionId)
                .orElseThrow(() -> new IllegalArgumentException("Reaction not found"));
        
        if (!reaction.getUserId().equals(currentUserPort.getCurrentUserId())) {
            throw new IllegalArgumentException("Not authorized to remove this reaction");
        }
        
        reactionRepository.delete(reaction);
    }
}
