package org.example.deuknetapplication.port.in.comment;

import java.util.UUID;

public interface DeleteCommentUseCase {
    void deleteComment(UUID commentId);
}
