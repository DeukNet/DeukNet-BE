package org.example.deuknetapplication.usecase.category;

import org.example.deuknetdomain.common.vo.CategoryName;

import java.util.UUID;

public interface CreateCategoryUseCase {
    UUID createCategory(CreateCategoryCommand command);
    
    record CreateCategoryCommand(
            CategoryName name,
            UUID parentCategoryId
    ) {}
}
