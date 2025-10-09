package org.example.deuknetapplication.usecase.category;

import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.model.command.category.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateCategoryUseCaseImpl implements CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCaseImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public UUID createCategory(CreateCategoryCommand command) {
        categoryRepository.findByName(command.name().getValue())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Category name already exists: " + command.name().getValue());
                });
        
        Category category = Category.create(
                command.name(),
                command.parentCategoryId()
        );
        
        category = categoryRepository.save(category);
        return category.getId();
    }
}
