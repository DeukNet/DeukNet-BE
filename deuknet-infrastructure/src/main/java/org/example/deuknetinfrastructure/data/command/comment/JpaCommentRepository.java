package org.example.deuknetinfrastructure.data.command.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCommentRepository extends JpaRepository<CommentEntity, UUID> {
}
