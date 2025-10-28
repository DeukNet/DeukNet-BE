package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.out.repository.PostCategoryAssignmentRepository;
import org.example.deuknetdomain.domain.post.PostCategoryAssignment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Post-Category 연관관계 관리 서비스
 *
 * 책임:
 * - Post와 Category 간의 연관관계 생성/삭제
 *
 * SRP 준수: 카테고리 할당이라는 단일 책임만 가짐
 */
@Service
@Transactional
public class PostCategoryAssignmentService {

    private final PostCategoryAssignmentRepository postCategoryAssignmentRepository;

    public PostCategoryAssignmentService(PostCategoryAssignmentRepository postCategoryAssignmentRepository) {
        this.postCategoryAssignmentRepository = postCategoryAssignmentRepository;
    }

    /**
     * Post에 여러 카테고리 할당
     *
     * @param postId Post ID
     * @param categoryIds 할당할 카테고리 ID 목록
     */
    public void assignCategories(UUID postId, List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        for (UUID categoryId : categoryIds) {
            PostCategoryAssignment assignment = PostCategoryAssignment.create(postId, categoryId);
            postCategoryAssignmentRepository.save(assignment);
        }
    }

    /**
     * Post의 모든 카테고리 할당 제거
     *
     * @param postId Post ID
     */
    public void removeAllCategories(UUID postId) {
        postCategoryAssignmentRepository.deleteByPostId(postId);
    }

    /**
     * Post의 카테고리 재할당 (기존 삭제 후 새로 할당)
     *
     * @param postId Post ID
     * @param categoryIds 새로운 카테고리 ID 목록
     */
    public void reassignCategories(UUID postId, List<UUID> categoryIds) {
        removeAllCategories(postId);
        assignCategories(postId, categoryIds);
    }
}
