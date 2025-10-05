package org.example.deuknetinfrastructure.persistence.mapper;

import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.common.vo.Title;
import org.example.deuknetdomain.model.command.post.post.Post;
import org.example.deuknetdomain.model.command.post.post.PostStatus;
import org.example.deuknetinfrastructure.persistence.entity.PostEntity;

public class PostMapper {
    
    public static Post toDomain(PostEntity entity) {
        if (entity == null) return null;
        
        return Post.restore(
                entity.getId(),
                Title.of(entity.getTitle()),
                Content.of(entity.getContent()),
                entity.getAuthorId(),
                PostStatus.valueOf(entity.getStatus()),
                entity.getViewCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    
    public static PostEntity toEntity(Post domain) {
        if (domain == null) return null;
        
        return new PostEntity(
                domain.getId(),
                domain.getTitle().getValue(),
                domain.getContent().getValue(),
                domain.getAuthorId(),
                domain.getStatus().name(),
                domain.getViewCount(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
