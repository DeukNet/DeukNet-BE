package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.model.command.reaction.Reaction;
import org.example.deuknetdomain.model.command.reaction.ReactionType;

import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository {
    Reaction save(Reaction reaction);
    Optional<Reaction> findById(UUID id);
    void delete(Reaction reaction);

    /**
     * 특정 타겟의 특정 타입 반응 수 조회
     */
    long countByTargetIdAndReactionType(UUID targetId, ReactionType reactionType);
}
