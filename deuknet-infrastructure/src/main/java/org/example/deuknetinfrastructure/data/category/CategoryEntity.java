package org.example.deuknetinfrastructure.data.category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "categories")
public class CategoryEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "parent_category_id", columnDefinition = "UUID")
    private UUID parentCategoryId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "thumbnail_image_url", length = 500)
    private String thumbnailImageUrl;

    @Column(name = "owner_id", columnDefinition = "UUID")
    private UUID ownerId;

    public CategoryEntity() {
    }

    public CategoryEntity(UUID id, String name, UUID parentCategoryId, String description, String thumbnailImageUrl, UUID ownerId) {
        this.id = id;
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.description = description;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.ownerId = ownerId;
    }
}
