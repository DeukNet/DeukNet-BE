package org.example.deuknetpresentation.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * DeukNet 전체 API 부하 테스트
 * 시나리오:
 * 1. 게시글 목록 조회 (최근글/인기글)
 * 2. 게시글 상세 조회 및 댓글 조회
 * 3. 카테고리 목록 및 카테고리별 게시글 조회
 * 4. 사용자 프로필 조회
 * 5. 게시글 검색 (키워드)
 * 6. 자동완성 (ngram - 실시간 타이핑)
 * 7. 혼합 시나리오 (실제 사용자 행동 패턴)
 *
 * 총 동시 사용자: 1,087명 (3초에 걸쳐 램프업)
 * 검증:
 * - 최대 응답 시간: 5초 이하
 * - 성공률: 95% 이상
 */
public class FullApiLoadTest extends Simulation {

    private static final String BASE_URL = "http://localhost:8080";

    // HTTP 프로토콜 설정
    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Load Test");

    private final ScenarioBuilder browsePostsScenario = scenario("Browse Posts")
        .exec(
            http("Get Posts - Recent")
                .get("/api/posts")
                .queryParam("status", "PUBLISHED")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .queryParam("sortBy", "createdAt")
                .queryParam("sortOrder", "desc")
                .check(status().is(200))
        )
        .pause(Duration.ofSeconds(2))
        .exec(
            http("Get Posts - Popular")
                .get("/api/posts/popular")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .check(status().is(200))
        );

    // 시나리오 2: 게시글 상세 조회
    private final ScenarioBuilder readPostScenario = scenario("Read Post Detail")
        .exec(
            http("Get Post List")
                .get("/api/posts")
                .queryParam("status", "PUBLISHED")
                .queryParam("page", "0")
                .queryParam("size", "5")
                .check(status().is(200))
                .check(jsonPath("$.content[0].id").optional().saveAs("postId"))
        )
        .pause(Duration.ofSeconds(1))
        .doIf(session -> session.contains("postId")).then(
            exec(
                http("Get Post Detail")
                    .get("/api/posts/#{postId}")
                    .check(status().is(200))
            )
            .pause(Duration.ofSeconds(2))
            .exec(
                http("Get Post Comments")
                    .get("/api/posts/#{postId}/comments")
                    .check(status().is(200))
            )
        );

    // 시나리오 3: 카테고리 조회
    private final ScenarioBuilder browseCategoriesScenario = scenario("Browse Categories")
        .exec(
            http("Get All Categories")
                .get("/api/categories")
                .check(status().is(200))
                .check(jsonPath("$[0].id").optional().saveAs("categoryId"))
        )
        .pause(Duration.ofSeconds(1))
        .doIf(session -> session.contains("categoryId")).then(
            exec(
                http("Get Posts by Category")
                    .get("/api/posts")
                    .queryParam("status", "PUBLISHED")
                    .queryParam("categoryId", "#{categoryId}")
                    .queryParam("page", "0")
                    .queryParam("size", "20")
                    .check(status().is(200))
            )
        );

    // 시나리오 4: 사용자 프로필 조회
    private final ScenarioBuilder viewUserProfileScenario = scenario("View User Profile")
        .exec(
            http("Get Post List")
                .get("/api/posts")
                .queryParam("status", "PUBLISHED")
                .queryParam("page", "0")
                .queryParam("size", "5")
                .check(status().is(200))
                .check(jsonPath("$.content[0].authorId").optional().saveAs("authorId"))
        )
        .pause(Duration.ofSeconds(1))
        .doIf(session -> session.contains("authorId")).then(
            exec(
                http("Get User Profile")
                    .get("/api/users/#{authorId}")
                    .check(status().is(200))
            )
        );

    // 시나리오 5: 검색 기능
    private final ScenarioBuilder searchScenario = scenario("Search Posts")
        .exec(
            http("Search Posts")
                .get("/api/posts")
                .queryParam("keyword", "테스트")
                .queryParam("status", "PUBLISHED")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .check(status().is(200))
        );

    // 시나리오 6: 자동완성 (ngram)
    private final ScenarioBuilder autocompleteScenario = scenario("Autocomplete Suggestions")
        .exec(
            http("Suggest Keywords - Single Char")
                .get("/api/posts/suggest")
                .queryParam("q", "테")
                .queryParam("size", "10")
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(300), Duration.ofMillis(500))
        .exec(
            http("Suggest Keywords - Two Chars")
                .get("/api/posts/suggest")
                .queryParam("q", "테스")
                .queryParam("size", "10")
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(300), Duration.ofMillis(500))
        .exec(
            http("Suggest Keywords - Full Word")
                .get("/api/posts/suggest")
                .queryParam("q", "테스트")
                .queryParam("size", "10")
                .check(status().is(200))
        );

    // 시나리오 7: 혼합 시나리오 (실제 사용자 행동 패턴)
    private final ScenarioBuilder mixedScenario = scenario("Mixed User Behavior")
        .exec(
            http("Visit Homepage - Get Posts")
                .get("/api/posts")
                .queryParam("status", "PUBLISHED")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .check(status().is(200))
                .check(jsonPath("$.content[0].id").optional().saveAs("firstPostId"))
                .check(jsonPath("$.content[1].id").optional().saveAs("secondPostId"))
        )
        .pause(Duration.ofSeconds(2, 5))
        .doIf(session -> session.contains("firstPostId")).then(
            exec(
                http("Read First Post")
                    .get("/api/posts/#{firstPostId}")
                    .check(status().is(200))
                    .check(jsonPath("$.authorId").optional().saveAs("authorId"))
            )
            .pause(Duration.ofSeconds(3, 8))
            .exec(
                http("Get Comments")
                    .get("/api/posts/#{firstPostId}/comments")
                    .check(status().is(200))
            )
        )
        .pause(Duration.ofSeconds(2, 4))
        .exec(
            http("Get Categories")
                .get("/api/categories")
                .check(status().is(200))
        )
        .pause(Duration.ofSeconds(1, 3))
        // 자동완성 시뮬레이션 (검색창에 타이핑)
        .exec(
            http("Autocomplete While Typing")
                .get("/api/posts/suggest")
                .queryParam("q", "프")
                .queryParam("size", "5")
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(200), Duration.ofMillis(400))
        .exec(
            http("Autocomplete More Chars")
                .get("/api/posts/suggest")
                .queryParam("q", "프로그")
                .queryParam("size", "5")
                .check(status().is(200))
        )
        .pause(Duration.ofSeconds(1, 2))
        .doIf(session -> session.contains("authorId")).then(
            exec(
                http("View Author Profile")
                    .get("/api/users/#{authorId}")
                    .check(status().is(200))
            )
        );

    {
        setUp(
            // 각 시나리오에 사용자 분배
            browsePostsScenario.injectOpen(
                rampUsers(100).during(Duration.ofSeconds(10))
            ),
            readPostScenario.injectOpen(
                rampUsers(100).during(Duration.ofSeconds(10))
            ),
            browseCategoriesScenario.injectOpen(
                rampUsers(500).during(Duration.ofSeconds(10))
            ),
            viewUserProfileScenario.injectOpen(
                rampUsers(500).during(Duration.ofSeconds(10))
            ),
            searchScenario.injectOpen(
                rampUsers(500).during(Duration.ofSeconds(10))
            ),
            autocompleteScenario.injectOpen(
                rampUsers(500).during(Duration.ofSeconds(10))
            ),
            mixedScenario.injectOpen(
                rampUsers(750).during(Duration.ofSeconds(10))
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().max().lt(5000),  // 최대 응답 시간 5초 이하
             global().successfulRequests().percent().gt(95.0)  // 성공률 95% 이상
         );
    }
}
