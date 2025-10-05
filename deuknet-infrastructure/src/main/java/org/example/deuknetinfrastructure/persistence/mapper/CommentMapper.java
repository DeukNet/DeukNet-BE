package org.example.deuknetinfrastructure.persistence.mapper;

import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.model.command.comment.Comment;
import org.example.deuknetinfrastructure.persistence.entity.CommentEntity;

public class CommentMapper {
    
    public static Comment toDomain(CommentEntity entity) {
        if (entity == null) return null;
        
        return Comment.restore(
                entity.getId(),
                entity.getPostId(),
                entity.getAuthorId(),
                Content.of(entity.getContent()),
                entity.getParentCommentId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    
    public static CommentEntity toEntity(Comment domain) {
        if (domain == null) return null;
        
        return new CommentEntity(
                domain.getId(),
                domain.getPostId(),
                domain.getAuthorId(),
                domain.getContent().getValue(),
                domain.getParentCommentId().orElse(null),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
