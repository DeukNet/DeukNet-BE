package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.UpdateCategoryCommand;
import org.example.deuknetapplication.port.in.category.UpdateCategoryUseCase;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.common.exception.BusinessException;
import org.example.deuknetdomain.common.exception.CommonErrorCode;
import org.example.deuknetdomain.common.exception.EntityNotFoundException;
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
        Category category = categoryRepository.findById(command.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category"));

        categoryRepository.findByName(command.getName())
                .ifPresent(c -> {
                    if (!c.getId().equals(command.getCategoryId())) {
                        throw new BusinessException(CommonErrorCode.DUPLICATE_RESOURCE);
                    }
                });

        // 이름만 변경 가능 (parentCategory는 불변)
        category.changeName(org.example.deuknetdomain.common.vo.CategoryName.of(command.getName()));

        categoryRepository.save(category);
    }
}
