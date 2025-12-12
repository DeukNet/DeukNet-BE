package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.comment.UpdateCommentApplicationRequest;
import org.example.deuknetapplication.port.in.comment.UpdateCommentUseCase;
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
 * Comment 수정 서비스 (SRP 준수)
 *
 * 책임:
 * - Comment 수정 권한 검증
 * - Comment 내용 업데이트
 * - 이벤트 발행 (Outbox Pattern)
 */
@Service
@Transactional
public class UpdateCommentService implements UpdateCommentUseCase {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public UpdateCommentService(
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
    public void updateComment(UpdateCommentApplicationRequest request) {
        // 1. Comment 조회 및 권한 검증
        Comment comment = getCommentAndVerifyOwnership(request.getCommentId());

        // 2. Comment 내용 업데이트
        updateCommentContent(comment, request.getContent());

        // 3. 이벤트 발행
        publishCommentUpdatedEvent(comment);
    }

    /**
     * Comment 조회 및 소유권 검증 (SRP: 권한 검증 책임 분리)
     */
    private Comment getCommentAndVerifyOwnership(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(ResourceNotFoundException::new);

        if (!comment.getAuthorId().equals(currentUserPort.getCurrentUserId())) {
            throw new OwnerMismatchException();
        }

        return comment;
    }

    /**
     * Comment 내용 업데이트 (SRP: Comment 업데이트 책임 분리)
     */
    private void updateCommentContent(Comment comment, String newContent) {
        comment.updateContent(Content.from(newContent));
        commentRepository.save(comment);
    }

    /**
     * CommentUpdated 이벤트 발행 (SRP: 이벤트 발행 책임 분리)
     */
    private void publishCommentUpdatedEvent(Comment comment) {
        User author = getAuthor(comment.getAuthorId());
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
                comment.getAuthorType().name(),
                comment.getCreatedAt(),
                now
        );

        dataChangeEventPublisher.publish(EventType.COMMENT_UPDATED, comment.getId(), projection);
    }

    /**
     * 작성자 조회 (SRP: 사용자 조회 책임 분리)
     */
    private User getAuthor(UUID authorId) {
        return userRepository.findById(authorId)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
