package org.example.deuknetapplication.port.out.external.search;

import org.example.deuknetapplication.port.in.category.CategoryRankingResponse;

import java.util.List;

/**
 * 카테고리 통계 조회 Port (Elasticsearch Aggregation)
 */
public interface CategoryStatsPort {

    /**
     * 카테고리별 게시물 통계를 집계하여 랭킹을 반환합니다.
     * 가중치: 게시물 수 * 1.5 + 조회수 * 1 + 추천수 * 2
     *
     * @param size 조회할 카테고리 수
     * @return 랭킹 점수 순으로 정렬된 카테고리 통계 목록
     */
    List<CategoryRankingResponse> getCategoryRanking(int size);
}
