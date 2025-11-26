package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.GetPostByIdUseCase;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.external.search.PostSearchPort;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.post.exception.PostNotFoundException;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 게시글 ID로 단일 게시글을 조회하는 유스케이스 구현
 * SRP: Elasticsearch 우선 조회 전략 + PostgreSQL 폴백 담당
 *
 * 조회 전략:
 * 1. Elasticsearch에서 먼저 조회 (빠른 성능)
 * 2. 없으면 PostgreSQL에서 QueryDSL로 조회 (정합성 보장)
 */
@Service
@Transactional(readOnly = true)
public class GetPostByIdService implements GetPostByIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPostByIdService.class);

    private final PostSearchPort postSearchPort;
    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;
    private final CurrentUserPort currentUserPort;

    public GetPostByIdService(
            PostSearchPort postSearchPort,
            PostRepository postRepository,
            ReactionRepository reactionRepository,
            CurrentUserPort currentUserPort
    ) {
        this.postSearchPort = postSearchPort;
        this.postRepository = postRepository;
        this.reactionRepository = reactionRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public PostSearchResponse getPostById(UUID postId) {
        // Phase 2 전략: Elasticsearch 우선 조회 + PostgreSQL 폴백

        // 1. Elasticsearch에서 조회 시도
        Optional<PostSearchResponse> elasticsearchResult = postSearchPort.findById(postId);

        PostSearchResponse response;
        if (elasticsearchResult.isPresent()) {
            log.debug("Post found in Elasticsearch: postId={}", postId);
            response = elasticsearchResult.get();
        } else {
            // 2. PostgreSQL에서 조회 (폴백)
            log.debug("Fetching post from PostgreSQL: postId={}", postId);
            response = fetchFromCommandModel(postId);
        }

        // 3. 현재 사용자의 reaction 조회 및 설정
        enrichWithUserReaction(response, postId);

        return response;
    }

    /**
     * PostgreSQL에서 게시글 상세 정보를 QueryDSL로 조회
     * 현재 CDC 구조에서 데이터 동기화 지연시간 때문에 조회 불가능한 문제를 해결하기 위해 Command에서 조회
     *
     * @param postId 조회할 게시글 ID
     * @return 게시글 상세 정보
     * @throws PostNotFoundException 게시글이 존재하지 않는 경우
     */
    private PostSearchResponse fetchFromCommandModel(UUID postId) {
        PostDetailProjection projection = postRepository.findDetailById(postId)
                .orElseThrow(PostNotFoundException::new);

        return new PostSearchResponse(projection);
    }

    /**
     * 현재 사용자의 reaction 정보 및 작성자 여부를 응답에 추가
     * 인증되지 않은 사용자의 경우 false로 설정
     * 성능 최적화: 1번의 쿼리로 LIKE, DISLIKE 모두 조회
     *
     * @param response 응답 객체
     * @param postId 게시글 ID
     */
    private void enrichWithUserReaction(PostSearchResponse response, UUID postId) {
        try {
            UUID currentUserId = currentUserPort.getCurrentUserId();

            // 작성자 여부 확인
            response.setIsAuthor(response.getAuthorId().equals(currentUserId));

            // 한 번의 쿼리로 모든 reaction 조회 (LIKE, DISLIKE 포함)
            java.util.List<org.example.deuknetdomain.domain.reaction.Reaction> reactions =
                    reactionRepository.findByTargetIdAndUserId(postId, currentUserId);

            // 기본값 설정
            response.setHasUserLiked(false);
            response.setUserLikeReactionId(null);
            response.setHasUserDisliked(false);
            response.setUserDislikeReactionId(null);

            // reaction 타입별로 분류
            for (var reaction : reactions) {
                if (reaction.getReactionType() == ReactionType.LIKE) {
                    response.setHasUserLiked(true);
                    response.setUserLikeReactionId(reaction.getId());
                } else if (reaction.getReactionType() == ReactionType.DISLIKE) {
                    response.setHasUserDisliked(true);
                    response.setUserDislikeReactionId(reaction.getId());
                }
            }
        } catch (Exception e) {
            // 인증되지 않은 사용자 (ForbiddenException 등)
            response.setIsAuthor(false);
            response.setHasUserLiked(false);
            response.setHasUserDisliked(false);
            response.setUserLikeReactionId(null);
            response.setUserDislikeReactionId(null);
        }
    }
}
