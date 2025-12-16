package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.GrantCategoryOwnershipUseCase;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.category.Category;
import org.example.deuknetdomain.domain.category.exception.CategoryNotFoundException;
import org.example.deuknetdomain.domain.category.exception.CategoryUpdateNotAllowedException;
import org.example.deuknetdomain.domain.user.User;
import org.example.deuknetdomain.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class GrantCategoryOwnershipService implements GrantCategoryOwnershipUseCase {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;

    public GrantCategoryOwnershipService(
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            CurrentUserPort currentUserPort
    ) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void grantOwnership(UUID categoryId, UUID targetUserId) {
        // 1. 카테고리 조회
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        // 2. 현재 사용자 조회
        UUID currentUserId = currentUserPort.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(UserNotFoundException::new);

        // 3. 대상 사용자 존재 확인
        userRepository.findById(targetUserId)
                .orElseThrow(UserNotFoundException::new);

        // 4. 권한 검증 (ADMIN 또는 현재 Owner만 가능)
        validateGrantPermission(category, currentUser);

        // 5. 소유권 부여
        category.updateOwnerId(targetUserId);

        // 6. 저장
        categoryRepository.save(category);
    }

    /**
     * 소유권 부여 권한 검증
     * - ADMIN: 모든 카테고리에 대해 소유권 부여 가능
     * - ownerId가 null: ADMIN만 소유권 부여 가능
     * - ownerId가 설정됨: ADMIN 또는 현재 Owner만 소유권 부여 가능
     */
    private void validateGrantPermission(Category category, User user) {
        // ADMIN은 모든 카테고리에 대해 소유권 부여 가능
        if (user.isAdmin()) {
            return;
        }

        // ownerId가 null이면 ADMIN만 소유권 부여 가능
        if (!category.hasOwner()) {
            throw new CategoryUpdateNotAllowedException();
        }

        // ownerId가 설정된 경우, 현재 Owner만 소유권 부여 가능
        if (!category.isOwnedBy(user.getId())) {
            throw new CategoryUpdateNotAllowedException();
        }
    }
}
