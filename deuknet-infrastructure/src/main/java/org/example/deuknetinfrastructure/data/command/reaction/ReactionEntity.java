package org.example.deuknetinfrastructure.data.command.reaction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.deuknetdomain.model.command.reaction.ReactionType;
import org.example.deuknetdomain.model.command.reaction.TargetType;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
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
}
