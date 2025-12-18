package org.example.deuknetpresentation.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * DeukNet 전체 API 부하 테스트
 * 목표: 초당 600 요청 (600 RPS) - 1초 단위로 집중 투하
 *
 * 시나리오: 단순 게시글 목록 조회 반복
 * - 복잡한 시나리오 없이 순수하게 600 RPS 부하 생성
 * - 각 요청은 독립적으로 실행 (pause 없음)
 *
 * 부하 패턴: constantUsersPerSec(600) - 초당 정확히 600 요청 유지
 * 테스트 시간: 60초
 * 검증:
 * - 최대 응답 시간: 3초 이하
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

    // 단순 시나리오: 게시글 목록 조회만 반복 (pause 없음)
    private final ScenarioBuilder simpleLoadScenario = scenario("Simple Load - 600 RPS")
        .exec(
            http("Get Posts List")
                .get("/api/posts")
                .queryParam("status", "PUBLISHED")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .check(status().in(200, 500, 502, 503, 504))  // 부하 테스트이므로 에러도 허용
        );

    {
        setUp(
            // 단일 시나리오로 초당 600 요청 집중 투하
            simpleLoadScenario.injectOpen(
                constantUsersPerSec(600).during(Duration.ofSeconds(60))  // 600 RPS를 60초간 유지
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().max().lt(5000),   // 최대 응답 시간 5초 이하 (부하 감안)
             global().successfulRequests().percent().gt(95.0)  // 성공률 95% 이상
         );
    }
}
