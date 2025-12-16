package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.CreateCategoryApplicationRequest;
import org.example.deuknetapplication.port.in.category.CreateCategoryUseCase;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.category.Category;
import org.example.deuknetdomain.domain.category.exception.CategoryAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateCategoryService implements CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CurrentUserPort currentUserPort;

    public CreateCategoryService(CategoryRepository categoryRepository, CurrentUserPort currentUserPort) {
        this.categoryRepository = categoryRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public UUID createCategory(CreateCategoryApplicationRequest request) {
        // 띄어쓰기 제거 및 정규화
        String normalizedName = request.getName().replaceAll("\\s+", "");

        // 중복 확인 (띄어쓰기 제거된 이름으로)
        categoryRepository.findByName(normalizedName)
                .ifPresent(c -> {
                    throw new CategoryAlreadyExistsException();
                });

        // 현재 사용자 ID 조회
        UUID currentUserId = currentUserPort.getCurrentUserId();

        Category category = Category.create(
                org.example.deuknetdomain.common.vo.CategoryName.of(normalizedName),
                request.getParentCategoryId(),
                request.getDescription(),
                request.getThumbnailImageUrl(),
                currentUserId
        );

        category = categoryRepository.save(category);
        return category.getId();
    }
}
