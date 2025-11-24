package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.comment.DeleteCommentUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.example.deuknetdomain.domain.comment.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Comment 삭제 서비스 (SRP 준수)
 *
 * 책임:
 * - Comment 삭제 권한 검증
 * - Comment 삭제
 * - 삭제 이벤트 발행 (Outbox Pattern)
 */
@Service
@Transactional
public class DeleteCommentService implements DeleteCommentUseCase {

    private final CommentRepository commentRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public DeleteCommentService(
            CommentRepository commentRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.commentRepository = commentRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public void deleteComment(UUID commentId) {
        // 1. Comment 조회 및 권한 검증
        Comment comment = getCommentAndVerifyOwnership(commentId);

        // 2. postId 저장 (삭제 후 이벤트 발행에 사용)
        UUID postId = comment.getPostId();

        // 3. Comment 삭제
        deleteCommentAggregate(comment);

        // 4. 삭제 이벤트 발행
        publishCommentDeletedEvent(comment.getId(), postId);
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
     * Comment 삭제 (SRP: Comment 삭제 책임 분리)
     */
    private void deleteCommentAggregate(Comment comment) {
        commentRepository.delete(comment);
    }

    /**
     * CommentDeleted 이벤트 발행 (SRP: 이벤트 발행 책임 분리)
     *
     * Projection을 삭제하도록 이벤트를 발행합니다.
     */
    private void publishCommentDeletedEvent(UUID commentId, UUID postId) {
        // 1. CommentDeleted 이벤트 발행
        dataChangeEventPublisher.publish(EventType.COMMENT_DELETED, commentId);

        // 2. PostCountProjection 발행 (commentCount 업데이트)
        Long commentCount = commentRepository.countByPostId(postId);
        PostCountProjection countProjection = PostCountProjection.builder()
                .id(postId)
                .commentCount(commentCount)
                .viewCount(null)      // null은 변경하지 않음
                .likeCount(null)      // null은 변경하지 않음
                .dislikeCount(null)   // null은 변경하지 않음
                .build();
        dataChangeEventPublisher.publish(EventType.POST_UPDATED, postId, countProjection);
    }
}
