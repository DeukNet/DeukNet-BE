package org.example.deuknetinfrastructure.data.command.post;

import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.common.vo.Title;
import org.example.deuknetdomain.model.command.post.post.Post;
import org.example.deuknetdomain.model.command.post.post.PostStatus;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {
    
    public Post toDomain(PostEntity entity) {
        if (entity == null) return null;
        
        return Post.restore(
                entity.getId(),
                Title.from(entity.getTitle()),
                Content.from(entity.getContent()),
                entity.getAuthorId(),
                PostStatus.valueOf(entity.getStatus()),
                entity.getViewCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    
    public PostEntity toEntity(Post domain) {
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
