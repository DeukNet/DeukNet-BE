package org.example.deuknetinfrastructure.data.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCommentRepository extends JpaRepository<CommentEntity, UUID> {

    /**
     * 특정 게시글의 댓글 수 조회
     */
    long countByPostId(UUID postId);
}
