package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.post.PublishPostUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.PostCategoryAssignmentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetapplication.service.reaction.ReactionProjectionFactory;
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
 * Post 발행 서비스 (SRP 준수)
 *
 * 책임:
 * - Post 발행 권한 검증
 * - Post 상태를 PUBLISHED로 변경
 * - 발행 이벤트 발행 (Outbox Pattern)
 */
@Service
@Transactional
public class PublishPostService implements PublishPostUseCase {

    private final PostRepository postRepository;
    private final PostCategoryAssignmentRepository postCategoryAssignmentRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final ReactionProjectionFactory reactionProjectionFactory;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public PublishPostService(
            PostRepository postRepository,
            PostCategoryAssignmentRepository postCategoryAssignmentRepository,
            UserRepository userRepository,
            CommentRepository commentRepository,
            ReactionRepository reactionRepository,
            CurrentUserPort currentUserPort,
            ReactionProjectionFactory reactionProjectionFactory,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.postRepository = postRepository;
        this.postCategoryAssignmentRepository = postCategoryAssignmentRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
        this.reactionProjectionFactory = reactionProjectionFactory;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public void publishPost(UUID postId) {
        // 1. Post 조회 및 권한 검증
        Post post = getPostAndVerifyOwnership(postId);

        // 2. Post 발행 (상태 변경)
        publishPostAggregate(post);

        // 3. 발행 이벤트 발행
        publishPostPublishedEvent(post);
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
     * Post 발행 (SRP: Post 상태 변경 책임 분리)
     */
    private void publishPostAggregate(Post post) {
        post.publish();
        postRepository.save(post);
    }

    /**
     * PostPublished 이벤트 발행 (SRP: 이벤트 발행 책임 분리)
     */
    private void publishPostPublishedEvent(Post post) {
        User author = getAuthor(post.getAuthorId());
        List<UUID> categoryIds = getCategoryIds(post.getId());
        publishPostDetailProjection(post, author, categoryIds);
    }

    /**
     * 작성자 조회 (SRP: 사용자 조회 책임 분리)
     */
    private User getAuthor(UUID authorId) {
        return userRepository.findById(authorId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    /**
     * 카테고리 ID 목록 조회 (SRP: 카테고리 조회 책임 분리)
     */
    private List<UUID> getCategoryIds(UUID postId) {
        return postCategoryAssignmentRepository.findByPostId(postId)
                .stream()
                .map(PostCategoryAssignment::getCategoryId)
                .toList();
    }

    /**
     * PostDetailProjection 이벤트 발행 (SRP: Detail Projection 발행 책임 분리)
     */
    private void publishPostDetailProjection(Post post, User author, List<UUID> categoryIds) {
        LocalDateTime now = LocalDateTime.now();

        // 현재 통계 조회
        long commentCount = commentRepository.countByPostId(post.getId());
        long likeCount = reactionRepository.countByTargetIdAndReactionType(
                post.getId(), ReactionType.LIKE);
        long dislikeCount = reactionRepository.countByTargetIdAndReactionType(
                post.getId(), ReactionType.DISLIKE);
        long viewCount = reactionRepository.countByTargetIdAndReactionType(
                post.getId(), ReactionType.VIEW);

        PostDetailProjection detailProjection = PostDetailProjection.builder()
                .id(post.getId())
                .title(post.getTitle().getValue())
                .content(post.getContent().getValue())
                .authorId(post.getAuthorId())
                .authorUsername(author.getUsername())
                .authorDisplayName(author.getDisplayName())
                .authorAvatarUrl(author.getAvatarUrl())
                .status(post.getStatus().name())  // PUBLISHED로 변경됨
                .viewCount(viewCount)  // Reaction에서 집계
                .createdAt(post.getCreatedAt())
                .updatedAt(now)
                .categoryIds(categoryIds)
                .categoryNames(java.util.List.of())  // 빈 카테고리 이름 목록
                .commentCount(commentCount)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .build();

        PostCountProjection countProjection = reactionProjectionFactory.createCountProjection(
                post.getId(), commentCount, likeCount, dislikeCount, viewCount
        );

        dataChangeEventPublisher.publish(EventType.POST_PUBLISHED, post.getId(), detailProjection);
        dataChangeEventPublisher.publish(EventType.POST_PUBLISHED, post.getId(), countProjection);
    }
}
