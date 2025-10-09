package org.example.deuknetapplication.usecase.comment;

import java.util.UUID;

public interface DeleteCommentUseCase {
    void deleteComment(UUID commentId);
}
