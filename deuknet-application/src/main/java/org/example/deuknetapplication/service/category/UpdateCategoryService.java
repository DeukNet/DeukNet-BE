package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.UpdateCategoryUseCase;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.model.command.category.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateCategoryService implements UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public UpdateCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void updateCategory(UpdateCategoryCommand command) {
        Category category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        // 이름 중복 체크 (자기 자신 제외)
        categoryRepository.findByName(command.name().getValue())
                .ifPresent(c -> {
                    if (!c.getId().equals(command.categoryId())) {
                        throw new IllegalArgumentException("Category with name already exists");
                    }
                });
        
        // parentCategoryId가 변경되는 경우 새로 생성
        if (!category.getParentCategoryId().equals(java.util.Optional.ofNullable(command.parentCategoryId()))) {
            throw new IllegalArgumentException("Cannot change parent category. Please delete and recreate.");
        }
        
        category.changeName(command.name());
        categoryRepository.save(category);
    }
}
