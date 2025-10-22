package org.example.deuknetinfrastructure.data.command.reaction;

import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaReactionRepository extends JpaRepository<ReactionEntity, UUID> {

    /**
     * 특정 타겟의 특정 타입 반응 수 조회
     */
    long countByTargetIdAndReactionType(UUID targetId, ReactionType reactionType);
}
