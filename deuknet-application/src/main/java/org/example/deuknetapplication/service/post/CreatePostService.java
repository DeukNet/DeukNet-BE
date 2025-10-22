package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.post.CreatePostApplicationRequest;
import org.example.deuknetapplication.port.in.post.CreatePostUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.PostCategoryAssignmentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.post.Post;
import org.example.deuknetdomain.model.command.post.PostCategoryAssignment;
import org.example.deuknetdomain.model.command.reaction.ReactionType;
import org.example.deuknetdomain.model.command.user.User;
import org.example.deuknetdomain.model.query.post.PostCountProjection;
import org.example.deuknetdomain.model.query.post.PostDetailProjection;
import org.example.deuknetdomain.model.query.post.PostSummaryProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class CreatePostService implements CreatePostUseCase {

    private final PostRepository postRepository;
    private final PostCategoryAssignmentRepository postCategoryAssignmentRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public CreatePostService(
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
    public UUID createPost(CreatePostApplicationRequest request) {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        Post post = Post.create(
                org.example.deuknetdomain.common.vo.Title.from(request.getTitle()),
                org.example.deuknetdomain.common.vo.Content.from(request.getContent()),
                currentUserId
        );

        post = postRepository.save(post);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (UUID categoryId : request.getCategoryIds()) {
                PostCategoryAssignment assignment = PostCategoryAssignment.create(
                        post.getId(),
                        categoryId
                );
                postCategoryAssignmentRepository.save(assignment);
            }
        }

        // 데이터 변경 이벤트 발행
        LocalDateTime now = LocalDateTime.now();

        // User 정보 조회
        User author = userRepository.findById(currentUserId)
                .orElseThrow(ResourceNotFoundException::new);

        // Count 정보 조회 (신규 Post이므로 모두 0)
        long commentCount = 0L;
        long likeCount = 0L;  // targetId로 조회

        // 1. PostDetail 이벤트 발행 (상세 조회용)
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
                .createdAt(now)
                .updatedAt(now)
                .categoryIds(request.getCategoryIds())
                .commentCount(commentCount)
                .likeCount(likeCount)
                .build();
        dataChangeEventPublisher.publish("PostCreated", post.getId(), detailProjection);

        // 2. PostSummary 이벤트 발행 (목록 조회용)
        PostSummaryProjection summaryProjection = PostSummaryProjection.builder()
                .id(post.getId())
                .title(post.getTitle().getValue())
                .authorId(post.getAuthorId())
                .authorDisplayName(author.getDisplayName())
                .status(post.getStatus().name())
                .viewCount(post.getViewCount())
                .commentCount(commentCount)
                .createdAt(now)
                .updatedAt(now)
                .build();
        dataChangeEventPublisher.publish("PostCreated", post.getId(), summaryProjection);

        // 3. PostCount 이벤트 발행 (통계 정보용)
        PostCountProjection countProjection = PostCountProjection.builder()
                .id(post.getId())
                .commentCount(commentCount)
                .likeCount(likeCount)
                .viewCount(post.getViewCount())
                .build();
        dataChangeEventPublisher.publish("PostCreated", post.getId(), countProjection);

        return post.getId();
    }
}
