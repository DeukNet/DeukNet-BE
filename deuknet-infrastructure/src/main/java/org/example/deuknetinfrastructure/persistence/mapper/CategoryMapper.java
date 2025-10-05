package org.example.deuknetinfrastructure.persistence.mapper;

import org.example.deuknetdomain.common.vo.CategoryName;
import org.example.deuknetdomain.model.command.category.Category;
import org.example.deuknetinfrastructure.persistence.entity.CategoryEntity;

public class CategoryMapper {
    
    public static Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;
        
        return Category.restore(
                entity.getId(),
                CategoryName.of(entity.getName()),
                entity.getParentCategoryId()
        );
    }
    
    public static CategoryEntity toEntity(Category domain) {
        if (domain == null) return null;
        
        return new CategoryEntity(
                domain.getId(),
                domain.getName().getValue(),
                domain.getParentCategoryId().orElse(null)
        );
    }
}
