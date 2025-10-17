package org.example.deuknetapplication.port.in.comment;

import java.util.UUID;

public interface CreateCommentUseCase {
    UUID createComment(CreateCommentCommand command);
}
