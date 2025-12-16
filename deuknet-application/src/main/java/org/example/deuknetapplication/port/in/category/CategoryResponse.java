package org.example.deuknetapplication.port.in.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private UUID id;
    private String name;
    private UUID parentCategoryId;
    private String description;
    private String thumbnailImageUrl;
    private UUID ownerId;
}
