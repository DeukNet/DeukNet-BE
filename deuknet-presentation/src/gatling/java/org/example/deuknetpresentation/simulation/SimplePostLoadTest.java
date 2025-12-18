package org.example.deuknetpresentation.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Post API 단순 부하 테스트
 * 목표: 초당 600 요청 (600 RPS) - 1초 단위로 집중 투하
 *
 * PostController의 검색 관련 GET 엔드포인트만 사용:
 * 1. GET /api/posts - 게시글 검색 (기본)
 * 2. GET /api/posts?keyword=테스트 - 키워드 검색
 * 3. GET /api/posts?sortType=POPULAR - 인기순 검색
 * 4. GET /api/posts/trending - 트렌딩 게시글
 * 5. GET /api/posts/featured - 추천 게시글
 * 6. GET /api/posts/suggest - 키워드 자동완성
 *
 * 부하 패턴: constantUsersPerSec(600) - 초당 정확히 600 요청 유지
 * 테스트 시간: 60초
 * 검증: 200 OK 응답만 성공
 */
public class SimplePostLoadTest extends Simulation {

    private static final String BASE_URL = "http://localhost:8080";

    // HTTP 프로토콜 설정
    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Simple Post Load Test");

    // 1. GET /api/posts - 기본 검색
    private final ScenarioBuilder searchPostsScenario = scenario("GET /api/posts")
        .exec(
            http("GET /api/posts")
                .get("/api/posts")
                .queryParam("sortType", "RECENT")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .check(status().is(200))
        );

    // 2. GET /api/posts?keyword=테스트 - 키워드 검색
    private final ScenarioBuilder searchKeywordScenario = scenario("GET /api/posts?keyword")
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

    // 4. GET /api/posts/trending - 트렌딩
    private final ScenarioBuilder trendingScenario = scenario("GET /api/posts/trending")
        .exec(
            http("GET /api/posts/trending")
                .get("/api/posts/trending")
                .check(status().is(200))
        );

    // 5. GET /api/posts/featured - 추천
    private final ScenarioBuilder featuredScenario = scenario("GET /api/posts/featured")
        .exec(
            http("GET /api/posts/featured")
                .get("/api/posts/featured")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .check(status().is(200))
        );

    // 6. GET /api/posts/suggest - 자동완성
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
            // 총 600 RPS를 검색 엔드포인트에 균등 분배
            searchPostsScenario.injectOpen(
                constantUsersPerSec(200).during(Duration.ofSeconds(60))  // 200 RPS
            ),
            searchKeywordScenario.injectOpen(
                constantUsersPerSec(150).during(Duration.ofSeconds(60))  // 150 RPS
            ),
            searchPopularScenario.injectOpen(
                constantUsersPerSec(150).during(Duration.ofSeconds(60))  // 150 RPS
            ),
            trendingScenario.injectOpen(
                constantUsersPerSec(40).during(Duration.ofSeconds(60))   // 40 RPS
            ),
            featuredScenario.injectOpen(
                constantUsersPerSec(40).during(Duration.ofSeconds(60))   // 40 RPS
            ),
            suggestScenario.injectOpen(
                constantUsersPerSec(20).during(Duration.ofSeconds(60))   // 20 RPS
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().max().lt(5000),   // 최대 응답 시간 5초 이하
             global().successfulRequests().percent().gt(95.0)  // 성공률 95% 이상
         );
    }
}
