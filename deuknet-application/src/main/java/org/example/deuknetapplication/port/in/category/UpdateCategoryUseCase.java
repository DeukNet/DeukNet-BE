package org.example.deuknetapplication.port.in.category;

import org.example.deuknetdomain.common.vo.CategoryName;

import java.util.UUID;

public interface UpdateCategoryUseCase {
    void updateCategory(UpdateCategoryCommand command);
    
    record UpdateCategoryCommand(
            UUID categoryId,
            CategoryName name
    ) {}
}
