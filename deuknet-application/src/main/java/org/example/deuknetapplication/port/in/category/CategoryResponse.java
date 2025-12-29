package org.example.deuknetapplication.port.in.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.deuknetdomain.domain.category.Category;

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

    /**
     * Domain 객체로부터 Response 생성
     */
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName().getValue(),
                category.getParentCategoryId().orElse(null),
                category.getDescription(),
                category.getThumbnailImageUrl(),
                category.getOwnerId()
        );
    }
}
