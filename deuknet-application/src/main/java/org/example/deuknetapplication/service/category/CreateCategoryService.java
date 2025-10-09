package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.CreateCategoryUseCase;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.model.command.category.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateCategoryService implements CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public UUID createCategory(CreateCategoryCommand command) {
        // 중복 체크
        categoryRepository.findByName(command.name().getValue())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Category with name already exists");
                });
        
        // Category 생성
        Category category = Category.create(
                command.name(),
                command.parentCategoryId()
        );
        
        category = categoryRepository.save(category);
        return category.getId();
    }
}
