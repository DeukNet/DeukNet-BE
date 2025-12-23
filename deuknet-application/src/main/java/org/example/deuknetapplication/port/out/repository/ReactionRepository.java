package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.domain.reaction.Reaction;
import org.example.deuknetdomain.domain.reaction.ReactionType;

import java.util.List;
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

    /**
     * 특정 사용자가 특정 타겟에 특정 타입의 반응을 이미 했는지 조회
     * (중복 방지용)
     */
    Optional<Reaction> findByTargetIdAndUserIdAndReactionType(UUID targetId, UUID userId, ReactionType reactionType);

    /**
     * 특정 사용자가 특정 타겟에 특정 타입의 반응을 이미 했는지 확인
     * (중복 방지용)
     */
    boolean existsByTargetIdAndUserIdAndReactionType(UUID targetId, UUID userId, ReactionType reactionType);

    /**
     * 특정 사용자가 특정 타겟에 대한 모든 반응을 조회
     * (성능 최적화: LIKE, DISLIKE를 한 번의 쿼리로 조회)
     */
    List<Reaction> findByTargetIdAndUserId(UUID targetId, UUID userId);

    /**
     * 특정 사용자가 좋아요를 누른 게시글 ID 목록 조회
     * 최신순으로 정렬
     */
    List<UUID> findLikedPostIdsByUserId(UUID userId);
}
