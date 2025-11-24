package org.example.deuknetapplication.port.in.comment;

import java.util.List;
import java.util.UUID;

public interface GetCommentsUseCase {
    List<CommentResponse> getCommentsByPostId(UUID postId);
}
