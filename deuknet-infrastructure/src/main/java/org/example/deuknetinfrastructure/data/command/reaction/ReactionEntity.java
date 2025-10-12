package org.example.deuknetinfrastructure.data.command.reaction;

import jakarta.persistence.*;
import org.example.deuknetdomain.model.command.reaction.ReactionType;
import org.example.deuknetdomain.model.command.reaction.TargetType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reactions")
public class ReactionEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 20)
    private ReactionType reactionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;
    
    @Column(name = "target_id", nullable = false, columnDefinition = "UUID")
    private UUID targetId;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ReactionEntity() {
    }

    public ReactionEntity(UUID id, ReactionType reactionType, TargetType targetType, 
                         UUID targetId, UUID userId, LocalDateTime createdAt) {
        this.id = id;
        this.reactionType = reactionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ReactionType getReactionType() {
        return reactionType;
    }

    public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
