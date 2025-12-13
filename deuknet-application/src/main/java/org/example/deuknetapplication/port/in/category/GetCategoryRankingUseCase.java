package org.example.deuknetapplication.port.in.category;

import java.util.List;

/**
 * 카테고리 랭킹 조회 유스케이스
 */
public interface GetCategoryRankingUseCase {
    /**
     * 카테고리 랭킹을 조회합니다.
     * 가중치: 게시물 수 * 1.5 + 조회수 * 1 + 추천수 * 2
     *
     * @param size 조회할 카테고리 수 (기본값: 10)
     * @return 랭킹 순으로 정렬된 카테고리 목록
     */
    List<CategoryRankingResponse> getCategoryRanking(int size);
}
