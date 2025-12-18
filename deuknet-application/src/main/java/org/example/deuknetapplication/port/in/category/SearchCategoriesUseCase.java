package org.example.deuknetapplication.port.in.category;

import org.example.deuknetapplication.port.in.post.PageResponse;

public interface SearchCategoriesUseCase {
    PageResponse<CategoryResponse> searchCategories(String keyword, int page, int size);
}
