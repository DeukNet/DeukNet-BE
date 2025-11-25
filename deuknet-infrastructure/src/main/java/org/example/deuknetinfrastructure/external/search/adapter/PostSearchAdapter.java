package org.example.deuknetinfrastructure.external.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.external.search.PostSearchPort;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetinfrastructure.external.search.document.PostDetailDocument;
import org.example.deuknetinfrastructure.external.search.exception.SearchOperationException;
import org.example.deuknetinfrastructure.external.search.mapper.PostDetailDocumentMapper;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 게시글 검색 Adapter (Elasticsearch)
 * <br>
 * PostSearchPort 구현 (out port)
 * - SearchPostService, GetPostByIdService가 사용
 *
 * todo 문제가 상당히 많은 코드
 * 1. debezium과 search의 책임 분리
 * 2. 너무 최적화 덜된코드
 * 3. ReactionRepository(다른 Aggregate)를 직접 참조????
 * 4. 심지어 Reaction가져오는 부분이 상상이상으로 능딸임(느림)
 */
@Component
@RequiredArgsConstructor
public class PostSearchAdapter implements PostSearchPort {

    private static final String INDEX_NAME = "posts-detail";
    private final ElasticsearchClient elasticsearchClient;
    private final CurrentUserPort currentUserPort;
    private final ReactionRepository reactionRepository;
    private final PostDetailDocumentMapper mapper;

    @Override
    public Optional<PostSearchResponse> findById(UUID id) {
        try {
            var document = elasticsearchClient.get(g -> g
                    .index(INDEX_NAME)
                    .id(id.toString()),
                PostDetailDocument.class
            );

            if (document.found()) {
                return Optional.ofNullable(document.source())
                        .map(mapper::toProjection)
                        .map(PostSearchResponse::new);
            }
            return Optional.empty();
        } catch (ElasticsearchException e) {
            if (e.getMessage() != null && e.getMessage().contains("index_not_found_exception")) {
                return Optional.empty();
            }
            throw new SearchOperationException("Failed to find post by id: " + id, e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to find post by id: " + id, e);
        }
    }

    @Override
    public PageResponse<PostSearchResponse> search(PostSearchRequest request) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 키워드 검색 (must) - 제목/내용 전문 검색
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            boolQueryBuilder.must(Query.of(q -> q
                .multiMatch(m -> m
                    .query(request.getKeyword())
                    .fields("title^2", "content")  // title에 2배 가중치
                )
            ));
        }

        // 작성자 필터 (filter)
        if (request.getAuthorId() != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("authorId").value(request.getAuthorId().toString()))
            ));
        }

        // 카테고리 필터 (filter)
        if (request.getCategoryId() != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("categoryIds").value(request.getCategoryId().toString()))
            ));
        }

        // 상태 필터 (filter)
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("status").value(request.getStatus()))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

        // 정렬 순서 변환
        SortOrder sortOrder = "asc".equalsIgnoreCase(request.getSortOrder())
                ? SortOrder.Asc
                : SortOrder.Desc;

        return executeSearch(boolQuery, request.getPage(), request.getSize(), request.getSortBy(), sortOrder);
    }

    @Override
    public PageResponse<PostSearchResponse> findPopularPosts(int page, int size, UUID categoryId) {
        Query query;

        if (categoryId != null) {
            // 카테고리 필터링 적용
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("categoryIds").value(categoryId.toString()))
            ));
            query = Query.of(q -> q.bool(boolQueryBuilder.build()));
        } else {
            // 전체 게시물
            query = Query.of(q -> q.matchAll(m -> m));
        }

        return executePopularSearch(query, page, size);
    }

    /**
     * 인기 게시물 검색 (추천수 * 3 + 조회수 * 1)
     */
    private PageResponse<PostSearchResponse> executePopularSearch(Query query, int page, int size) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .query(query)
                .from(page * size)
                .size(size)
                .sort(sort -> sort
                    .script(script -> script
                        .type(co.elastic.clients.elasticsearch._types.ScriptSortType.Number)
                        .script(sc -> sc
                            .inline(inline -> inline
                                .source("(doc['likeCount'].value * 3) + (doc['viewCount'].value * 1)")
                            )
                        )
                        .order(SortOrder.Desc)
                    )
                )
            );

            SearchResponse<PostDetailDocument> response = elasticsearchClient.search(
                searchRequest,
                PostDetailDocument.class
            );

            List<PostSearchResponse> results = response.hits().hits().stream()
                .map(Hit::source)
                .map(mapper::toProjection)
                .map(PostSearchResponse::new)
                .collect(Collectors.toList());

            // 각 결과에 사용자 정보 추가
            results.forEach(this::enrichWithUserInfo);

            long totalElements = response.hits().total() != null ? response.hits().total().value() : 0;
            return new PageResponse<>(results, totalElements, page, size);

        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            // 인덱스가 없는 경우 빈 페이지 반환
            if (e.getMessage() != null && (e.getMessage().contains("index_not_found_exception")
                    || e.getMessage().contains("all shards failed"))) {
                return new PageResponse<>(List.of(), 0, page, size);
            }
            throw new SearchOperationException("Failed to execute popular search", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to execute popular search", e);
        }
    }

    /**
     * 공통 검색 실행 메서드
     */
    private PageResponse<PostSearchResponse> executeSearch(
            Query query,
            int page,
            int size,
            String sortField,
            SortOrder sortOrder
    ) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .query(query)
                .from(page * size)
                .size(size)
                .sort(sort -> sort
                    .field(f -> f
                        .field(sortField)
                        .order(sortOrder)
                    )
                )
            );

            SearchResponse<PostDetailDocument> response = elasticsearchClient.search(
                searchRequest,
                PostDetailDocument.class
            );

            List<PostSearchResponse> results = response.hits().hits().stream()
                .map(Hit::source)
                .map(mapper::toProjection)
                .map(PostSearchResponse::new)
                .collect(Collectors.toList());

            // 각 결과에 사용자 정보 추가
            results.forEach(this::enrichWithUserInfo);

            long totalElements = response.hits().total() != null ? response.hits().total().value() : 0;
            return new PageResponse<>(results, totalElements, page, size);

        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            // 인덱스가 없는 경우 빈 페이지 반환
            if (e.getMessage() != null && (e.getMessage().contains("index_not_found_exception")
                    || e.getMessage().contains("all shards failed"))) {
                return new PageResponse<>(List.of(), 0, page, size);
            }
            throw new SearchOperationException("Failed to execute search", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to execute search", e);
        }
    }

    /**
     * 현재 사용자의 reaction 정보 및 작성자 여부를 응답에 추가
     * 인증되지 않은 사용자의 경우 false로 설정
     *
     * @param response 응답 객체
     * todo 성능을 잡아먹는 나쁜 친구, 단일 조회 시에만 사용하도록하자 (최대 응답시간 189ms..)
     */
    private void enrichWithUserInfo(PostSearchResponse response) {
        try {
            UUID currentUserId = currentUserPort.getCurrentUserId();

            // 작성자 여부 확인
            response.setIsAuthor(response.getAuthorId().equals(currentUserId));

            // LIKE 확인 // todo join으로 처리하기
            reactionRepository.findByTargetIdAndUserIdAndReactionType(
                    response.getId(), currentUserId, ReactionType.LIKE
            ).ifPresentOrElse(
                    likeReaction -> {
                        response.setHasUserLiked(true);
                        response.setUserLikeReactionId(likeReaction.getId());
                    },
                    () -> {
                        response.setHasUserLiked(false);
                        response.setUserLikeReactionId(null);
                    }
            );

            // DISLIKE 확인
            reactionRepository.findByTargetIdAndUserIdAndReactionType(
                    response.getId(), currentUserId, ReactionType.DISLIKE
            ).ifPresentOrElse(
                    dislikeReaction -> {
                        response.setHasUserDisliked(true);
                        response.setUserDislikeReactionId(dislikeReaction.getId());
                    },
                    () -> {
                        response.setHasUserDisliked(false);
                        response.setUserDislikeReactionId(null);
                    }
            );
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
