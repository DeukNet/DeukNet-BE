package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.UpdateCategoryApplicationRequest;
import org.example.deuknetapplication.port.in.category.UpdateCategoryUseCase;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.category.Category;
import org.example.deuknetdomain.domain.category.exception.CategoryNotFoundException;
import org.example.deuknetdomain.domain.category.exception.CategoryUpdateNotAllowedException;
import org.example.deuknetdomain.domain.user.User;
import org.example.deuknetdomain.domain.user.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UpdateCategoryService implements UpdateCategoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateCategoryService.class);

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;

    public UpdateCategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            CurrentUserPort currentUserPort
    ) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void updateCategory(UUID categoryId, UpdateCategoryApplicationRequest request) {
        // 1. 카테고리 조회
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        // 2. 현재 사용자 조회
        UUID currentUserId = currentUserPort.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(UserNotFoundException::new);

        // 3. 권한 검증
        validateUpdatePermission(category, user);

        // 4. 설명과 썸네일 이미지 업데이트
        if (request.getDescription() != null) {
            String description = request.getDescription().trim();
            category.updateDescription(description.isEmpty() ? null : description);
        }
        if (request.getThumbnailImageUrl() != null) {
            String thumbnailImageUrl = request.getThumbnailImageUrl().trim();
            category.updateThumbnailImageUrl(thumbnailImageUrl.isEmpty() ? null : thumbnailImageUrl);
        }

        // 5. 저장
        categoryRepository.save(category);

        log.info("[CATEGORY_UPDATED] categoryId={}, name={}, updatedBy={}, description={}",
                categoryId,
                category.getName().getValue(),
                currentUserId,
                request.getDescription());
    }

    /**
     * 카테고리 수정 권한 검증
     * - ADMIN: 모든 카테고리 수정 가능
     * - ownerId가 null: ADMIN만 수정 가능
     * - ownerId가 설정됨: ADMIN 또는 해당 Owner만 수정 가능
     */
    private void validateUpdatePermission(Category category, User user) {
        // ADMIN은 모든 카테고리 수정 가능
        if (user.isAdmin()) {
            return;
        }

        // ownerId가 null이면 ADMIN만 수정 가능
        if (!category.hasOwner()) {
            throw new CategoryUpdateNotAllowedException();
        }

        // ownerId가 설정된 경우, 해당 Owner만 수정 가능
        if (!category.isOwnedBy(user.getId())) {
            throw new CategoryUpdateNotAllowedException();
        }
    }
}
