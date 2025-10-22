package org.example.deuknetdomain.domain.reaction;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.AggregateRoot;

@Getter
public class Reaction extends AggregateRoot {

    private final ReactionType reactionType;
    private final TargetType targetType;
    private final UUID targetId;
    private final UUID userId;
    private final LocalDateTime createdAt;

    private Reaction(UUID id, ReactionType reactionType, TargetType targetType, 
                    UUID targetId, UUID userId, LocalDateTime createdAt) {
        super(id);
        this.reactionType = reactionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static Reaction create(ReactionType reactionType, TargetType targetType, UUID targetId, UUID userId) {
        return new Reaction(
                UUID.randomUUID(),
                reactionType,
                targetType,
                targetId,
                userId,
                LocalDateTime.now()
        );
    }

    public static Reaction restore(UUID id, ReactionType reactionType, TargetType targetType,
                                  UUID targetId, UUID userId, LocalDateTime createdAt) {
        return new Reaction(id, reactionType, targetType, targetId, userId, createdAt);
    }

    public boolean isLike() {
        return reactionType == ReactionType.LIKE;
    }

    public boolean isPostReaction() {
        return targetType == TargetType.POST;
    }

}
