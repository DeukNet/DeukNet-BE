package org.example.deuknetinfrastructure.data.command.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
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
}
