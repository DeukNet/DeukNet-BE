package org.example.deuknetapplication.usecase.comment;

import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.comment.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteCommentUseCaseImpl implements DeleteCommentUseCase {

    private final CommentRepository commentRepository;
    private final CurrentUserPort currentUserPort;

    public DeleteCommentUseCaseImpl(CommentRepository commentRepository, CurrentUserPort currentUserPort) {
        this.commentRepository = commentRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void deleteComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        if (!comment.getAuthorId().equals(currentUserPort.getCurrentUserId())) {
            throw new IllegalArgumentException("Not authorized to delete this comment");
        }
        
        commentRepository.delete(comment);
    }
}
