package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.GetPostByIdUseCase;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.post.PostSearchPort;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.post.exception.PostNotFoundException;
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

    public GetPostByIdService(
            PostSearchPort postSearchPort,
            PostRepository postRepository
    ) {
        this.postSearchPort = postSearchPort;
        this.postRepository = postRepository;
    }

    @Override
    public PostSearchResponse getPostById(UUID postId) {
        // Phase 2 전략: Elasticsearch 우선 조회 + PostgreSQL 폴백

        // 1. Elasticsearch에서 조회 시도
        Optional<PostSearchResponse> elasticsearchResult = postSearchPort.findById(postId);

        if (elasticsearchResult.isPresent()) {
            log.debug("Post found in Elasticsearch: postId={}", postId);
            return elasticsearchResult.get();
        }

        // 2. PostgreSQL에서 조회 (폴백)
        log.debug("Fetching post from PostgreSQL: postId={}", postId);
        return fetchFromCommandModel(postId);
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
}
