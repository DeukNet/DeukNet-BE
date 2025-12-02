package org.example.deuknetpresentation.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * DeukNet 전체 API 부하 테스트
 *
 * 시나리오:
 * 1. 게시글 목록 조회 (비인증)
 * 2. 게시글 상세 조회 (비인증)
 * 3. 카테고리 목록 조회 (비인증)
 * 4. 사용자 프로필 조회 (비인증)
 *
 * 동시 사용자: 150명
 * 기간: 60초
 */
public class FullApiLoadTest extends Simulation {

    // 테스트 대상 서버 (NodePort 사용)
    private static final String BASE_URL = "http://172.17.0.2:30080";

    // HTTP 프로토콜 설정
    private HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Load Test");

    // 시나리오 1: 게시글 목록 조회
    private ScenarioBuilder browsePostsScenario = scenario("Browse Posts")
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
    private ScenarioBuilder readPostScenario = scenario("Read Post Detail")
        .exec(
            http("Get Post List")
                .get("/api/posts")
                .queryParam("status", "PUBLISHED")
                .queryParam("page", "0")
                .queryParam("size", "5")
                .check(status().is(200))
                .check(jsonPath("$.content[0].id").saveAs("postId"))
        )
        .pause(Duration.ofSeconds(1))
        .exec(
            http("Get Post Detail")
                .get("/api/posts/#{postId}")
                .check(status().is(200))
        )
        .pause(Duration.ofSeconds(2))
        .exec(
            http("Get Post Comments")
                .get("/api/posts/#{postId}/comments")
                .check(status().is(200))
        );

    // 시나리오 3: 카테고리 조회
    private ScenarioBuilder browseCategoriesScenario = scenario("Browse Categories")
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
    private ScenarioBuilder viewUserProfileScenario = scenario("View User Profile")
        .exec(
            http("Get Post List")
                .get("/api/posts")
                .queryParam("status", "PUBLISHED")
                .queryParam("page", "0")
                .queryParam("size", "5")
                .check(status().is(200))
                .check(jsonPath("$.content[0].authorId").saveAs("authorId"))
        )
        .pause(Duration.ofSeconds(1))
        .exec(
            http("Get User Profile")
                .get("/api/users/#{authorId}")
                .check(status().is(200))
        );

    // 시나리오 5: 검색 기능
    private ScenarioBuilder searchScenario = scenario("Search Posts")
        .exec(
            http("Search Posts")
                .get("/api/posts")
                .queryParam("keyword", "테스트")
                .queryParam("status", "PUBLISHED")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .check(status().is(200))
        );

    // 시나리오 6: 혼합 시나리오 (실제 사용자 행동 패턴)
    private ScenarioBuilder mixedScenario = scenario("Mixed User Behavior")
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
                    .check(jsonPath("$.authorId").saveAs("authorId"))
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
        .doIf(session -> session.contains("authorId")).then(
            exec(
                http("View Author Profile")
                    .get("/api/users/#{authorId}")
                    .check(status().is(200))
            )
        );

    {
        setUp(
            // 각 시나리오에 사용자 분배 (총 150명)
            browsePostsScenario.injectOpen(
                rampUsers(30).during(Duration.ofSeconds(3))
            ),
            readPostScenario.injectOpen(
                rampUsers(37).during(Duration.ofSeconds(3))
            ),
            browseCategoriesScenario.injectOpen(
                rampUsers(220).during(Duration.ofSeconds(3))
            ),
            viewUserProfileScenario.injectOpen(
                rampUsers(150).during(Duration.ofSeconds(3))
            ),
            searchScenario.injectOpen(
                rampUsers(150).during(Duration.ofSeconds(3))
            ),
            mixedScenario.injectOpen(
                rampUsers(300).during(Duration.ofSeconds(3))
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().max().lt(5000),  // 최대 응답 시간 5초 이하
             global().successfulRequests().percent().gt(95.0)  // 성공률 95% 이상
         );
    }
}
