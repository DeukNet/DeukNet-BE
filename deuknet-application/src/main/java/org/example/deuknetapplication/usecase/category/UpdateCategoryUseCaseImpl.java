package org.example.deuknetapplication.usecase.category;

import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.model.command.category.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateCategoryUseCaseImpl implements UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public UpdateCategoryUseCaseImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void updateCategory(UpdateCategoryCommand command) {
        Category category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        categoryRepository.findByName(command.name().getValue())
                .ifPresent(c -> {
                    if (!c.getId().equals(command.categoryId())) {
                        throw new IllegalArgumentException("Category name already exists: " + command.name().getValue());
                    }
                });
        
        // 이름만 변경 가능 (parentCategory는 불변)
        category.changeName(command.name());
        
        categoryRepository.save(category);
    }
}
