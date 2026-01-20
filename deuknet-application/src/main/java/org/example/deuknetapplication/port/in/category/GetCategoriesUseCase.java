package org.example.deuknetapplication.port.in.category;

import org.example.deuknetapplication.port.in.post.PageResponse;

public interface GetCategoriesUseCase {
    PageResponse<CategoryResponse> getCategories(int page, int size);
}
