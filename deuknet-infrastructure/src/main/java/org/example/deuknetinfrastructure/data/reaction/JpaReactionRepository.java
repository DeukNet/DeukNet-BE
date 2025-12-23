package org.example.deuknetinfrastructure.data.reaction;

import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.reaction.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaReactionRepository extends JpaRepository<ReactionEntity, UUID> {

    /**
     * 특정 타겟의 특정 타입 반응 수 조회
     */
    long countByTargetIdAndReactionType(UUID targetId, ReactionType reactionType);

    /**
     * 특정 사용자가 특정 타겟에 특정 타입의 반응을 이미 했는지 조회
     * 중복 데이터가 있을 경우 첫 번째 것만 반환
     */
    java.util.Optional<ReactionEntity> findFirstByTargetIdAndUserIdAndReactionType(
            UUID targetId, UUID userId, ReactionType reactionType);

    /**
     * 특정 사용자가 특정 타겟에 특정 타입의 반응을 이미 했는지 확인
     */
    boolean existsByTargetIdAndUserIdAndReactionType(
            UUID targetId, UUID userId, ReactionType reactionType);

    /**
     * 특정 사용자가 특정 타겟에 대한 모든 반응을 조회
     */
    List<ReactionEntity> findByTargetIdAndUserId(UUID targetId, UUID userId);

    /**
     * 특정 사용자가 좋아요를 누른 게시글 ID 목록 조회 (최신순)
     */
    @Query("SELECT r.targetId FROM ReactionEntity r WHERE r.userId = :userId AND r.reactionType = 'LIKE' AND r.targetType = 'POST' ORDER BY r.createdAt DESC")
    List<UUID> findLikedPostIdsByUserId(@Param("userId") UUID userId);
}
