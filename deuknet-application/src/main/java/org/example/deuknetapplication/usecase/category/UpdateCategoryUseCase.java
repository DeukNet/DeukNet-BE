package org.example.deuknetapplication.usecase.category;

import org.example.deuknetdomain.common.vo.CategoryName;

import java.util.UUID;

public interface UpdateCategoryUseCase {
    void updateCategory(UpdateCategoryCommand command);
    
    record UpdateCategoryCommand(
            UUID categoryId,
            CategoryName name
    ) {}
}
