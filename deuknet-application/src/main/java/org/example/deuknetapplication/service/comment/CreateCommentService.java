package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.port.in.comment.CreateCommentUseCase;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.comment.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateCommentService implements CreateCommentUseCase {

    private final CommentRepository commentRepository;
    private final CurrentUserPort currentUserPort;

    public CreateCommentService(CommentRepository commentRepository, CurrentUserPort currentUserPort) {
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
