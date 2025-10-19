package org.example.deuknetapplication.service.reaction;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.reaction.RemoveReactionUseCase;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
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
                .orElseThrow(ResourceNotFoundException::new);

        if (!reaction.getUserId().equals(currentUserPort.getCurrentUserId())) {
            throw new OwnerMismatchException();
        }

        reactionRepository.delete(reaction);
    }
}
