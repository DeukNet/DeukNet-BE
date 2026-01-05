package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.CategoryResponse;
import org.example.deuknetapplication.port.in.category.GetCategoriesUseCase;
import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.domain.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetCategoriesService implements GetCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public GetCategoriesService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public PageResponse<CategoryResponse> getCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<CategoryResponse> content = categoryPage.getContent().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                categoryPage.getTotalElements(),
                categoryPage.getNumber(),
                categoryPage.getSize()
        );
    }
}
