package org.example.deuknetapplication.port.in.category;

import java.util.UUID;

public interface CreateCategoryUseCase {
    UUID createCategory(CreateCategoryApplicationRequest request);
}
