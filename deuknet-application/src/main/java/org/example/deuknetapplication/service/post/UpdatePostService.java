package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.post.UpdatePostApplcationRequest;
import org.example.deuknetapplication.port.in.post.UpdatePostUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.PostCategoryAssignmentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.common.vo.Title;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.post.PostCategoryAssignment;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Post 수정 서비스 (SRP 준수)
 * 책임:
 * - Post 수정 권한 검증
 * - Post 내용 및 카테고리 업데이트
 * - 이벤트 발행 (Outbox Pattern)
 */
@Service
@Transactional
public class UpdatePostService implements UpdatePostUseCase {

    private final PostRepository postRepository;
    private final PostCategoryAssignmentRepository postCategoryAssignmentRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public UpdatePostService(
            PostRepository postRepository,
            PostCategoryAssignmentRepository postCategoryAssignmentRepository,
            UserRepository userRepository,
            CommentRepository commentRepository,
            ReactionRepository reactionRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.postRepository = postRepository;
        this.postCategoryAssignmentRepository = postCategoryAssignmentRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public void updatePost(UpdatePostApplcationRequest request) {
        // 1. Post 조회 및 권한 검증
        Post post = getPostAndVerifyOwnership(request.getPostId());

        // 2. Post 내용 업데이트
        updatePostContent(post, request);

        // 3. 카테고리 재할당
        reassignCategories(request.getPostId(), request.getCategoryIds());

        // 4. 이벤트 발행
        publishPostUpdatedEvents(post, request.getCategoryIds());
    }

    /**
     * Post 조회 및 소유권 검증 (SRP: 권한 검증 책임 분리)
     */
    private Post getPostAndVerifyOwnership(UUID postId) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(ResourceNotFoundException::new);

        if (!post.getAuthorId().equals(currentUserId)) {
            throw new OwnerMismatchException();
        }

        return post;
    }

    /**
     * Post 내용 업데이트 (SRP: Post 업데이트 책임 분리)
     */
    private void updatePostContent(Post post, UpdatePostApplcationRequest request) {
        post.updateContent(
                Title.from(request.getTitle()),
                Content.from(request.getContent())
        );
        postRepository.save(post);
    }

    /**
     * 카테고리 재할당 (SRP: 카테고리 관리 책임 분리)
     */
    private void reassignCategories(UUID postId, List<UUID> categoryIds) {
        // 기존 카테고리 삭제
        postCategoryAssignmentRepository.deleteByPostId(postId);

        // 새 카테고리 할당
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        for (UUID categoryId : categoryIds) {
            PostCategoryAssignment assignment = PostCategoryAssignment.create(postId, categoryId);
            postCategoryAssignmentRepository.save(assignment);
        }
    }

    /**
     * PostUpdated 이벤트 발행 (SRP: 이벤트 발행 책임 분리)
     */
    private void publishPostUpdatedEvents(Post post, List<UUID> categoryIds) {
        User author = getAuthor(post.getAuthorId());
        publishPostDetailProjection(post, author, categoryIds);
        publishPostCountProjection(post);
    }

    /**
     * 작성자 조회 (SRP: 사용자 조회 책임 분리)
     */
    private User getAuthor(UUID authorId) {
        return userRepository.findById(authorId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    /**
     * PostDetailProjection 이벤트 발행 (SRP: Detail Projection 발행 책임 분리)
     */
    private void publishPostDetailProjection(Post post, User author, List<UUID> categoryIds) {
        LocalDateTime now = LocalDateTime.now();

        // 현재 통계 조회 (실제 DB에서)
        long commentCount = commentRepository.countByPostId(post.getId());
        long likeCount = reactionRepository.countByTargetIdAndReactionType(
                post.getId(), ReactionType.LIKE);

        PostDetailProjection projection = PostDetailProjection.builder()
                .id(post.getId())
                .title(post.getTitle().getValue())
                .content(post.getContent().getValue())
                .authorId(post.getAuthorId())
                .authorUsername(author.getUsername())
                .authorDisplayName(author.getDisplayName())
                .authorAvatarUrl(author.getAvatarUrl())
                .status(post.getStatus().name())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(now)
                .categoryIds(categoryIds)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .build();

        dataChangeEventPublisher.publish("PostUpdated", post.getId(), projection);
    }

    /**
     * PostCountProjection 이벤트 발행 (SRP: Count Projection 발행 책임 분리)
     */
    private void publishPostCountProjection(Post post) {
        // 현재 통계 조회 (실제 DB에서)
        long commentCount = commentRepository.countByPostId(post.getId());
        long likeCount = reactionRepository.countByTargetIdAndReactionType(
                post.getId(), ReactionType.LIKE);

        PostCountProjection projection = PostCountProjection.builder()
                .id(post.getId())
                .commentCount(commentCount)
                .likeCount(likeCount)
                .viewCount(post.getViewCount())
                .build();

        dataChangeEventPublisher.publish("PostUpdated", post.getId(), projection);
    }
}
