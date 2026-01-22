package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.GetPostUseCase;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.permission.exception.AnonymousAccessDeniedException;
import org.example.deuknetdomain.domain.post.AuthorType;
import org.example.deuknetdomain.domain.post.exception.PostNotFoundException;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 게시글 단일 조회 유스케이스 구현
 * SRP: PostgreSQL 직접 조회 (Category JOIN 포함)
 */
@Service
@Transactional(readOnly = true)
public class GetPostService implements GetPostUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPostService.class);

    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;
    private final UserRepository userRepository;

    public GetPostService(
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
    public PostSearchResponse getPostById(UUID postId, boolean forceCommandModel) {

        // 단일 조회는 항상 PostgreSQL에서 조회 (categoryName JOIN 포함)
        PostSearchResponse response = fetchFromPostgreSQL(postId);

        // 익명 게시물 조회 권한 검증
        validateAnonymousAccessForView(response);

        // 현재 사용자 정보 enrichment (isAuthor, reaction 등) - 익명 마스킹 전에 수행
        enrichWithUserReaction(response, postId);

        // User 정보 enrichment (익명이면 authorId를 null로 마스킹)
        enrichWithUserInfo(response);

        return response;
    }

    /**
     * 익명 게시물 조회 시 권한 검증
     * 익명 게시물을 조회하려면 canAccessAnonymous 권한이 필요
     */
    private void validateAnonymousAccessForView(PostSearchResponse response) {
        if (!AuthorType.ANONYMOUS.equals(response.getAuthorType())) {
            return; // 실명 게시물은 권한 체크 불필요
        }

        try {
            UUID currentUserId = currentUserPort.getCurrentUserId();
            User user = userRepository.findById(currentUserId).orElse(null);

            if (user == null || !user.isCanAccessAnonymous()) {
                log.warn("[ANONYMOUS_ACCESS_DENIED] userId={}, postId={}, attempted to view anonymous post without permission",
                        currentUserId, response.getId());
                throw new AnonymousAccessDeniedException();
            }
        } catch (AnonymousAccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            // 비인증 사용자가 익명 게시물 조회 시도
            log.warn("[ANONYMOUS_ACCESS_DENIED] Unauthenticated user attempted to view anonymous post: postId={}",
                    response.getId());
            throw new AnonymousAccessDeniedException();
        }
    }

    /**
     * PostgreSQL에서 게시글 상세 정보를 QueryDSL로 직접 조회 (Category JOIN 포함)
     *
     * @param postId 조회할 게시글 ID
     * @return 게시글 상세 정보
     * @throws PostNotFoundException 게시글이 존재하지 않는 경우
     */
    private PostSearchResponse fetchFromPostgreSQL(UUID postId) {
        log.debug("Fetching post directly from PostgreSQL: postId={}", postId);
        PostDetailProjection projection = postRepository.findDetailById(postId)
                .orElseThrow(PostNotFoundException::new);

        return PostSearchResponse.fromProjection(projection);
    }

    /**
     * 익명 여부에 따라 User 정보를 조회하여 설정
     * UserRepository.enrichWithUserInfo를 통해 ANONYMOUS/REAL 처리
     */
    private void enrichWithUserInfo(PostSearchResponse response) {
        userRepository.enrichWithUserInfo(response);
    }

    private void enrichWithUserReaction(PostSearchResponse response, UUID postId) {
        try {
            UUID currentUserId = currentUserPort.getCurrentUserId();

            // 작성자 여부 확인 (익명 게시물도 authorId로 확인 가능)
            response.setIsAuthor(
                    Optional.ofNullable(response.getAuthorId())
                            .map(authorId -> authorId.equals(currentUserId))
                            .orElse(false)
            );

            // 한 번의 쿼리로 모든 reaction 조회 (LIKE, DISLIKE 포함)
            var reactions = reactionRepository.findByTargetIdAndUserId(postId, currentUserId);

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
        } catch (Exception e) {
            // 인증되지 않은 사용자 (ForbiddenException 등)
            setUnauthenticatedUserDefaults(response);
        }
    }

    /**
     * 인증되지 않은 사용자의 기본값 설정
     */
    private void setUnauthenticatedUserDefaults(PostSearchResponse response) {
        response.setIsAuthor(false);
        response.setHasUserLiked(false);
        response.setUserLikeReactionId(null);
        response.setHasUserDisliked(false);
        response.setUserDislikeReactionId(null);
    }
}
