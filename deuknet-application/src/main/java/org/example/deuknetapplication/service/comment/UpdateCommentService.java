package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.comment.UpdateCommentApplicationRequest;
import org.example.deuknetapplication.port.in.comment.UpdateCommentUseCase;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.domain.comment.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Comment 수정 서비스 (SRP 준수)
 *
 * 책임:
 * - Comment 수정 권한 검증
 * - Comment 내용 업데이트
 *
 * Note: Comment는 CQRS를 사용하지 않으므로 이벤트를 발행하지 않음
 */
@Service
@Transactional
public class UpdateCommentService implements UpdateCommentUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateCommentService.class);

    private final CommentRepository commentRepository;
    private final CurrentUserPort currentUserPort;

    public UpdateCommentService(
            CommentRepository commentRepository,
            CurrentUserPort currentUserPort
    ) {
        this.commentRepository = commentRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void updateComment(UpdateCommentApplicationRequest request) {
        // 1. Comment 조회 및 권한 검증
        Comment comment = getCommentAndVerifyOwnership(request.getCommentId());

        // 2. Comment 내용 업데이트
        updateCommentContent(comment, request.getContent());

        log.info("[COMMENT_UPDATED] commentId={}, postId={}, authorId={}",
                comment.getId(),
                comment.getPostId(),
                comment.getAuthorId());
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
}
