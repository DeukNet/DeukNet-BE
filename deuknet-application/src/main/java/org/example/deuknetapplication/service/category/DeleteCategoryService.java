package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.DeleteCategoryUseCase;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.category.Category;
import org.example.deuknetdomain.domain.category.exception.CategoryDeleteNotAllowedException;
import org.example.deuknetdomain.domain.category.exception.CategoryHasChildrenException;
import org.example.deuknetdomain.domain.category.exception.CategoryNotFoundException;
import org.example.deuknetdomain.domain.user.User;
import org.example.deuknetdomain.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DeleteCategoryService implements DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;

    public DeleteCategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            CurrentUserPort currentUserPort
    ) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void deleteCategory(UUID categoryId) {
        // 1. 카테고리 조회
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        // 2. 현재 사용자 조회
        UUID currentUserId = currentUserPort.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(UserNotFoundException::new);

        // 3. 권한 검증
        validateDeletePermission(category, user);

        // 4. 하위 카테고리 확인
        List<Category> children = categoryRepository.findByParentCategoryId(categoryId);
        if (!children.isEmpty()) {
            throw new CategoryHasChildrenException();
        }

        // 5. 삭제
        categoryRepository.delete(category);
    }

    /**
     * 카테고리 삭제 권한 검증
     * - ADMIN: 모든 카테고리 삭제 가능
     * - ownerId가 null: ADMIN만 삭제 가능
     * - ownerId가 설정됨: ADMIN 또는 해당 Owner만 삭제 가능
     */
    private void validateDeletePermission(Category category, User user) {
        // ADMIN은 모든 카테고리 삭제 가능
        if (user.isAdmin()) {
            return;
        }

        // ownerId가 null이면 ADMIN만 삭제 가능
        if (!category.hasOwner()) {
            throw new CategoryDeleteNotAllowedException();
        }

        // ownerId가 설정된 경우, 해당 Owner만 삭제 가능
        if (!category.isOwnedBy(user.getId())) {
            throw new CategoryDeleteNotAllowedException();
        }
    }
}
