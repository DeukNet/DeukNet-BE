package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.CategoryResponse;
import org.example.deuknetapplication.port.in.category.GetAllCategoriesUseCase;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.domain.category.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetAllCategoriesService implements GetAllCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public GetAllCategoriesService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse toResponse(Category category) {
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
