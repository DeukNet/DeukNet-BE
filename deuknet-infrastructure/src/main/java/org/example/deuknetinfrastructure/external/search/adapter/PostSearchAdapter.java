package org.example.deuknetinfrastructure.external.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.external.search.PostSearchPort;
import org.example.deuknetdomain.domain.post.PostStatus;
import org.example.deuknetinfrastructure.external.search.document.PostDetailDocument;
import org.example.deuknetinfrastructure.external.search.exception.SearchOperationException;
import org.example.deuknetinfrastructure.external.search.mapper.PostDetailDocumentMapper;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 게시글 검색 Adapter (Elasticsearch)
 * PostSearchPort 구현 (out port)
 */
@Component
@RequiredArgsConstructor
public class PostSearchAdapter implements PostSearchPort {

    private static final String INDEX_NAME = "posts-detail";
    private final ElasticsearchClient elasticsearchClient;
    private final PostDetailDocumentMapper mapper;

    /**
     * 검색 설정 (가중치, 스코어 임계값 등)
     */
    private static class SearchConfig {
        // 최신순 검색 가중치 (검색어는 필터 역할만)
        static final double RECENT_TITLE_BOOST = 3.0;
        static final double RECENT_CONTENT_BOOST = 1.0;

        // 인기순 검색 가중치 (검색어 + 인기도 균형)
        static final double POPULAR_TITLE_BOOST = 5.0;
        static final double POPULAR_CONTENT_BOOST = 1.0;

        // 정확도순 검색 가중치 (검색어 매칭이 가장 중요)
        static final double RELEVANCE_TITLE_BOOST = 5.0;
        static final double RELEVANCE_CONTENT_BOOST = 1.0;
        static final double RELEVANCE_MIN_SCORE = 2.0;

        // minimumShouldMatch 설정
        static final int MIN_TOKENS_FOR_STRICT_MATCH = 4;
        static final int STRICT_MATCH_TOLERANCE = 2;
    }

    // ==================== Public API Methods ====================

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
                        .map(doc -> mapper.toProjection(doc, null, null, null))
                        .map(PostSearchResponse::fromProjection);
            }
            return Optional.empty();
        } catch (ElasticsearchException e) {
            if (isIndexNotFound(e)) {
                return Optional.empty();
            }
            throw new SearchOperationException("Failed to find post by id: " + id, e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to find post by id: " + id, e);
        }
    }

    @Override
    public List<PostSearchResponse> findByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        try {
            var response = elasticsearchClient.mget(m -> m
                    .index(INDEX_NAME)
                    .ids(ids.stream().map(UUID::toString).collect(Collectors.toList())),
                PostDetailDocument.class
            );

            return response.docs().stream()
                    .filter(doc -> !doc.isFailure() && doc.result().found())
                    .map(doc -> doc.result().source())
                    .filter(Objects::nonNull)
                    .map(doc -> mapper.toProjection(doc, null, null, null))
                    .map(PostSearchResponse::fromProjection)
                    .collect(Collectors.toList());
        } catch (ElasticsearchException e) {
            if (isIndexNotFound(e)) {
                return List.of();
            }
            throw new SearchOperationException("Failed to find posts by ids", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to find posts by ids", e);
        }
    }

    @Override
    @Deprecated
    public PageResponse<PostSearchResponse> search(PostSearchRequest request) {
        throw new UnsupportedOperationException("Use sortType-specific methods instead");
    }

    /**
     * 최신순 검색
     * - 정렬: 작성일 기준 내림차순 (최신 글이 위로)
     * - 검색어: 필터 역할 (가중치 낮음)
     * - 용도: 전체 게시물 최신 순으로 탐색
     */
    @Override
    public PageResponse<PostSearchResponse> searchByRecent(String keyword, UUID authorId, UUID categoryId, int page, int size, boolean includeAnonymous) {
        Query query = buildSearchQuery(keyword, authorId, categoryId, includeAnonymous,
                SearchConfig.RECENT_TITLE_BOOST, SearchConfig.RECENT_CONTENT_BOOST);
        return executeSearch(query, page, size, "createdAt", SortOrder.Desc);
    }

    /**
     * 인기순 검색
     * - 정렬: 인기도 점수 기준 (좋아요 * 3 + 조회수 * 1, 30일 이상 시 감쇠)
     * - 검색어: 필터 + 부스트 (가중치 중간)
     * - 용도: 인기 있는 게시물 탐색
     */
    @Override
    public PageResponse<PostSearchResponse> searchByPopular(String keyword, UUID authorId, UUID categoryId, int page, int size, boolean includeAnonymous) {
        Query query = buildSearchQuery(keyword, authorId, categoryId, includeAnonymous,
                SearchConfig.POPULAR_TITLE_BOOST, SearchConfig.POPULAR_CONTENT_BOOST);
        return executePopularSearch(query, page, size);
    }

    /**
     * 정확도순 검색 (관련성)
     * - 정렬: Elasticsearch _score 기준
     * - 검색어: 매칭이 가장 중요 (가중치 높음)
     * - 용도: 특정 키워드에 대한 정확한 검색
     * - 최소 스코어: 2.0 이상만 반환
     */
    @Override
    public PageResponse<PostSearchResponse> searchByRelevance(String keyword, UUID authorId, UUID categoryId, int page, int size, boolean includeAnonymous) {
        Query query = buildSearchQuery(keyword, authorId, categoryId, includeAnonymous,
                SearchConfig.RELEVANCE_TITLE_BOOST, SearchConfig.RELEVANCE_CONTENT_BOOST);
        return executeSearchByRelevance(query, page, size);
    }

    @Override
    public List<String> suggestKeywords(String prefix, int size) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }

        try {
            Query matchQuery = Query.of(q -> q
                    .match(m -> m
                            .field("title.autocomplete")
                            .query(prefix.trim())
                    )
            );

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .query(matchQuery)
                    .size(size)
                    .source(src -> src.filter(f -> f.includes("title")))
            );

            SearchResponse<PostDetailDocument> response = elasticsearchClient.search(
                    searchRequest,
                    PostDetailDocument.class
            );

            Set<String> suggestions = new LinkedHashSet<>();
            response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(PostDetailDocument::getTitle)
                    .filter(Objects::nonNull)
                    .limit(size)
                    .forEach(suggestions::add);

            return new ArrayList<>(suggestions);

        } catch (ElasticsearchException e) {
            if (isIndexNotFoundOrShardFailed(e)) {
                return List.of();
            }
            throw new SearchOperationException("Failed to suggest keywords", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to suggest keywords", e);
        }
    }

    @Override
    public PageResponse<PostSearchResponse> findFeaturedPosts(UUID categoryId, int page, int size, boolean includeAnonymous) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        addPublicStatusFilter(boolQueryBuilder);
        addCategoryFilter(boolQueryBuilder, categoryId);
        addAnonymousFilter(boolQueryBuilder, includeAnonymous);

        Query query = Query.of(q -> q.bool(boolQueryBuilder.build()));

        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .query(query)
                    .from(page * size)
                    .size(Math.min(size, 20))
                    .sort(sort -> sort
                            .field(f -> f
                                    .field("likeCount")
                                    .order(SortOrder.Desc)
                            )
                    )
            );

            return executeSearchRequest(searchRequest, page, size);

        } catch (ElasticsearchException e) {
            if (isIndexNotFoundOrShardFailed(e)) {
                return new PageResponse<>(List.of(), 0, page, size);
            }
            throw new SearchOperationException("Failed to find featured posts", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to find featured posts", e);
        }
    }

    @Override
    public List<PostSearchResponse> findTrendingPosts(int size) {
        try {
            long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q
                            .bool(b -> b
                                    .filter(f -> f.term(t -> t.field("status").value(PostStatus.PUBLIC.name())))
                                    .filter(f -> f.range(r -> r
                                            .field("createdAt")
                                            .gte(co.elastic.clients.json.JsonData.of(oneDayAgo))
                                    ))
                            )
                    )
                    .size(size)
                    .source(src -> src.filter(f -> f.excludes("content")))
                    .sort(sort -> sort
                            .script(script -> script
                                    .type(co.elastic.clients.elasticsearch._types.ScriptSortType.Number)
                                    .script(sc -> sc
                                            .inline(inline -> inline
                                                    .source("""
                                    long now = new Date().getTime();
                                    long created = doc['createdAt'].value.toInstant().toEpochMilli();
                                    double hoursOld = (now - created) / (1000.0 * 60 * 60);
                                    double ageDecay = 1.0 + Math.pow(hoursOld / 24.0, 2);
                                    double rawScore = (doc['viewCount'].value * 0.3) + (doc['likeCount'].value * 0.7);
                                    return rawScore / ageDecay;
                                """)
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

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .map(doc -> mapper.toProjection(doc, null, null, null))
                    .map(PostSearchResponse::fromProjection)
                    .collect(Collectors.toList());

        } catch (ElasticsearchException e) {
            if (isIndexNotFoundOrShardFailed(e)) {
                return List.of();
            }
            throw new SearchOperationException("Failed to find trending posts", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to find trending posts", e);
        }
    }

    // ==================== Query Building ====================

    /**
     * 통합 검색 쿼리 생성
     */
    private Query buildSearchQuery(String keyword, UUID authorId, UUID categoryId,
                                    boolean includeAnonymous, double titleBoost, double contentBoost) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        addPublicStatusFilter(boolQueryBuilder);
        addKeywordFilter(boolQueryBuilder, keyword, titleBoost, contentBoost);
        addAuthorFilter(boolQueryBuilder, authorId);
        addCategoryFilter(boolQueryBuilder, categoryId);
        addAnonymousFilter(boolQueryBuilder, includeAnonymous);

        return Query.of(q -> q.bool(boolQueryBuilder.build()));
    }

    /**
     * PUBLIC 상태 필터 추가
     */
    private void addPublicStatusFilter(BoolQuery.Builder builder) {
        builder.filter(Query.of(q -> q
                .term(t -> t.field("status").value(PostStatus.PUBLIC.name()))
        ));
    }

    /**
     * 검색어 필터 추가
     */
    private void addKeywordFilter(BoolQuery.Builder builder, String keyword,
                                   double titleBoost, double contentBoost) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }

        List<String> keywords = parseKeywords(keyword);
        String minimumMatch = calculateMinimumMatch(keywords.size());

        builder.must(b -> b.bool(inner -> {
            keywords.forEach(kw -> inner.should(s ->
                    s.multiMatch(m -> m
                            .query(kw)
                            .fields("title^" + titleBoost, "content^" + contentBoost)
                            .operator(Operator.Or)
                    )
            ));
            return inner.minimumShouldMatch(minimumMatch);
        }));
    }

    /**
     * 작성자 필터 추가
     */
    private void addAuthorFilter(BoolQuery.Builder builder, UUID authorId) {
        if (authorId != null) {
            builder.filter(Query.of(q -> q
                    .term(t -> t.field("authorId").value(authorId.toString()))
            ));
        }
    }

    /**
     * 카테고리 필터 추가
     */
    private void addCategoryFilter(BoolQuery.Builder builder, UUID categoryId) {
        if (categoryId != null) {
            builder.filter(Query.of(q -> q
                    .term(t -> t.field("categoryId").value(categoryId.toString()))
            ));
        }
    }

    /**
     * 익명 게시물 필터 추가
     * includeAnonymous=false일 때 ANONYMOUS 게시물 제외 (REAL 또는 authorType 없는 것은 포함)
     */
    private void addAnonymousFilter(BoolQuery.Builder builder, boolean includeAnonymous) {
        if (!includeAnonymous) {
            // ANONYMOUS가 아닌 모든 게시물 포함 (REAL 또는 authorType 필드 없는 기존 데이터)
            builder.mustNot(Query.of(q -> q
                    .term(t -> t.field("authorType").value("ANONYMOUS"))
            ));
        }
    }

    /**
     * 키워드 파싱 (공백 기준 분리)
     */
    private List<String> parseKeywords(String keyword) {
        return Arrays.stream(keyword.trim().split("\\s+"))
                .filter(k -> !k.isBlank())
                .toList();
    }

    /**
     * minimumShouldMatch 계산
     * - 1-3개 토큰: 최소 1개 매칭
     * - 4개 이상 토큰: N-2 매칭 (예: 4토큰=2개, 5토큰=3개, 6토큰=4개)
     */
    private String calculateMinimumMatch(int tokenCount) {
        if (tokenCount >= SearchConfig.MIN_TOKENS_FOR_STRICT_MATCH) {
            return String.valueOf(tokenCount - SearchConfig.STRICT_MATCH_TOLERANCE);
        }
        return "1";
    }

    // ==================== Search Execution ====================

    /**
     * 인기 게시물 검색 (Script 정렬)
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
                                                    .source("""
                                    long now = new Date().getTime();
                                    long created = doc['createdAt'].value.toInstant().toEpochMilli();
                                    long daysDiff = (now - created) / (1000L * 60 * 60 * 24);
                                    double timeDecay = daysDiff > 30 ? 0.75 : 1.0;
                                    double popularity = (doc['likeCount'].value * 3) + (doc['viewCount'].value * 1);
                                    return popularity * timeDecay;
                                """)
                                            )
                                    )
                                    .order(SortOrder.Desc)
                            )
                    )
            );

            return executeSearchRequest(searchRequest, page, size);

        } catch (ElasticsearchException e) {
            if (isIndexNotFoundOrShardFailed(e)) {
                return new PageResponse<>(List.of(), 0, page, size);
            }
            throw new SearchOperationException("Failed to execute popular search", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to execute popular search", e);
        }
    }

    /**
     * 관련성 검색 (_score 기준)
     */
    private PageResponse<PostSearchResponse> executeSearchByRelevance(Query query, int page, int size) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .query(query)
                    .from(page * size)
                    .size(size)
                    .minScore(SearchConfig.RELEVANCE_MIN_SCORE)
            );

            return executeSearchRequest(searchRequest, page, size);

        } catch (ElasticsearchException e) {
            if (isIndexNotFoundOrShardFailed(e)) {
                return new PageResponse<>(List.of(), 0, page, size);
            }
            throw new SearchOperationException("Failed to execute search by relevance", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to execute search by relevance", e);
        }
    }

    /**
     * 일반 검색 (필드 정렬)
     */
    private PageResponse<PostSearchResponse> executeSearch(Query query, int page, int size,
                                                            String sortField, SortOrder sortOrder) {
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

            return executeSearchRequest(searchRequest, page, size);

        } catch (ElasticsearchException e) {
            if (isIndexNotFoundOrShardFailed(e)) {
                return new PageResponse<>(List.of(), 0, page, size);
            }
            throw new SearchOperationException("Failed to execute search", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to execute search", e);
        }
    }

    /**
     * SearchRequest 실행 (content 필드 제외)
     */
    private PageResponse<PostSearchResponse> executeSearchRequest(SearchRequest searchRequest,
                                                                   int page, int size) throws IOException {
        SearchRequest modifiedRequest = SearchRequest.of(s -> s
                .index(searchRequest.index())
                .query(searchRequest.query())
                .from(searchRequest.from())
                .size(searchRequest.size())
                .sort(searchRequest.sort())
                .minScore(searchRequest.minScore())
                .source(src -> src.filter(f -> f.excludes("content")))
        );

        SearchResponse<PostDetailDocument> response = elasticsearchClient.search(
                modifiedRequest,
                PostDetailDocument.class
        );

        List<PostSearchResponse> results = response.hits().hits().stream()
                .map(Hit::source)
                .map(doc -> mapper.toProjection(doc, null, null, null))
                .map(PostSearchResponse::fromProjection)
                .collect(Collectors.toList());

        long totalElements = response.hits().total() != null ? response.hits().total().value() : 0;
        return new PageResponse<>(results, totalElements, page, size);
    }

    // ==================== Utility Methods ====================

    /**
     * 인덱스 없음 예외 체크
     */
    private boolean isIndexNotFound(ElasticsearchException e) {
        return e.getMessage() != null && e.getMessage().contains("index_not_found_exception");
    }

    /**
     * 인덱스 없음 또는 샤드 실패 예외 체크
     */
    private boolean isIndexNotFoundOrShardFailed(ElasticsearchException e) {
        return e.getMessage() != null && (e.getMessage().contains("index_not_found_exception")
                || e.getMessage().contains("all shards failed"));
    }
}
