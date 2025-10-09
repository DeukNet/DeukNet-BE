package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.model.command.post.postcategory.PostCategoryAssignment;

import java.util.List;
import java.util.UUID;

public interface PostCategoryAssignmentRepository {
    PostCategoryAssignment save(PostCategoryAssignment assignment);
    void deleteByPostId(UUID postId);
    List<PostCategoryAssignment> findByPostId(UUID postId);
}
