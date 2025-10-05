package org.example.deuknetinfrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "post_category_assignments")
public class PostCategoryAssignmentEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "post_id", nullable = false, columnDefinition = "UUID")
    private UUID postId;
    
    @Column(name = "category_id", nullable = false, columnDefinition = "UUID")
    private UUID categoryId;

    public PostCategoryAssignmentEntity() {
    }

    public PostCategoryAssignmentEntity(UUID id, UUID postId, UUID categoryId) {
        this.id = id;
        this.postId = postId;
        this.categoryId = categoryId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }
}
