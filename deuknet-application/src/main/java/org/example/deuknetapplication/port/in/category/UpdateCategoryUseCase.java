package org.example.deuknetapplication.port.in.category;

import java.util.UUID;

public interface UpdateCategoryUseCase {
    void updateCategory(UUID categoryId, UpdateCategoryApplicationRequest request);
}
