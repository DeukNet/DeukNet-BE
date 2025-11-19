package org.example.deuknetinfrastructure.data.reaction;

import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaReactionRepository extends JpaRepository<ReactionEntity, UUID> {

    /**
     * 특정 타겟의 특정 타입 반응 수 조회
     */
    long countByTargetIdAndReactionType(UUID targetId, ReactionType reactionType);

    /**
     * 특정 사용자가 특정 타겟에 특정 타입의 반응을 이미 했는지 조회
     */
    java.util.Optional<ReactionEntity> findByTargetIdAndUserIdAndReactionType(
            UUID targetId, UUID userId, ReactionType reactionType);

    /**
     * 특정 사용자가 특정 타겟에 특정 타입의 반응을 이미 했는지 확인
     */
    boolean existsByTargetIdAndUserIdAndReactionType(
            UUID targetId, UUID userId, ReactionType reactionType);
}
