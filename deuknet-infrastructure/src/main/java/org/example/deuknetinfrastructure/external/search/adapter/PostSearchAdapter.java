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
 * <br>
 * PostSearchPort 구현 (out port)
 * - SearchPostService, GetPostByIdService가 사용
 * todo 리펙토링 합시다.
 */
@Component
@RequiredArgsConstructor
public class PostSearchAdapter implements PostSearchPort {

    private static final String INDEX_NAME = "posts-detail";
    private final ElasticsearchClient elasticsearchClient;
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
                        .map(doc -> mapper.toProjection(doc, null, null, null))
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
    @Deprecated
    public PageResponse<PostSearchResponse> search(PostSearchRequest request) {
        // Deprecated: Service 레이어에서 sortType별 메서드를 직접 호출
        throw new UnsupportedOperationException("Use sortType-specific methods instead");
    }

    @Override
    public PageResponse<PostSearchResponse> searchByPopular(String keyword, UUID authorId, UUID categoryId, int page, int size, boolean includeAnonymous) {
        Query boolQuery = buildBoolQuery(keyword, authorId, categoryId, includeAnonymous);
        return executePopularSearch(boolQuery, page, size);
    }

    @Override
    public PageResponse<PostSearchResponse> searchByRelevance(String keyword, UUID authorId, UUID categoryId, int page, int size, boolean includeAnonymous) {
        Query boolQuery = buildBoolQueryForRelevance(keyword, authorId, categoryId, includeAnonymous);
        return executeSearchByRelevance(boolQuery, page, size);
    }

    @Override
    public PageResponse<PostSearchResponse> searchByRecent(String keyword, UUID authorId, UUID categoryId, int page, int size, boolean includeAnonymous) {
        Query boolQuery = buildBoolQuery(keyword, authorId, categoryId, includeAnonymous);
        return executeSearch(boolQuery, page, size, "createdAt", SortOrder.Desc);
    }

    @Override
    public List<String> suggestKeywords(String prefix, int size) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }

        try {
            // 제목의 autocomplete 필드만 사용한 prefix 검색
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

            // 제목에서 고유한 키워드 추출
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
            if (e.getMessage() != null && (e.getMessage().contains("index_not_found_exception")
                    || e.getMessage().contains("all shards failed"))) {
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

        // PUBLISHED 상태 필터링 (필수)
        boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("status").value("PUBLISHED"))
        ));

        // 카테고리 필터링
        if (categoryId != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                    .term(t -> t.field("categoryId").value(categoryId.toString()))
            ));
        }

        // 익명 게시물 필터링
        if (!includeAnonymous) {
            boolQueryBuilder.filter(Query.of(q -> q
                    .term(t -> t.field("authorType").value("REAL"))
            ));
        }

        Query query = Query.of(q -> q.bool(boolQueryBuilder.build()));

        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .query(query)
                    .from(page * size)
                    .size(Math.min(size, 20))  // 최대 20개
                    .sort(sort -> sort
                            .field(f -> f
                                    .field("likeCount")
                                    .order(SortOrder.Desc)
                            )
                    )
            );

            return getPostSearchResponsePageResponse(page, size, searchRequest);

        } catch (ElasticsearchException e) {
            if (e.getMessage() != null && (e.getMessage().contains("index_not_found_exception")
                    || e.getMessage().contains("all shards failed"))) {
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
            // 24시간 이내 게시글만 대상
            long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q
                            .bool(b -> b
                                    .filter(f -> f.term(t -> t.field("status").value("PUBLISHED")))
                                    .filter(f -> f.range(r -> r
                                            .field("createdAt")
                                            .gte(co.elastic.clients.json.JsonData.of(oneDayAgo))
                                    ))
                            )
                    )
                    .size(size)
                    .sort(sort -> sort
                            .script(script -> script
                                    .type(co.elastic.clients.elasticsearch._types.ScriptSortType.Number)
                                    .script(sc -> sc
                                            .inline(inline -> inline
                                                    .source("""
                                    long now = new Date().getTime();
                                    long created = doc['createdAt'].value.toInstant().toEpochMilli();
                                    double hoursOld = (now - created) / (1000.0 * 60 * 60);

                                    // age_decay = 1 + (hours_old / 24)^2
                                    double ageDecay = 1.0 + Math.pow(hoursOld / 24.0, 2);

                                    // score = (viewCount * 0.3 + likeCount * 0.7) / age_decay
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
                    .map(PostSearchResponse::new)
                    .collect(Collectors.toList());

        } catch (ElasticsearchException e) {
            if (e.getMessage() != null && (e.getMessage().contains("index_not_found_exception")
                    || e.getMessage().contains("all shards failed"))) {
                return List.of();
            }
            throw new SearchOperationException("Failed to find trending posts", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to find trending posts", e);
        }
    }

    /**
     * BoolQuery 생성 공통 메서드
     */
    private Query buildBoolQuery(String keyword, UUID authorId, UUID categoryId, boolean includeAnonymous) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // PUBLIC 상태 필터링 (고정)
        boolQueryBuilder.filter(Query.of(q -> q
            .term(t -> t.field("status").value(PostStatus.PUBLIC.name()))
        ));

        // 검색어 필터링
        applyKeywordFilter(boolQueryBuilder, keyword);

        // 작성자 필터
        if (authorId != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("authorId").value(authorId.toString()))
            ));
        }

        // 카테고리 필터
        if (categoryId != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("categoryId").value(categoryId.toString()))
            ));
        }

        // 익명 게시물 필터링
        if (!includeAnonymous) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("authorType").value("REAL"))
            ));
        }

        return Query.of(q -> q.bool(boolQueryBuilder.build()));
    }

    /**
     * 관련성 검색용 BoolQuery 생성 (높은 가중치 적용)
     */
    private Query buildBoolQueryForRelevance(String keyword, UUID authorId, UUID categoryId, boolean includeAnonymous) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // PUBLIC 상태 필터링 (고정)
        boolQueryBuilder.filter(Query.of(q -> q
            .term(t -> t.field("status").value(PostStatus.PUBLIC.name()))
        ));

        // 검색어 필터링 (높은 가중치)
        applyKeywordFilterForRelevance(boolQueryBuilder, keyword);

        // 작성자 필터
        if (authorId != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("authorId").value(authorId.toString()))
            ));
        }

        // 카테고리 필터
        if (categoryId != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("categoryId").value(categoryId.toString()))
            ));
        }

        // 익명 게시물 필터링
        if (!includeAnonymous) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("authorType").value("REAL"))
            ));
        }

        return Query.of(q -> q.bool(boolQueryBuilder.build()));
    }


    /**
     * 인기 게시물 검색
     * 인기도 = (likeCount * 3 + viewCount * 1)
     * 시간 감쇠 = 30일 이상 경과 시 0.75배
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

            return getPostSearchResponsePageResponse(page, size, searchRequest);

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
     * 관련성 점수(_score) 기준 검색 실행 메서드
     * 스코어 1.5 이상만 반환 (검색 품질 향상)
     */
    private PageResponse<PostSearchResponse> executeSearchByRelevance(
            Query query,
            int page,
            int size
    ) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .query(query)
                .from(page * size)
                .size(size)
                .minScore(1.5)  // 스코어 1.5 이상만 반환
                // _score 기준 내림차순 정렬 (관련성이 높은 순)
            );

            return getPostSearchResponsePageResponse(page, size, searchRequest);

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
     * 공통 검색 실행 메서드 (필드 기준 정렬)
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

            return getPostSearchResponsePageResponse(page, size, searchRequest);

        } catch (ElasticsearchException e) {
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

    private PageResponse<PostSearchResponse> getPostSearchResponsePageResponse(int page, int size, SearchRequest searchRequest) throws IOException {
        SearchResponse<PostDetailDocument> response = elasticsearchClient.search(
            searchRequest,
            PostDetailDocument.class
        );

        List<PostSearchResponse> results = response.hits().hits().stream()
            .map(Hit::source)
            .map(doc -> mapper.toProjection(doc, null, null, null))
            .map(PostSearchResponse::new)
            .collect(Collectors.toList());

        long totalElements = response.hits().total() != null ? response.hits().total().value() : 0;
        return new PageResponse<>(results, totalElements, page, size);
    }

    @Override
    public PageResponse<PostSearchResponse> findFeaturedPosts(UUID categoryId, int page, int size, boolean includeAnonymous) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // PUBLIC 상태 필터링 (필수)
        boolQueryBuilder.filter(Query.of(q -> q
            .term(t -> t.field("status").value(PostStatus.PUBLIC.name()))
        ));

        // 카테고리 필터링
        if (categoryId != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("categoryId").value(categoryId.toString()))
            ));
        }

        // 익명 게시물 필터링
        if (!includeAnonymous) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("authorType").value("REAL"))
            ));
        }

        Query query = Query.of(q -> q.bool(boolQueryBuilder.build()));

        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .query(query)
                .from(page * size)
                .size(Math.min(size, 20))  // 최대 20개
                .sort(sort -> sort
                    .field(f -> f
                        .field("likeCount")
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
                .map(doc -> mapper.toProjection(doc, null, null, null))
                .map(PostSearchResponse::new)
                .collect(Collectors.toList());

            long totalElements = response.hits().total() != null ? response.hits().total().value() : 0;
            return new PageResponse<>(results, totalElements, page, size);

        } catch (ElasticsearchException e) {
            if (e.getMessage() != null && (e.getMessage().contains("index_not_found_exception")
                    || e.getMessage().contains("all shards failed"))) {
                return new PageResponse<>(List.of(), 0, page, size);
            }
            throw new SearchOperationException("Failed to find featured posts", e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to find featured posts", e);
        }
    }

    /**
     * 검색어 필터링을 BoolQuery에 적용하는 공통 메서드
     * Nori 형태소 분석 기반 검색 (fuzziness 제거, 스코어 품질 향상)
     * 제목 가중치 2.0, 내용 가중치 0.8
     */
    private void applyKeywordFilter(BoolQuery.Builder boolQueryBuilder, String keyword) {
        applyKeywordFilterWithBoost(boolQueryBuilder, keyword, 2.0, 0.8);
    }

    /**
     * 관련성 검색용 검색어 필터링 (세밀한 검색을 위한 균형잡힌 가중치)
     * minScore 1.5를 통과하면서도 내용 검색을 잘 반영하도록 조정
     * 제목 가중치 3.0, 내용 가중치 2.0 (비율 1.5:1)
     */
    private void applyKeywordFilterForRelevance(BoolQuery.Builder boolQueryBuilder, String keyword) {
        applyKeywordFilterWithBoost(boolQueryBuilder, keyword, 3.0, 2.0);
    }

    /**
     * 검색어 필터링을 BoolQuery에 적용하는 공통 메서드 (가중치 커스터마이징 가능)
     * Nori 형태소 분석 기반 검색
     *
     * @param boolQueryBuilder BoolQuery 빌더
     * @param keyword 검색 키워드
     * @param titleBoost 제목 필드 가중치
     * @param contentBoost 내용 필드 가중치
     */
    private void applyKeywordFilterWithBoost(
            BoolQuery.Builder boolQueryBuilder,
            String keyword,
            double titleBoost,
            double contentBoost
    ) {
        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();

            List<String> keywords = Arrays.stream(trimmedKeyword.split("\\s+"))
                    .filter(k -> !k.isBlank())
                    .toList();

            boolQueryBuilder.must(b -> b.bool(inner -> {
                // multi_match: title + content (커스터마이징 가능한 가중치)
                keywords.forEach(kw -> inner.should(s ->
                        s.multiMatch(m -> m
                                .query(kw)
                                .fields("title^" + titleBoost, "content^" + contentBoost)
                                .operator(Operator.Or)
                        )
                ));

                return inner.minimumShouldMatch("1");
            }));
        }
    }
}
