package org.example.deuknetapplication.usecase.comment;

import org.example.deuknetdomain.common.vo.Content;

import java.util.UUID;

public interface UpdateCommentUseCase {
    void updateComment(UpdateCommentCommand command);
    
    record UpdateCommentCommand(
            UUID commentId,
            Content content
    ) {}
}
