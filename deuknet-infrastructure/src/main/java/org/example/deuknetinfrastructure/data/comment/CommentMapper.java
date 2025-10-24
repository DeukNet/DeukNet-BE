package org.example.deuknetinfrastructure.data.comment;

import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.domain.comment.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    
    public Comment toDomain(CommentEntity entity) {
        if (entity == null) return null;
        
        return Comment.restore(
                entity.getId(),
                entity.getPostId(),
                entity.getAuthorId(),
                Content.from(entity.getContent()),
                entity.getParentCommentId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    
    public CommentEntity toEntity(Comment domain) {
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
