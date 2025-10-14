package org.example.deuknetapplication.port.in.comment;

import org.example.deuknetdomain.common.vo.Content;

import java.util.UUID;

public interface UpdateCommentUseCase {
    void updateComment(UpdateCommentCommand command);
    
    record UpdateCommentCommand(
            UUID commentId,
            Content content
    ) {}
}
