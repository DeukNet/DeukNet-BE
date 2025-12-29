package org.example.deuknetinfrastructure.data.comment;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetdomain.domain.comment.Comment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.example.deuknetinfrastructure.data.comment.QCommentEntity.commentEntity;

@Component
public class CommentRepositoryAdapter implements CommentRepository {

    private final JpaCommentRepository jpaCommentRepository;
    private final CommentMapper mapper;
    private final JPAQueryFactory queryFactory;

    public CommentRepositoryAdapter(
            JpaCommentRepository jpaCommentRepository,
            CommentMapper mapper,
            JPAQueryFactory queryFactory
    ) {
        this.jpaCommentRepository = jpaCommentRepository;
        this.mapper = mapper;
        this.queryFactory = queryFactory;
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

    @Override
    public List<Comment> findByPostId(UUID postId) {
        List<CommentEntity> entities = queryFactory
                .selectFrom(commentEntity)
                .where(commentEntity.postId.eq(postId))
                .orderBy(commentEntity.createdAt.asc())
                .fetch();

        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
