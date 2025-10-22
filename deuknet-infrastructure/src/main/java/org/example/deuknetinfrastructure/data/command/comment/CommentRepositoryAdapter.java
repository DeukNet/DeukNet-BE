package org.example.deuknetinfrastructure.data.command.comment;

import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetdomain.model.command.comment.Comment;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CommentRepositoryAdapter implements CommentRepository {

    private final JpaCommentRepository jpaCommentRepository;
    private final CommentMapper mapper;

    public CommentRepositoryAdapter(JpaCommentRepository jpaCommentRepository, CommentMapper mapper) {
        this.jpaCommentRepository = jpaCommentRepository;
        this.mapper = mapper;
    }

    @Override
    public Comment save(Comment comment) {
        CommentEntity entity = mapper.toEntity(comment);
        CommentEntity savedEntity = jpaCommentRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Comment> findById(UUID id) {
        return jpaCommentRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(Comment comment) {
        CommentEntity entity = mapper.toEntity(comment);
        jpaCommentRepository.delete(entity);
    }

    @Override
    public long countByPostId(UUID postId) {
        return jpaCommentRepository.countByPostId(postId);
    }
}
