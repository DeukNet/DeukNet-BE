package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.DeleteCategoryUseCase;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.model.command.category.Category;
import org.example.deuknetdomain.model.command.category.exception.CategoryHasChildrenException;
import org.example.deuknetdomain.model.command.category.exception.CategoryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DeleteCategoryService implements DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public DeleteCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void deleteCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        List<Category> children = categoryRepository.findByParentCategoryId(categoryId);
        if (!children.isEmpty()) {
            throw new CategoryHasChildrenException();
        }

        categoryRepository.delete(category);
    }
}
