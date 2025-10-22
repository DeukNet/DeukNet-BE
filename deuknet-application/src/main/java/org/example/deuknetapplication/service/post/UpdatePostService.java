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
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.post.PostCategoryAssignment;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.user.User;
import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetapplication.projection.post.PostSummaryProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
        UUID currentUserId = currentUserPort.getCurrentUserId();

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(ResourceNotFoundException::new);

        if (!post.getAuthorId().equals(currentUserId)) {
            throw new OwnerMismatchException();
        }

        post.updateContent(
                org.example.deuknetdomain.common.vo.Title.from(request.getTitle()),
                org.example.deuknetdomain.common.vo.Content.from(request.getContent())
        );
        postRepository.save(post);

        postCategoryAssignmentRepository.deleteByPostId(request.getPostId());

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (UUID categoryId : request.getCategoryIds()) {
                PostCategoryAssignment assignment = PostCategoryAssignment.create(
                        request.getPostId(),
                        categoryId
                );
                postCategoryAssignmentRepository.save(assignment);
            }
        }

        // 데이터 변경 이벤트 발행
        LocalDateTime now = LocalDateTime.now();

        // User 정보 조회
        User author = userRepository.findById(post.getAuthorId())
                .orElseThrow(ResourceNotFoundException::new);

        // Count 정보 조회 (실제 DB에서 조회)
        long commentCount = commentRepository.countByPostId(post.getId());
        long likeCount = reactionRepository.countByTargetIdAndReactionType(post.getId(), ReactionType.LIKE);

        // 1. PostDetail 업데이트 이벤트 발행
        PostDetailProjection detailProjection = PostDetailProjection.builder()
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
                .categoryIds(request.getCategoryIds())
                .commentCount(commentCount)
                .likeCount(likeCount)
                .build();
        dataChangeEventPublisher.publish("PostUpdated", post.getId(), detailProjection);

        // 2. PostSummary 업데이트 이벤트 발행
        PostSummaryProjection summaryProjection = PostSummaryProjection.builder()
                .id(post.getId())
                .title(post.getTitle().getValue())
                .authorId(post.getAuthorId())
                .authorDisplayName(author.getDisplayName())
                .status(post.getStatus().name())
                .viewCount(post.getViewCount())
                .commentCount(commentCount)
                .createdAt(post.getCreatedAt())
                .updatedAt(now)
                .build();
        dataChangeEventPublisher.publish("PostUpdated", post.getId(), summaryProjection);

        // 3. PostCount 업데이트 이벤트 발행
        PostCountProjection countProjection = PostCountProjection.builder()
                .id(post.getId())
                .commentCount(commentCount)
                .likeCount(likeCount)
                .viewCount(post.getViewCount())
                .build();
        dataChangeEventPublisher.publish("PostUpdated", post.getId(), countProjection);
    }
}
