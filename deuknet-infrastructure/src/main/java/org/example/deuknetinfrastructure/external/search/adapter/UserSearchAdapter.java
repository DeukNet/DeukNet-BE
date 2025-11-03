package org.example.deuknetinfrastructure.external.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.example.deuknetinfrastructure.external.search.document.UserDocument;
import org.example.deuknetinfrastructure.external.search.exception.SearchOperationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User 검색 Adapter
 *
 * UserDocument를 활용한 사용자 검색 기능을 제공합니다.
 * - username 검색 (정확 일치)
 * - displayName, bio 전문 검색
 * - 활동 통계 기반 정렬 (게시글 수, 댓글 수, 팔로워 수)
 */
@Component
@RequiredArgsConstructor
public class UserSearchAdapter {

    private static final String INDEX_NAME = "users";
    private final ElasticsearchClient elasticsearchClient;

    /**
     * ID로 사용자 조회
     */
    public Optional<UserDocument> findById(UUID id) {
        try {
            var response = elasticsearchClient.get(g -> g
                    .index(INDEX_NAME)
                    .id(id.toString()),
                UserDocument.class
            );

            if (response.found()) {
                return Optional.ofNullable(response.source());
            }
            return Optional.empty();
        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            // 인덱스가 없는 경우 빈 결과 반환
            if (e.getMessage() != null && e.getMessage().contains("index_not_found_exception")) {
                return Optional.empty();
            }
            throw new SearchOperationException("Failed to find user by id: " + id, e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to find user by id: " + id, e);
        }
    }

    /**
     * username으로 사용자 조회 (정확 일치)
     */
    public Optional<UserDocument> findByUsername(String username) {
        Query termQuery = Query.of(q -> q
            .term(t -> t
                .field("username")
                .value(username)
            )
        );

        List<UserDocument> results = executeSearch(termQuery, 0, 1, "createdAt", SortOrder.Desc);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * displayName으로 사용자 검색 (부분 일치)
     *
     * @param displayName 표시 이름
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과 리스트
     */
    public List<UserDocument> searchByDisplayName(String displayName, int page, int size) {
        Query matchQuery = Query.of(q -> q
            .match(m -> m
                .field("displayName")
                .query(displayName)
            )
        );

        return executeSearch(matchQuery, page, size, "followerCount", SortOrder.Desc);
    }

    /**
     * 키워드로 사용자 검색 (displayName + bio)
     *
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과 리스트
     */
    public List<UserDocument> searchByKeyword(String keyword, int page, int size) {
        Query multiMatchQuery = Query.of(q -> q
            .multiMatch(m -> m
                .query(keyword)
                .fields("displayName^2", "bio")  // displayName에 2배 가중치
            )
        );

        return executeSearch(multiMatchQuery, page, size, "followerCount", SortOrder.Desc);
    }

    /**
     * 활동적인 사용자 조회 (게시글 수 기준)
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 사용자 리스트
     */
    public List<UserDocument> findActiveUsersByPostCount(int page, int size) {
        Query matchAllQuery = Query.of(q -> q.matchAll(m -> m));

        return executeSearch(matchAllQuery, page, size, "postCount", SortOrder.Desc);
    }

    /**
     * 인기 사용자 조회 (팔로워 수 기준)
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 사용자 리스트
     */
    public List<UserDocument> findPopularUsers(int page, int size) {
        Query matchAllQuery = Query.of(q -> q.matchAll(m -> m));

        return executeSearch(matchAllQuery, page, size, "followerCount", SortOrder.Desc);
    }

    /**
     * 최근 가입 사용자 조회
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 사용자 리스트
     */
    public List<UserDocument> findRecentUsers(int page, int size) {
        Query matchAllQuery = Query.of(q -> q.matchAll(m -> m));

        return executeSearch(matchAllQuery, page, size, "createdAt", SortOrder.Desc);
    }

    /**
     * 복합 검색 (키워드 + 필터)
     *
     * @param keyword 검색 키워드 (null 가능)
     * @param minPostCount 최소 게시글 수 (null 가능)
     * @param minFollowerCount 최소 팔로워 수 (null 가능)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortField 정렬 필드
     * @param sortOrder 정렬 순서
     * @return 검색 결과 리스트
     */
    public List<UserDocument> searchWithFilters(
            String keyword,
            Long minPostCount,
            Long minFollowerCount,
            int page,
            int size,
            String sortField,
            SortOrder sortOrder
    ) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 키워드 검색
        if (keyword != null && !keyword.isBlank()) {
            boolQueryBuilder.must(Query.of(q -> q
                .multiMatch(m -> m
                    .query(keyword)
                    .fields("displayName^2", "bio", "username")
                )
            ));
        }

        // 최소 게시글 수 필터
        if (minPostCount != null && minPostCount > 0) {
            boolQueryBuilder.filter(Query.of(q -> q
                .range(r -> r
                    .field("postCount")
                    .gte(co.elastic.clients.json.JsonData.of(minPostCount))
                )
            ));
        }

        // 최소 팔로워 수 필터
        if (minFollowerCount != null && minFollowerCount > 0) {
            boolQueryBuilder.filter(Query.of(q -> q
                .range(r -> r
                    .field("followerCount")
                    .gte(co.elastic.clients.json.JsonData.of(minFollowerCount))
                )
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

        return executeSearch(boolQuery, page, size, sortField, sortOrder);
    }

    /**
     * 공통 검색 실행 메서드
     */
    private List<UserDocument> executeSearch(
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

            SearchResponse<UserDocument> response = elasticsearchClient.search(
                searchRequest,
                UserDocument.class
            );

            return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());

        } catch (IOException e) {
            throw new SearchOperationException("Failed to execute search", e);
        }
    }

    /**
     * 전체 개수 조회
     */
    public long count(Query query) {
        try {
            var response = elasticsearchClient.count(c -> c
                .index(INDEX_NAME)
                .query(query)
            );
            return response.count();
        } catch (IOException e) {
            throw new SearchOperationException("Failed to count documents", e);
        }
    }
}
