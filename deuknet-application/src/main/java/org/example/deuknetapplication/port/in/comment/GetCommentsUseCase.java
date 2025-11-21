package org.example.deuknetapplication.port.in.comment;

import org.example.deuknetapplication.projection.comment.CommentProjection;

import java.util.List;
import java.util.UUID;

public interface GetCommentsUseCase {
    List<CommentProjection> getCommentsByPostId(UUID postId);
}
