package org.example.deuknetinfrastructure.data.post;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetinfrastructure.data.user.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.example.deuknetinfrastructure.data.category.QCategoryEntity.categoryEntity;
import static org.example.deuknetinfrastructure.data.comment.QCommentEntity.commentEntity;
import static org.example.deuknetinfrastructure.data.post.QPostCategoryAssignmentEntity.postCategoryAssignmentEntity;
import static org.example.deuknetinfrastructure.data.post.QPostEntity.postEntity;
import static org.example.deuknetinfrastructure.data.reaction.QReactionEntity.reactionEntity;
import static org.example.deuknetinfrastructure.data.user.QUserEntity.userEntity;

@Component
public class PostRepositoryAdapter implements PostRepository {

    private final JpaPostRepository jpaPostRepository;
    private final PostMapper mapper;
    private final JPAQueryFactory queryFactory;

    public PostRepositoryAdapter(
            JpaPostRepository jpaPostRepository,
            PostMapper mapper,
            JPAQueryFactory queryFactory
    ) {
        this.jpaPostRepository = jpaPostRepository;
        this.mapper = mapper;
        this.queryFactory = queryFactory;
    }

    @Override
    public Post save(Post post) {
        PostEntity entity = mapper.toEntity(post);
        PostEntity savedEntity = jpaPostRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return jpaPostRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(Post post) {
        PostEntity entity = mapper.toEntity(post);
        jpaPostRepository.delete(entity);
    }

    @Override
    public Optional<PostDetailProjection> findDetailById(UUID id) {
        // 1. Post + User join + count 서브쿼리로 한 번에 조회
        Tuple postAndCounts = queryFactory
                .select(
                        postEntity,
                        userEntity,
                        JPAExpressions.select(commentEntity.count())
                                .from(commentEntity)
                                .where(commentEntity.postId.eq(id)),
                        JPAExpressions.select(reactionEntity.count())
                                .from(reactionEntity)
                                .where(
                                        reactionEntity.targetId.eq(id),
                                        reactionEntity.reactionType.eq(ReactionType.LIKE)
                                ),
                        JPAExpressions.select(reactionEntity.count())
                                .from(reactionEntity)
                                .where(
                                        reactionEntity.targetId.eq(id),
                                        reactionEntity.reactionType.eq(ReactionType.DISLIKE)
                                )
                )
                .from(postEntity)
                .leftJoin(userEntity).on(postEntity.authorId.eq(userEntity.id))
                .where(postEntity.id.eq(id))
                .fetchOne();

        if (postAndCounts == null) {
            return Optional.empty();
        }

        PostEntity post = postAndCounts.get(postEntity);
        UserEntity user = postAndCounts.get(userEntity);
        Long commentCount = postAndCounts.get(2, Long.class);
        Long likeCount = postAndCounts.get(3, Long.class);
        Long dislikeCount = postAndCounts.get(4, Long.class);

        // 2. Category Assignment + Category join으로 조회
        List<Tuple> categoryResults = queryFactory
                .select(postCategoryAssignmentEntity.categoryId, categoryEntity.name)
                .from(postCategoryAssignmentEntity)
                .leftJoin(categoryEntity).on(postCategoryAssignmentEntity.categoryId.eq(categoryEntity.id))
                .where(postCategoryAssignmentEntity.postId.eq(id))
                .fetch();

        List<UUID> categoryIds = categoryResults.stream()
                .map(tuple -> tuple.get(postCategoryAssignmentEntity.categoryId))
                .collect(Collectors.toList());

        List<String> categoryNames = categoryResults.stream()
                .map(tuple -> tuple.get(categoryEntity.name))
                .collect(Collectors.toList());

        // 7. Projection 생성
        PostDetailProjection result = PostDetailProjection.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorId(post.getAuthorId())
                .authorUsername(user != null ? user.getUsername() : null)
                .authorDisplayName(user != null ? user.getDisplayName() : null)
                .authorAvatarUrl(user != null ? user.getAvatarUrl() : null)
                .status(post.getStatus().name())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .categoryIds(categoryIds)
                .categoryNames(categoryNames)
                .commentCount(commentCount != null ? commentCount : 0L)
                .likeCount(likeCount != null ? likeCount : 0L)
                .dislikeCount(dislikeCount != null ? dislikeCount : 0L)
                .build();

        return Optional.of(result);
    }
}
