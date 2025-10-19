package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.UpdateCategoryApplicationRequest;
import org.example.deuknetapplication.port.in.category.UpdateCategoryUseCase;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.model.command.category.Category;
import org.example.deuknetdomain.model.command.category.exception.CategoryAlreadyExistsException;
import org.example.deuknetdomain.model.command.category.exception.CategoryNotFoundException;
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
    public void updateCategory(UpdateCategoryApplicationRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(CategoryNotFoundException::new);

        categoryRepository.findByName(request.getName())
                .ifPresent(c -> {
                    if (!c.getId().equals(request.getCategoryId())) {
                        throw new CategoryAlreadyExistsException();
                    }
                });

        // 이름만 변경 가능 (parentCategory는 불변)
        category.changeName(org.example.deuknetdomain.common.vo.CategoryName.of(request.getName()));

        categoryRepository.save(category);
    }
}
