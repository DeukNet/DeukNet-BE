package org.example.deuknetapplication.service.reaction;

import org.example.deuknetapplication.port.in.reaction.RemoveReactionUseCase;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.common.exception.EntityNotFoundException;
import org.example.deuknetdomain.common.exception.ForbiddenException;
import org.example.deuknetdomain.model.command.reaction.Reaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class RemoveReactionService implements RemoveReactionUseCase {

    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;

    public RemoveReactionService(ReactionRepository reactionRepository, CurrentUserPort currentUserPort) {
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void removeReaction(UUID reactionId) {
        Reaction reaction = reactionRepository.findById(reactionId)
                .orElseThrow(() -> new EntityNotFoundException("Reaction"));
        
        if (!reaction.getUserId().equals(currentUserPort.getCurrentUserId())) {
            throw new ForbiddenException("Not authorized to remove this reaction");
        }
        
        reactionRepository.delete(reaction);
    }
}
