package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.GetMyLikedPostsUseCase;
import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 내가 좋아요 누른 게시글 조회 유스케이스 구현
 * PostgreSQL에서 reactions와 posts를 JOIN하여 직접 조회 (Elasticsearch 불필요)
 */
@Service
@Transactional(readOnly = true)
public class GetMyLikedPostsService implements GetMyLikedPostsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetMyLikedPostsService.class);

    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final UserRepository userRepository;

    public GetMyLikedPostsService(
            PostRepository postRepository,
            ReactionRepository reactionRepository,
            CurrentUserPort currentUserPort,
            UserRepository userRepository
    ) {
        this.postRepository = postRepository;
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
        this.userRepository = userRepository;
    }

    @Override
    public PageResponse<PostSearchResponse> getMyLikedPosts(int page, int size) {
        // CurrentUserPort를 통해 현재 사용자 ID 조회 (CLAUDE.md 원칙)
        UUID currentUserId = currentUserPort.getCurrentUserId();
        log.debug("Fetching liked posts for user: userId={}, page={}, size={}", currentUserId, page, size);

        // 1. 총 개수 조회
        long totalElements = postRepository.countLikedPostsByUserId(currentUserId);

        // 2. 페이지네이션 적용하여 조회 (QueryDSL JOIN)
        int offset = page * size;
        List<PostSearchResponse> posts = postRepository.findLikedPostsByUserId(currentUserId, offset, size)
                .stream()
                .map(PostSearchResponse::fromProjection)
                .toList();

        // 3. 각 게시글에 사용자 정보 enrichment
        posts.forEach(post -> enrichPostResponse(post, currentUserId));

        log.debug("Successfully fetched {} liked posts out of {} total", posts.size(), totalElements);
        return new PageResponse<>(posts, totalElements, page, size);
    }

    /**
     * 게시글 응답 enrichment (isAuthor 체크 → User 정보 설정)
     */
    private void enrichPostResponse(PostSearchResponse response, UUID currentUserId) {
        // 1. 작성자 여부 확인 (익명 게시물도 authorId로 확인 가능)
        response.setIsAuthor(
                Optional.ofNullable(response.getAuthorId())
                        .map(authorId -> authorId.equals(currentUserId))
                        .orElse(false)
        );

        // 2. 좋아요/싫어요 정보 설정 (이미 좋아요 누른 게시글이므로 hasUserLiked=true)
        var reactions = reactionRepository.findByTargetIdAndUserId(response.getId(), currentUserId);

        // LIKE reaction 처리
        reactions.stream()
                .filter(reaction -> reaction.getReactionType() == ReactionType.LIKE)
                .findFirst()
                .ifPresentOrElse(
                        like -> {
                            response.setHasUserLiked(true);
                            response.setUserLikeReactionId(like.getId());
                        },
                        () -> {
                            response.setHasUserLiked(false);
                            response.setUserLikeReactionId(null);
                        }
                );

        // DISLIKE reaction 처리
        reactions.stream()
                .filter(reaction -> reaction.getReactionType() == ReactionType.DISLIKE)
                .findFirst()
                .ifPresentOrElse(
                        dislike -> {
                            response.setHasUserDisliked(true);
                            response.setUserDislikeReactionId(dislike.getId());
                        },
                        () -> {
                            response.setHasUserDisliked(false);
                            response.setUserDislikeReactionId(null);
                        }
                );

        // 3. User 정보 enrichment (익명이면 authorId를 null로 마스킹)
        userRepository.enrichWithUserInfo(response);
    }
}
