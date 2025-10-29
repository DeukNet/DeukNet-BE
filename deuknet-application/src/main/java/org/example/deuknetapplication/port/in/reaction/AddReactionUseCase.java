package org.example.deuknetapplication.port.in.reaction;

import org.example.deuknetdomain.domain.reaction.ReactionType;

import java.util.UUID;

public interface AddReactionUseCase {
    UUID addReaction(AddReactionCommand command);

    record AddReactionCommand(
            UUID targetId,
            ReactionType reactionType
    ) {}
}
