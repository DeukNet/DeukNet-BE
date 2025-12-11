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
    public PageResponse<PostSearchResponse> search(PostSearchRequest request) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 검색어 필터링

        applyKeywordFilter(boolQueryBuilder, request.getKeyword());

        // 작성자 필터
        if (request.getAuthorId() != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("authorId").value(request.getAuthorId().toString()))
            ));
        }

        // 카테고리 필터
        if (request.getCategoryId() != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("categoryIds").value(request.getCategoryId().toString()))
            ));
        }

        // 상태 필터
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("status").value(request.getStatus()))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

        // 검색어가 있을 때는 관련성 점수로 정렬, 없으면 지정된 필드로 정렬
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            // 관련성 점수(_score) 기준 정렬 (내림차순)
            return executeSearchByRelevance(boolQuery, request.getPage(), request.getSize());
        } else {
            // 일반 필드 기준 정렬
            SortOrder sortOrder = "asc".equalsIgnoreCase(request.getSortOrder())
                    ? SortOrder.Asc
                    : SortOrder.Desc;
            return executeSearch(boolQuery, request.getPage(), request.getSize(), request.getSortBy(), sortOrder);
        }
    }

    @Override
    public PageResponse<PostSearchResponse> findPopularPosts(int page, int size, UUID categoryId, String keyword) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // PUBLISHED 상태 필터링 (필수)
        boolQueryBuilder.filter(Query.of(q -> q
            .term(t -> t.field("status").value("PUBLISHED"))
        ));

        // 카테고리 필터링
        if (categoryId != null) {
            boolQueryBuilder.filter(Query.of(q -> q
                .term(t -> t.field("categoryIds").value(categoryId.toString()))
            ));
        }

        // 검색어 필터링
        applyKeywordFilter(boolQueryBuilder, keyword);

        Query query = Query.of(q -> q.bool(boolQueryBuilder.build()));

        return executePopularSearch(query, page, size);
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
    public PageResponse<PostSearchResponse> findFeaturedPosts(UUID categoryId, int page, int size) {
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
        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();

            List<String> keywords = Arrays.stream(trimmedKeyword.split("\\s+"))
                    .filter(k -> !k.isBlank())
                    .toList();

            boolQueryBuilder.must(b -> b.bool(inner -> {
                // multi_match: title(가중치 2.0) + content(가중치 0.8)
                keywords.forEach(kw -> inner.should(s ->
                        s.multiMatch(m -> m
                                .query(kw)
                                .fields("title^2.0", "content^0.8")
                                .operator(Operator.Or)
                        )
                ));

                return inner.minimumShouldMatch("1");
            }));
        }
    }
}
