package org.example.deuknetapplication.service.reaction;

import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.reaction.AddReactionUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetapplication.service.post.PostProjectionFactory;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.reaction.Reaction;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.reaction.TargetType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Reaction 추가 서비스

 * 책임:
 * - Reaction 생성 (LIKE, DISLIKE, VIEW)
 * - LIKE/DISLIKE 배타적 처리 (둘 중 하나만 가능)
 * - PostDetailProjection 업데이트 이벤트 발행

 * 참고:
 * - VIEW(조회수)는 중복이 허용됩니다 (동시 요청 시 여러 개 생성 가능)
 * - LIKE/DISLIKE는 배타적이므로, 하나를 누르면 반대쪽이 자동으로 삭제됩니다
 */
@Service
@Transactional
public class AddReactionService implements AddReactionUseCase {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;
    private final PostProjectionFactory projectionFactory;

    public AddReactionService(
            ReactionRepository reactionRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher,
            PostProjectionFactory projectionFactory) {
        this.reactionRepository = reactionRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
        this.projectionFactory = projectionFactory;
    }

    @Override
    public UUID addReaction(AddReactionCommand command) {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        Reaction reaction = switch (command.reactionType()) {
            case VIEW -> handleView(currentUserId, command);
            case LIKE, DISLIKE -> handleLikeAndDislike(currentUserId, command);
        };

        return reaction.getId();
    }

    private Reaction handleView(UUID userId, AddReactionCommand command) {

        Reaction reaction = Reaction.create(
                command.reactionType(),
                TargetType.POST,
                command.targetId(),
                userId
        );

        if (reactionRepository.findByTargetIdAndUserIdAndReactionType(
                command.targetId(),
                userId,
                command.reactionType())
                .isEmpty()) {
            reactionRepository.save(reaction);
            publishReactionEvent(command.targetId(), command.reactionType());
        }

        return reaction;
    }

    private Reaction handleLikeAndDislike(UUID userId, AddReactionCommand command) {
        ReactionType oppositeType = command.reactionType() == ReactionType.LIKE
                ? ReactionType.DISLIKE
                : ReactionType.LIKE;

        reactionRepository.findByTargetIdAndUserIdAndReactionType(
                command.targetId(),
                userId,
                oppositeType
        ).ifPresent(existingReaction -> {
            reactionRepository.delete(existingReaction);
            // 반대 반응 삭제 이벤트 발행
            publishReactionEvent(command.targetId(), oppositeType);
        });

        // 같은 타입의 LIKE/DISLIKE가 이미 있는지 확인
        Optional<Reaction> existing = reactionRepository.findByTargetIdAndUserIdAndReactionType(
                command.targetId(),
                userId,
                command.reactionType()
        );

        Reaction reaction = existing.orElseGet(() -> Reaction.create(
                command.reactionType(),
                TargetType.POST,
                command.targetId(),
                userId
        ));

        reactionRepository.save(reaction);
        publishReactionEvent(command.targetId(), command.reactionType());

        return reaction;
    }

    /**
     * PostDetailProjection 생성 및 이벤트 발행
     * Reaction 변경 시 전체 통계를 업데이트
     */
    private void publishReactionEvent(UUID targetId, ReactionType reactionType) {
        // Post 조회
        Post post = postRepository.findById(targetId)
                .orElseThrow(ResourceNotFoundException::new);

        // 모든 통계 조회
        long commentCount = commentRepository.countByPostId(targetId);
        long likeCount = reactionRepository.countByTargetIdAndReactionType(targetId, ReactionType.LIKE);
        long dislikeCount = reactionRepository.countByTargetIdAndReactionType(targetId, ReactionType.DISLIKE);
        long viewCount = reactionRepository.countByTargetIdAndReactionType(targetId, ReactionType.VIEW);

        // PostDetailProjection 생성
        PostDetailProjection projection = projectionFactory.createDetailProjectionForUpdate(
                post,
                post.getCategoryId(),
                commentCount,
                likeCount,
                dislikeCount,
                viewCount
        );

        dataChangeEventPublisher.publish(EventType.REACTION_ADDED, targetId, projection);
    }
}
