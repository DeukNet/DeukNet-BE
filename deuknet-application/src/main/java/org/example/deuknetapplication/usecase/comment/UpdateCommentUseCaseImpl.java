package org.example.deuknetapplication.usecase.comment;

import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.comment.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateCommentUseCaseImpl implements UpdateCommentUseCase {

    private final CommentRepository commentRepository;
    private final CurrentUserPort currentUserPort;

    public UpdateCommentUseCaseImpl(CommentRepository commentRepository, CurrentUserPort currentUserPort) {
        this.commentRepository = commentRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void updateComment(UpdateCommentCommand command) {
        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        if (!comment.getAuthorId().equals(currentUserPort.getCurrentUserId())) {
            throw new IllegalArgumentException("Not authorized to update this comment");
        }
        
        comment.updateContent(command.content());
        commentRepository.save(comment);
    }
}
