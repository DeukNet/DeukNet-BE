package org.example.deuknetapplication.usecase.comment;

import org.example.deuknetdomain.common.vo.Content;

import java.util.UUID;

public interface CreateCommentUseCase {
    UUID createComment(CreateCommentCommand command);
    
    record CreateCommentCommand(
            UUID postId,
            Content content,
            UUID parentCommentId
    ) {}
}
