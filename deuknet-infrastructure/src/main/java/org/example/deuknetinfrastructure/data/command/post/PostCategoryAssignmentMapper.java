package org.example.deuknetinfrastructure.data.command.post;

import org.example.deuknetdomain.model.command.post.PostCategoryAssignment;
import org.springframework.stereotype.Component;

@Component
public class PostCategoryAssignmentMapper {
    
    public PostCategoryAssignment toDomain(PostCategoryAssignmentEntity entity) {
        if (entity == null) return null;
        
        return PostCategoryAssignment.restore(
                entity.getId(),
                entity.getPostId(),
                entity.getCategoryId()
        );
    }
    
    public PostCategoryAssignmentEntity toEntity(PostCategoryAssignment domain) {
        if (domain == null) return null;
        
        return new PostCategoryAssignmentEntity(
                domain.getId(),
                domain.getPostId(),
                domain.getCategoryId()
        );
    }
}
