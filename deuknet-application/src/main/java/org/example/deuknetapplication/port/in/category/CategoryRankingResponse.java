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
}
