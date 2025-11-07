package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.comment.CreateCommentAppliationRequest;
import org.example.deuknetapplication.port.in.comment.CreateCommentUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.comment.CommentProjection;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.domain.comment.Comment;
import org.example.deuknetdomain.domain.user.User;
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

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public CreateCommentService(
            CommentRepository commentRepository,
            UserRepository userRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public UUID createComment(CreateCommentAppliationRequest request) {
        // 1. 현재 사용자 조회
        User author = getCurrentUser();

        // 2. Comment Aggregate 생성 및 저장
        Comment comment = createCommentAggregate(request, author.getId());

        // 3. 이벤트 발행 (Outbox Pattern)
        publishCommentCreatedEvent(comment, author);

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
    private Comment createCommentAggregate(CreateCommentAppliationRequest request, UUID authorId) {
        Comment comment = Comment.create(
                request.getPostId(),
                authorId,
                Content.from(request.getContent()),
                request.getParentCommentId()
        );
        return commentRepository.save(comment);
    }

    /**
     * CommentCreated 이벤트 발행 (SRP: 이벤트 발행 책임 분리)
     */
    private void publishCommentCreatedEvent(Comment comment, User author) {
        LocalDateTime now = LocalDateTime.now();

        CommentProjection projection = new CommentProjection(
                comment.getId(),
                comment.getPostId(),
                comment.getContent().getValue(),
                author.getId(),
                author.getUsername(),
                author.getDisplayName(),
                author.getAvatarUrl(),
                comment.getParentCommentId().orElse(null),
                comment.isReply(),
                now,
                now
        );

        dataChangeEventPublisher.publish(EventType.COMMENT_CREATED, comment.getId(), projection);
    }
}
