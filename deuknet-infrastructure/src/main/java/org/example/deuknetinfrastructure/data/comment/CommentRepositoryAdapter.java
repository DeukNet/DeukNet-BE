package org.example.deuknetinfrastructure.data.comment;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.projection.comment.CommentProjection;
import org.example.deuknetdomain.domain.comment.Comment;
import org.example.deuknetinfrastructure.data.user.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.example.deuknetinfrastructure.data.comment.QCommentEntity.commentEntity;
import static org.example.deuknetinfrastructure.data.user.QUserEntity.userEntity;

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
    public List<CommentProjection> findProjectionsByPostId(UUID postId) {
        // QueryDSL로 Comment + User join 조회
        List<Tuple> results = queryFactory
                .select(commentEntity, userEntity)
                .from(commentEntity)
                .leftJoin(userEntity).on(commentEntity.authorId.eq(userEntity.id))
                .where(commentEntity.postId.eq(postId))
                .orderBy(commentEntity.createdAt.asc())
                .fetch();

        return results.stream()
                .map(tuple -> {
                    CommentEntity comment = tuple.get(commentEntity);
                    UserEntity user = tuple.get(userEntity);

                    return new CommentProjection(
                            comment.getId(),
                            comment.getPostId(),
                            comment.getContent(),
                            comment.getAuthorId(),
                            user != null ? user.getUsername() : null,
                            user != null ? user.getDisplayName() : null,
                            user != null ? user.getAvatarUrl() : null,
                            comment.getParentCommentId(),
                            comment.getParentCommentId() != null,  // isReply: parentCommentId가 있으면 true
                            comment.getCreatedAt(),
                            comment.getUpdatedAt()
                    );
                })
                .collect(Collectors.toList());
    }
}
