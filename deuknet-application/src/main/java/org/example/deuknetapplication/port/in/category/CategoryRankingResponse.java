package org.example.deuknetapplication.port.in.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 카테고리 랭킹 응답 DTO
 * 가중치: 게시물 수 * 1.5 + 조회수 * 1 + 추천수 * 2
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRankingResponse {
    private UUID categoryId;
    private String categoryName;
    private long postCount;
    private long totalViewCount;
    private long totalLikeCount;
    private double rankingScore;  // (postCount * 1.5) + (viewCount * 1) + (likeCount * 2)

    /**
     * 통계 데이터로부터 Response 생성
     */
    public static CategoryRankingResponse of(
            UUID categoryId,
            String categoryName,
            long postCount,
            long totalViewCount,
            long totalLikeCount,
            double rankingScore
    ) {
        return new CategoryRankingResponse(
                categoryId,
                categoryName,
                postCount,
                totalViewCount,
                totalLikeCount,
                rankingScore
        );
    }
}
