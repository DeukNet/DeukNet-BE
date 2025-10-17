package org.example.deuknetinfrastructure.data.command.category;

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

    public CategoryEntity() {
    }

    public CategoryEntity(UUID id, String name, UUID parentCategoryId) {
        this.id = id;
        this.name = name;
        this.parentCategoryId = parentCategoryId;
    }
}
