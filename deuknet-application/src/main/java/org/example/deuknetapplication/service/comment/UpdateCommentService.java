package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.comment.UpdateCommentApplicationRequest;
import org.example.deuknetapplication.port.in.comment.UpdateCommentUseCase;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
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
                .orElseThrow(ResourceNotFoundException::new);

        if (!comment.getAuthorId().equals(currentUserPort.getCurrentUserId())) {
            throw new OwnerMismatchException();
        }

        comment.updateContent(org.example.deuknetdomain.common.vo.Content.from(request.getContent()));
        commentRepository.save(comment);
    }
}
