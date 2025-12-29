package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.comment.CreateCommentApplicationRequest;
import org.example.deuknetapplication.port.in.comment.CreateCommentUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetapplication.service.post.PostProjectionFactory;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.domain.comment.Comment;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Comment 생성 서비스 (SRP 준수)
 * 책임:
 * - Comment Aggregate 생성 및 저장
 * - 이벤트 발행 (Outbox Pattern)
 */
@Service
@Transactional
public class CreateCommentService implements CreateCommentUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateCommentService.class);

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;
    private final PostProjectionFactory projectionFactory;

    public CreateCommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            ReactionRepository reactionRepository,
            UserRepository userRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher,
            PostProjectionFactory projectionFactory
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.reactionRepository = reactionRepository;
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
        this.projectionFactory = projectionFactory;
    }

    @Override
    public UUID createComment(CreateCommentApplicationRequest request) {
        // 1. 현재 사용자 조회
        User author = getCurrentUser();

        // 2. Comment Aggregate 생성 및 저장
        Comment comment = createCommentAggregate(request, author.getId());

        // 3. 이벤트 발행 (Outbox Pattern)
        publishCommentCreatedEvent(comment, author);

        log.info("[COMMENT_CREATED] commentId={}, postId={}, authorId={}, username={}, isReply={}",
                comment.getId(),
                comment.getPostId(),
                author.getId(),
                author.getUsername(),
                comment.isReply());

        return comment.getId();
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
     * Comment Aggregate 생성 및 저장 (SRP: Comment 생성 책임 분리)
     */
    private Comment createCommentAggregate(CreateCommentApplicationRequest request, UUID authorId) {
        Comment comment = Comment.create(
                request.getPostId(),
                authorId,
                Content.from(request.getContent()),
                request.getParentCommentId(),
                request.getAuthorType()
        );
        return commentRepository.save(comment);
    }

    /**
     * CommentCreated 이벤트 발행 (SRP: 이벤트 발행 책임 분리)
     * Comment는 CQRS를 사용하지 않으므로 PostDetailProjection만 발행
     */
    private void publishCommentCreatedEvent(Comment comment, User author) {
        // PostDetailProjection 발행 (댓글 수 업데이트)
        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(ResourceNotFoundException::new);

        long commentCount = commentRepository.countByPostId(comment.getPostId());
        long likeCount = reactionRepository.countByTargetIdAndReactionType(comment.getPostId(), ReactionType.LIKE);
        long dislikeCount = reactionRepository.countByTargetIdAndReactionType(comment.getPostId(), ReactionType.DISLIKE);
        long viewCount = reactionRepository.countByTargetIdAndReactionType(comment.getPostId(), ReactionType.VIEW);

        PostDetailProjection detailProjection = projectionFactory.createDetailProjectionForUpdate(
                post,
                post.getCategoryId(),
                commentCount,
                likeCount,
                dislikeCount,
                viewCount
        );
        dataChangeEventPublisher.publish(EventType.POST_UPDATED, comment.getPostId(), detailProjection);
    }
}
