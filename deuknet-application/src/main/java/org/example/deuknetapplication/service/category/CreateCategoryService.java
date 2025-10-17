package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.CreateCategoryApplicationRequest;
import org.example.deuknetapplication.port.in.category.CreateCategoryUseCase;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.common.exception.BusinessException;
import org.example.deuknetdomain.common.exception.CommonErrorCode;
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
    public UUID createCategory(CreateCategoryApplicationRequest request) {
        categoryRepository.findByName(request.getName())
                .ifPresent(c -> {
                    throw new BusinessException(CommonErrorCode.DUPLICATE_RESOURCE);
                });

        Category category = Category.create(
                org.example.deuknetdomain.common.vo.CategoryName.of(request.getName()),
                request.getParentCategoryId()
        );

        category = categoryRepository.save(category);
        return category.getId();
    }
}
