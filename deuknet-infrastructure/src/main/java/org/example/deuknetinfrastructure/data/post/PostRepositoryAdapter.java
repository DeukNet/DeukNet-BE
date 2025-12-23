package org.example.deuknetinfrastructure.data.post;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.reaction.TargetType;
import org.example.deuknetinfrastructure.data.user.UserEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.example.deuknetinfrastructure.data.category.QCategoryEntity.categoryEntity;
import static org.example.deuknetinfrastructure.data.comment.QCommentEntity.commentEntity;
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
        // 최적화된 단일 쿼리: Post + 모든 count를 CASE WHEN으로 한 번에 조회
        Tuple result = queryFactory
                .select(
                        postEntity.id,
                        postEntity.title,
                        postEntity.content,
                        postEntity.authorId,
                        postEntity.authorType,
                        postEntity.status,
                        postEntity.thumbnailImageUrl,
                        postEntity.createdAt,
                        postEntity.updatedAt,
                        postEntity.categoryId,
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
                                ),
                        JPAExpressions.select(reactionEntity.count())
                                .from(reactionEntity)
                                .where(
                                        reactionEntity.targetId.eq(id),
                                        reactionEntity.reactionType.eq(ReactionType.VIEW)
                                )
                )
                .from(postEntity)
                .where(postEntity.id.eq(id))
                .fetchOne();

        if (result == null) {
            return Optional.empty();
        }

        Long commentCount = result.get(10, Long.class);
        Long likeCount = result.get(11, Long.class);
        Long dislikeCount = result.get(12, Long.class);
        Long viewCount = result.get(13, Long.class);

        PostDetailProjection projection = PostDetailProjection.builder()
                .id(result.get(0, UUID.class))
                .title(result.get(1, String.class))
                .content(result.get(2, String.class))
                .authorId(result.get(3, UUID.class))
                .authorType(result.get(4, org.example.deuknetdomain.domain.post.AuthorType.class) != null
                        ? result.get(4, org.example.deuknetdomain.domain.post.AuthorType.class).name()
                        : null)
                .status(result.get(5, org.example.deuknetdomain.domain.post.PostStatus.class).name())
                .thumbnailImageUrl(result.get(6, String.class))
                .createdAt(result.get(7, java.time.LocalDateTime.class))
                .updatedAt(result.get(8, java.time.LocalDateTime.class))
                .categoryId(result.get(9, UUID.class))
                .commentCount(commentCount != null ? commentCount : 0L)
                .likeCount(likeCount != null ? likeCount : 0L)
                .dislikeCount(dislikeCount != null ? dislikeCount : 0L)
                .viewCount(viewCount != null ? viewCount : 0L)
                .build();

        return Optional.of(projection);
    }

    @Override
    public List<PostDetailProjection> findLikedPostsByUserId(UUID userId, int offset, int limit) {
        // reactions와 posts를 JOIN하여 좋아요 누른 게시글 조회 (서브쿼리로 N+1 방지)
        List<Tuple> results = queryFactory
                .select(
                        postEntity.id,
                        postEntity.title,
                        postEntity.content,
                        postEntity.authorId,
                        postEntity.authorType,
                        postEntity.status,
                        postEntity.thumbnailImageUrl,
                        postEntity.createdAt,
                        postEntity.updatedAt,
                        postEntity.categoryId,
                        reactionEntity.createdAt,  // reaction 생성 시간 (정렬용)
                        JPAExpressions.select(commentEntity.count())
                                .from(commentEntity)
                                .where(commentEntity.postId.eq(postEntity.id)),
                        JPAExpressions.select(reactionEntity.count())
                                .from(reactionEntity)
                                .where(
                                        reactionEntity.targetId.eq(postEntity.id),
                                        reactionEntity.reactionType.eq(ReactionType.LIKE)
                                ),
                        JPAExpressions.select(reactionEntity.count())
                                .from(reactionEntity)
                                .where(
                                        reactionEntity.targetId.eq(postEntity.id),
                                        reactionEntity.reactionType.eq(ReactionType.DISLIKE)
                                ),
                        JPAExpressions.select(reactionEntity.count())
                                .from(reactionEntity)
                                .where(
                                        reactionEntity.targetId.eq(postEntity.id),
                                        reactionEntity.reactionType.eq(ReactionType.VIEW)
                                )
                )
                .from(reactionEntity)
                .join(postEntity).on(reactionEntity.targetId.eq(postEntity.id))
                .where(
                        reactionEntity.userId.eq(userId),
                        reactionEntity.reactionType.eq(ReactionType.LIKE),
                        reactionEntity.targetType.eq(TargetType.POST)
                )
                .orderBy(reactionEntity.createdAt.desc())  // 최신 좋아요 순
                .offset(offset)
                .limit(limit)
                .fetch();

        List<PostDetailProjection> projections = new ArrayList<>();

        for (Tuple result : results) {
            Long commentCount = result.get(11, Long.class);
            Long likeCount = result.get(12, Long.class);
            Long dislikeCount = result.get(13, Long.class);
            Long viewCount = result.get(14, Long.class);

            PostDetailProjection projection = PostDetailProjection.builder()
                    .id(result.get(0, UUID.class))
                    .title(result.get(1, String.class))
                    .content(result.get(2, String.class))
                    .authorId(result.get(3, UUID.class))
                    .authorType(result.get(4, org.example.deuknetdomain.domain.post.AuthorType.class) != null
                            ? result.get(4, org.example.deuknetdomain.domain.post.AuthorType.class).name()
                            : null)
                    .status(result.get(5, org.example.deuknetdomain.domain.post.PostStatus.class).name())
                    .thumbnailImageUrl(result.get(6, String.class))
                    .createdAt(result.get(7, java.time.LocalDateTime.class))
                    .updatedAt(result.get(8, java.time.LocalDateTime.class))
                    .categoryId(result.get(9, UUID.class))
                    .commentCount(commentCount != null ? commentCount : 0L)
                    .likeCount(likeCount != null ? likeCount : 0L)
                    .dislikeCount(dislikeCount != null ? dislikeCount : 0L)
                    .viewCount(viewCount != null ? viewCount : 0L)
                    .build();

            projections.add(projection);
        }

        return projections;
    }

    @Override
    public long countLikedPostsByUserId(UUID userId) {
        Long count = queryFactory
                .select(reactionEntity.count())
                .from(reactionEntity)
                .where(
                        reactionEntity.userId.eq(userId),
                        reactionEntity.reactionType.eq(ReactionType.LIKE),
                        reactionEntity.targetType.eq(TargetType.POST)
                )
                .fetchOne();

        return count != null ? count : 0L;
    }
}
