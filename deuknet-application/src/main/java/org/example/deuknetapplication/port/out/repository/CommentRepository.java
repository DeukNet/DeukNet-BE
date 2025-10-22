package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.domain.comment.Comment;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {
    Comment save(Comment comment);
    Optional<Comment> findById(UUID id);
    void delete(Comment comment);

    /**
     * 특정 게시글의 댓글 수 조회
     */
    long countByPostId(UUID postId);
}
