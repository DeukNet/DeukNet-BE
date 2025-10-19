package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.comment.DeleteCommentUseCase;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.comment.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteCommentService implements DeleteCommentUseCase {

    private final CommentRepository commentRepository;
    private final CurrentUserPort currentUserPort;

    public DeleteCommentService(CommentRepository commentRepository, CurrentUserPort currentUserPort) {
        this.commentRepository = commentRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void deleteComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(ResourceNotFoundException::new);

        if (!comment.getAuthorId().equals(currentUserPort.getCurrentUserId())) {
            throw new OwnerMismatchException();
        }

        commentRepository.delete(comment);
    }
}
