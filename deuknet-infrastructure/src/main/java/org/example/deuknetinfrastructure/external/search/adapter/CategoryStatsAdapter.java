package org.example.deuknetinfrastructure.external.search.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.port.in.category.CategoryRankingResponse;
import org.example.deuknetapplication.port.out.external.search.CategoryStatsPort;
import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.domain.category.Category;
import org.example.deuknetinfrastructure.external.search.document.PostDetailDocument;
import org.example.deuknetinfrastructure.external.search.exception.SearchOperationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 카테고리 통계 Adapter (Elasticsearch Aggregation)
 */
@Component
@RequiredArgsConstructor
public class CategoryStatsAdapter implements CategoryStatsPort {

    private static final String INDEX_NAME = "posts-detail";
    private final ElasticsearchClient elasticsearchClient;
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryRankingResponse> getCategoryRanking(int size) {
        try {
            // Elasticsearch Aggregation 쿼리 생성
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .size(0)  // 문서 자체는 필요 없고 aggregation 결과만 필요
                    .aggregations("category_stats", Aggregation.of(a -> a
                            .terms(t -> t
                                    .field("categoryId")
                                    .size(1000)  // 모든 카테고리 집계
                            )
                            .aggregations("total_views", Aggregation.of(sum -> sum
                                    .sum(sumAgg -> sumAgg.field("viewCount"))
                            ))
                            .aggregations("total_likes", Aggregation.of(sum -> sum
                                    .sum(sumAgg -> sumAgg.field("likeCount"))
                            ))
                    ))
            );

            SearchResponse<PostDetailDocument> response = elasticsearchClient.search(
                    searchRequest,
                    PostDetailDocument.class
            );

            // Aggregation 결과 파싱
            List<CategoryRankingResponse> rankings = new ArrayList<>();
            var categoryStatsAgg = response.aggregations().get("category_stats");

            if (categoryStatsAgg != null && categoryStatsAgg.isSterms()) {
                for (StringTermsBucket bucket : categoryStatsAgg.sterms().buckets().array()) {
                    String categoryIdStr = bucket.key().stringValue();
                    UUID categoryId = UUID.fromString(categoryIdStr);

                    long postCount = bucket.docCount();
                    long totalViewCount = (long) bucket.aggregations().get("total_views").sum().value();
                    long totalLikeCount = (long) bucket.aggregations().get("total_likes").sum().value();

                    // 가중치 계산: 게시물 수 * 1.5 + 조회수 * 1 + 추천수 * 2
                    double rankingScore = (postCount * 1.5) + (totalViewCount * 1.0) + (totalLikeCount * 2.0);

                    // 카테고리 이름 조회
                    String categoryName = categoryRepository.findById(categoryId)
                            .map(Category::getName)
                            .map(name -> name.getValue())
                            .orElse("Unknown");

                    rankings.add(CategoryRankingResponse.builder()
                            .categoryId(categoryId)
                            .categoryName(categoryName)
                            .postCount(postCount)
                            .totalViewCount(totalViewCount)
                            .totalLikeCount(totalLikeCount)
                            .rankingScore(rankingScore)
                            .build());
                }
            }

            // 랭킹 점수 순으로 정렬하고 size만큼 반환
            return rankings.stream()
                    .sorted(Comparator.comparingDouble(CategoryRankingResponse::getRankingScore).reversed())
                    .limit(size)
                    .toList();

        } catch (IOException e) {
            throw new SearchOperationException("Failed to get category ranking", e);
        }
    }
}
