package org.example.deuknetapplication.usecase.comment;

import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.comment.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateCommentUseCaseImpl implements CreateCommentUseCase {

    private final CommentRepository commentRepository;
    private final CurrentUserPort currentUserPort;

    public CreateCommentUseCaseImpl(CommentRepository commentRepository, CurrentUserPort currentUserPort) {
        this.commentRepository = commentRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public UUID createComment(CreateCommentCommand command) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        
        Comment comment = Comment.create(
                command.postId(),
                currentUserId,
                command.content(),
                command.parentCommentId()
        );
        
        comment = commentRepository.save(comment);
        return comment.getId();
    }
}
