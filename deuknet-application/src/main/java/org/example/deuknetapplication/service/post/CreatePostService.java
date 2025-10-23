package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.post.CreatePostApplicationRequest;
import org.example.deuknetapplication.port.in.post.CreatePostUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.PostCategoryAssignmentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.common.vo.Title;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.post.PostCategoryAssignment;
import org.example.deuknetdomain.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Post 생성 서비스 (SRP 준수)
 *
 * 책임:
 * - Post Aggregate 생성 및 저장
 * - 카테고리 할당
 * - 이벤트 발행 (Outbox Pattern)
 */
@Service
@Transactional
public class CreatePostService implements CreatePostUseCase {

    private final PostRepository postRepository;
    private final PostCategoryAssignmentRepository postCategoryAssignmentRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public CreatePostService(
            PostRepository postRepository,
            PostCategoryAssignmentRepository postCategoryAssignmentRepository,
            UserRepository userRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.postRepository = postRepository;
        this.postCategoryAssignmentRepository = postCategoryAssignmentRepository;
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public UUID createPost(CreatePostApplicationRequest request) {
        // 1. 현재 사용자 조회
        User author = getCurrentUser();

        // 2. Post Aggregate 생성 및 저장
        Post post = createPostAggregate(request, author.getId());

        // 3. 카테고리 할당
        assignCategories(post.getId(), request.getCategoryIds());

        // 4. 이벤트 발행 (Outbox Pattern)
        publishPostCreatedEvents(post, author, request.getCategoryIds());

        return post.getId();
    }

    /**
     * 현재 사용자 조회 (SRP: 사용자 조회 책임 분리)
     */
    private User getCurrentUser() {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        return userRepository.findById(currentUserId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    /**
     * Post Aggregate 생성 및 저장 (SRP: Post 생성 책임 분리)
     */
    private Post createPostAggregate(CreatePostApplicationRequest request, UUID authorId) {
        Post post = Post.create(
                Title.from(request.getTitle()),
                Content.from(request.getContent()),
                authorId
        );
        return postRepository.save(post);
    }

    /**
     * 카테고리 할당 (SRP: 카테고리 할당 책임 분리)
     */
    private void assignCategories(UUID postId, List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        for (UUID categoryId : categoryIds) {
            PostCategoryAssignment assignment = PostCategoryAssignment.create(postId, categoryId);
            postCategoryAssignmentRepository.save(assignment);
        }
    }

    /**
     * PostCreated 이벤트 발행 (SRP: 이벤트 발행 책임 분리)
     *
     * 여러 Projection에 대한 이벤트를 발행합니다.
     * - PostDetailProjection: 상세 조회용
     * - PostCountProjection: 통계 집계용
     */
    private void publishPostCreatedEvents(Post post, User author, List<UUID> categoryIds) {
        publishPostDetailProjection(post, author, categoryIds);
        publishPostCountProjection(post);
    }

    /**
     * PostDetailProjection 이벤트 발행 (SRP: Detail Projection 발행 책임 분리)
     */
    private void publishPostDetailProjection(Post post, User author, List<UUID> categoryIds) {
        LocalDateTime now = LocalDateTime.now();

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
                .createdAt(now)
                .updatedAt(now)
                .categoryIds(categoryIds)
                .commentCount(0L)
                .likeCount(0L)
                .build();

        dataChangeEventPublisher.publish("PostCreated", post.getId(), projection);
    }

    /**
     * PostCountProjection 이벤트 발행 (SRP: Count Projection 발행 책임 분리)
     */
    private void publishPostCountProjection(Post post) {
        PostCountProjection projection = PostCountProjection.builder()
                .id(post.getId())
                .commentCount(0L)
                .likeCount(0L)
                .viewCount(post.getViewCount())
                .build();

        dataChangeEventPublisher.publish("PostCreated", post.getId(), projection);
    }
}
