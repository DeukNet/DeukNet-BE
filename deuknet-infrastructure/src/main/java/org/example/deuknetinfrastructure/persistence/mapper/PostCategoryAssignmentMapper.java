package org.example.deuknetinfrastructure.persistence.mapper;

import org.example.deuknetdomain.model.command.post.post.Post;
import org.example.deuknetdomain.model.command.post.postcategory.PostCategoryAssignment;
import org.example.deuknetinfrastructure.persistence.entity.PostCategoryAssignmentEntity;
import org.example.deuknetinfrastructure.persistence.entity.PostEntity;

public class PostCategoryAssignmentMapper {
    
    public static PostCategoryAssignment toDomain(PostCategoryAssignmentEntity entity, Post post) {
        if (entity == null) return null;
        
        return PostCategoryAssignment.restore(
                entity.getId(),
                post,
                entity.getCategoryId()
        );
    }
    
    public static PostCategoryAssignmentEntity toEntity(PostCategoryAssignment domain) {
        if (domain == null) return null;
        
        return new PostCategoryAssignmentEntity(
                domain.getId(),
                domain.getPost().getId(),
                domain.getCategoryId()
        );
    }
    
    public static PostCategoryAssignment toDomainWithPostEntity(PostCategoryAssignmentEntity entity, PostEntity postEntity) {
        if (entity == null || postEntity == null) return null;
        
        Post post = PostMapper.toDomain(postEntity);
        return toDomain(entity, post);
    }
}
