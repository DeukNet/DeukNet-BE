package org.example.deuknetapplication.port.in.post;

import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.common.vo.Title;

import java.util.List;
import java.util.UUID;

public interface CreatePostUseCase {
    UUID createPost(CreatePostCommand command);
    
    record CreatePostCommand(
            Title title,
            Content content,
            List<UUID> categoryIds
    ) {}
}
