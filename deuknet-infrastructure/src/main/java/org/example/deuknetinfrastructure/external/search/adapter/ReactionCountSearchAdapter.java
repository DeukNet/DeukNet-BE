package org.example.deuknetinfrastructure.external.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.example.deuknetinfrastructure.external.search.document.ReactionCountDocument;
import org.example.deuknetinfrastructure.external.search.exception.SearchOperationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ReactionCount 검색 Adapter
 *
 * ReactionCountDocument를 활용한 리액션 집계 조회 기능을 제공합니다.
 * - targetId(Post/Comment)별 리액션 카운트 조회
 * - 인기 컨텐츠 조회 (리액션 수 기준)
 * - 리액션 통계 집계
 */
@Component
@RequiredArgsConstructor
public class ReactionCountSearchAdapter {

    private static final String INDEX_NAME = "reaction_counts";
    private final ElasticsearchClient elasticsearchClient;

    /**
     * targetId로 리액션 카운트 조회
     *
     * @param targetId 대상 ID (Post ID 또는 Comment ID)
     * @return 리액션 카운트 Document
     */
    public Optional<ReactionCountDocument> findByTargetId(UUID targetId) {
        try {
            var response = elasticsearchClient.get(g -> g
                    .index(INDEX_NAME)
                    .id(targetId.toString()),
                ReactionCountDocument.class
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
            throw new SearchOperationException("Failed to find reaction count by targetId: " + targetId, e);
        } catch (IOException e) {
            throw new SearchOperationException("Failed to find reaction count by targetId: " + targetId, e);
        }
    }

    /**
     * 여러 targetId의 리액션 카운트를 일괄 조회
     *
     * @param targetIds 대상 ID 리스트
     * @return 리액션 카운트 Document 리스트
     */
    public List<ReactionCountDocument> findByTargetIds(List<UUID> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return List.of();
        }

        Query idsQuery = Query.of(q -> q
            .ids(i -> i
                .values(targetIds.stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList())
                )
            )
        );

        return executeSearch(idsQuery, 0, targetIds.size(), "totalCount", SortOrder.Desc);
    }

    /**
     * 좋아요가 많은 컨텐츠 조회
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 리액션 카운트 Document 리스트 (likeCount 내림차순)
     */
    public List<ReactionCountDocument> findMostLiked(int page, int size) {
        Query matchAllQuery = Query.of(q -> q.matchAll(m -> m));

        return executeSearch(matchAllQuery, page, size, "likeCount", SortOrder.Desc);
    }

    /**
     * 전체 리액션이 많은 컨텐츠 조회
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 리액션 카운트 Document 리스트 (totalCount 내림차순)
     */
    public List<ReactionCountDocument> findMostReacted(int page, int size) {
        Query matchAllQuery = Query.of(q -> q.matchAll(m -> m));

        return executeSearch(matchAllQuery, page, size, "totalCount", SortOrder.Desc);
    }

    /**
     * 최소 좋아요 수 이상의 컨텐츠 조회
     *
     * @param minLikeCount 최소 좋아요 수
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 리액션 카운트 Document 리스트
     */
    public List<ReactionCountDocument> findByMinLikeCount(long minLikeCount, int page, int size) {
        Query rangeQuery = Query.of(q -> q
            .range(r -> r
                .field("likeCount")
                .gte(co.elastic.clients.json.JsonData.of(minLikeCount))
            )
        );

        return executeSearch(rangeQuery, page, size, "likeCount", SortOrder.Desc);
    }

    /**
     * 최소 전체 리액션 수 이상의 컨텐츠 조회
     *
     * @param minTotalCount 최소 전체 리액션 수
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 리액션 카운트 Document 리스트
     */
    public List<ReactionCountDocument> findByMinTotalCount(long minTotalCount, int page, int size) {
        Query rangeQuery = Query.of(q -> q
            .range(r -> r
                .field("totalCount")
                .gte(co.elastic.clients.json.JsonData.of(minTotalCount))
            )
        );

        return executeSearch(rangeQuery, page, size, "totalCount", SortOrder.Desc);
    }

    /**
     * 최근 업데이트된 리액션 카운트 조회
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 리액션 카운트 Document 리스트
     */
    public List<ReactionCountDocument> findRecentlyUpdated(int page, int size) {
        Query matchAllQuery = Query.of(q -> q.matchAll(m -> m));

        return executeSearch(matchAllQuery, page, size, "lastEventTimestamp", SortOrder.Desc);
    }

    /**
     * 공통 검색 실행 메서드
     */
    private List<ReactionCountDocument> executeSearch(
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

            SearchResponse<ReactionCountDocument> response = elasticsearchClient.search(
                searchRequest,
                ReactionCountDocument.class
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

    /**
     * 전체 리액션 카운트 집계
     *
     * @return 모든 컨텐츠의 전체 리액션 수 합계
     */
    public long getTotalReactionCount() {
        // 현재는 간단히 count 반환
        Query matchAllQuery = Query.of(q -> q.matchAll(m -> m));
        return count(matchAllQuery);
    }
}
