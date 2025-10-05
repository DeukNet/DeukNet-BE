package org.example.deuknetinfrastructure.persistence.mapper;

import org.example.deuknetdomain.model.command.reaction.Reaction;
import org.example.deuknetdomain.model.command.reaction.ReactionType;
import org.example.deuknetdomain.model.command.reaction.TargetType;
import org.example.deuknetinfrastructure.persistence.entity.ReactionEntity;

public class ReactionMapper {
    
    public static Reaction toDomain(ReactionEntity entity) {
        if (entity == null) return null;
        
        return Reaction.restore(
                entity.getId(),
                ReactionType.valueOf(entity.getReactionType()),
                TargetType.valueOf(entity.getTargetType()),
                entity.getTargetId(),
                entity.getUserId(),
                entity.getCreatedAt()
        );
    }
    
    public static ReactionEntity toEntity(Reaction domain) {
        if (domain == null) return null;
        
        return new ReactionEntity(
                domain.getId(),
                domain.getReactionType().name(),
                domain.getTargetType().name(),
                domain.getTargetId(),
                domain.getUserId(),
                domain.getCreatedAt()
        );
    }
}
