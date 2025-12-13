package org.example.deuknetapplication.service.category;

import org.example.deuknetapplication.port.in.category.CategoryRankingResponse;
import org.example.deuknetapplication.port.in.category.GetCategoryRankingUseCase;
import org.example.deuknetapplication.port.out.external.search.CategoryStatsPort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 카테고리 랭킹 조회 서비스
 * SRP: CategoryStatsPort로 위임하여 통계 조회
 */
@Service
public class GetCategoryRankingService implements GetCategoryRankingUseCase {

    private final CategoryStatsPort categoryStatsPort;

    public GetCategoryRankingService(CategoryStatsPort categoryStatsPort) {
        this.categoryStatsPort = categoryStatsPort;
    }

    @Override
    public List<CategoryRankingResponse> getCategoryRanking(int size) {
        // 최대 100개로 제한
        if (size > 100) {
            size = 100;
        }

        return categoryStatsPort.getCategoryRanking(size);
    }
}
