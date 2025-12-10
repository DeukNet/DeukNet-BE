package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.comment.DeleteCommentUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetapplication.service.post.PostProjectionFactory;
import org.example.deuknetdomain.domain.comment.Comment;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.reaction.ReactionType;
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
    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;
    private final PostProjectionFactory projectionFactory;

    public DeleteCommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            ReactionRepository reactionRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher,
            PostProjectionFactory projectionFactory
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
        this.projectionFactory = projectionFactory;
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

        // 2. PostDetailProjection 발행 (전체 통계 업데이트)
        Post post = postRepository.findById(postId)
                .orElseThrow(ResourceNotFoundException::new);

        long commentCount = commentRepository.countByPostId(postId);
        long likeCount = reactionRepository.countByTargetIdAndReactionType(postId, ReactionType.LIKE);
        long dislikeCount = reactionRepository.countByTargetIdAndReactionType(postId, ReactionType.DISLIKE);
        long viewCount = reactionRepository.countByTargetIdAndReactionType(postId, ReactionType.VIEW);

        PostDetailProjection detailProjection = projectionFactory.createDetailProjectionForUpdate(
                post,
                post.getCategoryId(),
                commentCount,
                likeCount,
                dislikeCount,
                viewCount
        );
        dataChangeEventPublisher.publish(EventType.POST_UPDATED, postId, detailProjection);
    }
}
