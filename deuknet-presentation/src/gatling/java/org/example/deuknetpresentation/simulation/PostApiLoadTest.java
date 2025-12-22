package org.example.deuknetpresentation.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Post API 검색 위주 부하 테스트
 * 목표: 초당 600 요청 (600 RPS)
 *
 * PostController의 검색 관련 GET 엔드포인트:
 * 1. GET /api/posts - 게시글 검색 (keyword, categoryId, authorId, sortType)
 * 2. GET /api/posts/{id} - 게시글 상세 조회
 * 3. GET /api/posts/trending - 트렌딩 게시글
 * 4. GET /api/posts/featured - 추천 게시글
 * 5. GET /api/posts/suggest - 키워드 자동완성
 *
 * 부하 패턴: constantUsersPerSec(600) - 초당 600 요청 유지
 * 테스트 시간: 60초
 * 검증: 200 OK 응답만 성공
 */
public class PostApiLoadTest extends Simulation {

    private static final String BASE_URL = "http://localhost:8080";

    // HTTP 프로토콜 설정
    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Post API Load Test");

    // 1. GET /api/posts - 게시글 검색 (기본)
    private final ScenarioBuilder searchPostsScenario = scenario("GET /api/posts - Search")
        .exec(
            http("GET /api/posts")
                .get("/api/posts")
                .queryParam("sortType", "RECENT")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .check(status().is(200))
        );

    // 2. GET /api/posts?keyword=xxx - 키워드 검색
    private final ScenarioBuilder searchWithKeywordScenario = scenario("GET /api/posts?keyword")
        .exec(
            http("GET /api/posts?keyword=테스트")
                .get("/api/posts")
                .queryParam("keyword", "테스트")
                .queryParam("sortType", "RECENT")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .check(status().is(200))
        );

    // 3. GET /api/posts?sortType=POPULAR - 인기순 검색
    private final ScenarioBuilder searchPopularScenario = scenario("GET /api/posts?sortType=POPULAR")
        .exec(
            http("GET /api/posts?sortType=POPULAR")
                .get("/api/posts")
                .queryParam("sortType", "POPULAR")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .check(status().is(200))
        );

    // 4. GET /api/posts/{id} - 게시글 상세 조회
    private final ScenarioBuilder getPostDetailScenario = scenario("GET /api/posts/{id}")
        .exec(
            http("Get Post List for ID")
                .get("/api/posts")
                .queryParam("sortType", "RECENT")
                .queryParam("page", "0")
                .queryParam("size", "1")
                .check(status().is(200))
                .check(jsonPath("$.content[0].id").optional().saveAs("postId"))
        )
        .doIf(session -> session.contains("postId")).then(
            exec(
                http("GET /api/posts/{id}")
                    .get("/api/posts/#{postId}")
                    .check(status().is(200))
            )
        );

    // 5. GET /api/posts/trending - 트렌딩 게시글
    private final ScenarioBuilder getTrendingScenario = scenario("GET /api/posts/trending")
        .exec(
            http("GET /api/posts/trending")
                .get("/api/posts/trending")
                .check(status().is(200))
        );

    // 6. GET /api/posts/featured - 추천 게시글
    private final ScenarioBuilder getFeaturedScenario = scenario("GET /api/posts/featured")
        .exec(
            http("GET /api/posts/featured")
                .get("/api/posts/featured")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .check(status().is(200))
        );

    // 7. GET /api/posts/suggest - 키워드 자동완성
    private final ScenarioBuilder suggestScenario = scenario("GET /api/posts/suggest")
        .exec(
            http("GET /api/posts/suggest?q=테")
                .get("/api/posts/suggest")
                .queryParam("q", "테")
                .queryParam("size", "10")
                .check(status().is(200))
        );

    {
        setUp(
            // 총 600 RPS를 검색 관련 엔드포인트에 분배
            searchPostsScenario.injectOpen(
                constantUsersPerSec(200).during(Duration.ofSeconds(60))
            ),
            searchWithKeywordScenario.injectOpen(
                constantUsersPerSec(100).during(Duration.ofSeconds(60))
            ),
            searchPopularScenario.injectOpen(
                constantUsersPerSec(100).during(Duration.ofSeconds(60))
            ),
            getPostDetailScenario.injectOpen(
                constantUsersPerSec(100).during(Duration.ofSeconds(60))
            ),
            getTrendingScenario.injectOpen(
                constantUsersPerSec(100).during(Duration.ofSeconds(60))
            ),
            getFeaturedScenario.injectOpen(
                constantUsersPerSec(100).during(Duration.ofSeconds(60))
            ),
            suggestScenario.injectOpen(
                constantUsersPerSec(200).during(Duration.ofSeconds(60))
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().max().lt(5000),   // 최대 응답 시간 5초 이하
             global().responseTime().mean().lt(1000),  // 평균 응답 시간 1초 이하
             global().successfulRequests().percent().gt(95.0)  // 성공률 95% 이상
         );
    }
}
