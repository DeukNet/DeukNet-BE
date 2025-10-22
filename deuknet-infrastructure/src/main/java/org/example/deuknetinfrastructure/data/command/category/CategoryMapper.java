package org.example.deuknetinfrastructure.data.command.category;

import org.example.deuknetdomain.common.vo.CategoryName;
import org.example.deuknetdomain.domain.category.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    
    public Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;
        
        return Category.restore(
                entity.getId(),
                CategoryName.of(entity.getName()),
                entity.getParentCategoryId()
        );
    }
    
    public CategoryEntity toEntity(Category domain) {
        if (domain == null) return null;
        
        return new CategoryEntity(
                domain.getId(),
                domain.getName().getValue(),
                domain.getParentCategoryId().orElse(null)
        );
    }
}
