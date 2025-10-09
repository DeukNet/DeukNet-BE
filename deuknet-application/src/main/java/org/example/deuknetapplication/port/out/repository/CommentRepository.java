package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.model.command.comment.Comment;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {
    Comment save(Comment comment);
    Optional<Comment> findById(UUID id);
    void delete(Comment comment);
}
