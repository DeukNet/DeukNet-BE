package org.example.deuknetapplication.port.in.reaction;

import org.example.deuknetdomain.model.command.reaction.ReactionType;

import java.util.UUID;

public interface AddReactionUseCase {
    void addReaction(AddReactionCommand command);
    
    record AddReactionCommand(
            UUID targetId,
            ReactionType reactionType
    ) {}
}
