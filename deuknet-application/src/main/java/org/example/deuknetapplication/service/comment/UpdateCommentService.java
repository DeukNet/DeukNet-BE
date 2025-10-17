package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.port.in.comment.UpdateCommentApplicationRequest;
import org.example.deuknetapplication.port.in.comment.UpdateCommentUseCase;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.common.exception.EntityNotFoundException;
import org.example.deuknetdomain.common.exception.ForbiddenException;
import org.example.deuknetdomain.model.command.comment.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateCommentService implements UpdateCommentUseCase {

    private final CommentRepository commentRepository;
    private final CurrentUserPort currentUserPort;

    public UpdateCommentService(CommentRepository commentRepository, CurrentUserPort currentUserPort) {
        this.commentRepository = commentRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void updateComment(UpdateCommentApplicationRequest request) {
        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(() -> new EntityNotFoundException("Comment"));

        if (!comment.getAuthorId().equals(currentUserPort.getCurrentUserId())) {
            throw new ForbiddenException("Not authorized to update this comment");
        }

        comment.updateContent(org.example.deuknetdomain.common.vo.Content.from(request.getContent()));
        commentRepository.save(comment);
    }
}
