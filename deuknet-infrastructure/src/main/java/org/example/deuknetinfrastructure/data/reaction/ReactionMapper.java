package org.example.deuknetinfrastructure.data.reaction;

import org.example.deuknetdomain.domain.reaction.Reaction;
import org.springframework.stereotype.Component;

@Component
public class ReactionMapper {
    
    public Reaction toDomain(ReactionEntity entity) {
        if (entity == null) return null;
        
        return Reaction.restore(
                entity.getId(),
                entity.getReactionType(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getUserId(),
                entity.getCreatedAt()
        );
    }
    
    public ReactionEntity toEntity(Reaction domain) {
        if (domain == null) return null;
        
        return new ReactionEntity(
                domain.getId(),
                domain.getReactionType(),
                domain.getTargetType(),
                domain.getTargetId(),
                domain.getUserId(),
                domain.getCreatedAt()
        );
    }
}
