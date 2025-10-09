package org.example.deuknetinfrastructure.data.command.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaPostCategoryAssignmentRepository extends JpaRepository<PostCategoryAssignmentEntity, UUID> {
    void deleteByPostId(UUID postId);
    List<PostCategoryAssignmentEntity> findByPostId(UUID postId);
}
