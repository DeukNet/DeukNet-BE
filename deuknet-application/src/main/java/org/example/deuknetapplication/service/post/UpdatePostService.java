package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.post.UpdatePostApplcationRequest;
import org.example.deuknetapplication.port.in.post.UpdatePostUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.common.vo.Title;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Post 수정 유스케이스 구현체 (진짜 SRP 준수)
 *
 * 단일 책임: Post 수정 유스케이스의 흐름을 조정(orchestration)
 *
 * - Post 수정 도메인 로직만 직접 처리
 * - 나머지 책임은 각 전문 서비스에 위임
 */
@Service
@Transactional
public class UpdatePostService implements UpdatePostUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final PostProjectionFactory postProjectionFactory;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public UpdatePostService(
            PostRepository postRepository,
            UserRepository userRepository,
            CommentRepository commentRepository,
            ReactionRepository reactionRepository,
            CurrentUserPort currentUserPort,
            PostProjectionFactory postProjectionFactory,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
        this.postProjectionFactory = postProjectionFactory;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public void updatePost(UpdatePostApplcationRequest request) {
        // 1. Post 조회 및 권한 검증 (핵심 도메인 로직)
        Post post = getPostAndVerifyOwnership(request.getPostId());

        // 2. Post 내용 및 카테고리 업데이트 (핵심 도메인 로직)
        updatePostContent(post, request);

        // 3. 통계 조회
        long commentCount = commentRepository.countByPostId(post.getId());
        long likeCount = reactionRepository.countByTargetIdAndReactionType(
                post.getId(), ReactionType.LIKE);
        long dislikeCount = reactionRepository.countByTargetIdAndReactionType(
                post.getId(), ReactionType.DISLIKE);
        long viewCount = reactionRepository.countByTargetIdAndReactionType(
                post.getId(), ReactionType.VIEW);

        // 4. Projection 생성 (전문 팩토리에 위임)
        PostDetailProjection detailProjection = postProjectionFactory.createDetailProjectionForUpdate(
                post, request.getCategoryId(), commentCount, likeCount, dislikeCount, viewCount
        );

        // 5. 이벤트 발행
        dataChangeEventPublisher.publish(EventType.POST_UPDATED, post.getId(), detailProjection);
    }

    /**
     * Post 조회 및 소유권 검증
     */
    private Post getPostAndVerifyOwnership(UUID postId) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(ResourceNotFoundException::new);

        if (!post.getAuthorId().equals(currentUserId)) {
            throw new OwnerMismatchException();
        }

        return post;
    }

    /**
     * Post 내용 및 카테고리 업데이트
     */
    private void updatePostContent(Post post, UpdatePostApplcationRequest request) {
        post.updateContent(
                Title.from(request.getTitle()),
                Content.from(request.getContent())
        );
        post.updateCategory(request.getCategoryId());

        // 썸네일 이미지 URL 업데이트 (null 허용)
        if (request.getThumbnailImageUrl() != null) {
            post.updateThumbnailImageUrl(request.getThumbnailImageUrl());
        }

        postRepository.save(post);
    }

    /**
     * 작성자 조회
     */
    private User getAuthor(UUID authorId) {
        return userRepository.findById(authorId)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
