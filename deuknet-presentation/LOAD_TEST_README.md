# DeukNet API Load Testing with Gatling

## 개요
Gatling을 사용한 DeukNet API 부하 테스트

## 테스트 시나리오

### FullApiLoadTest
전체 API를 대상으로 하는 통합 부하 테스트

**총 사용자 수**: 100명 (30초에 걸쳐 점진적 증가)

**시나리오 구성**:
1. **게시글 목록 조회** (20명)
   - 최신 게시글 조회
   - 인기 게시글 조회

2. **게시글 상세 조회** (25명)
   - 게시글 목록에서 첫 번째 게시글 선택
   - 게시글 상세 정보 조회
   - 댓글 목록 조회

3. **카테고리 조회** (15명)
   - 전체 카테고리 목록 조회
   - 특정 카테고리의 게시글 조회

4. **사용자 프로필 조회** (10명)
   - 게시글 작성자 프로필 조회

5. **검색 기능** (10명)
   - 키워드로 게시글 검색

6. **혼합 시나리오** (20명)
   - 실제 사용자 행동 패턴 시뮬레이션
   - 홈페이지 방문 → 게시글 읽기 → 댓글 확인 → 작성자 프로필 조회

**성능 목표**:
- 최대 응답 시간: 5초 이하
- 성공률: 95% 이상

## 실행 방법

### 1. 서버 준비
```bash
# 백엔드 서버가 localhost:8080에서 실행 중이어야 합니다
# Kubernetes에서 포트 포워딩
kubectl port-forward svc/deuknet-app 8080:8080
```

### 2. Gatling 테스트 실행
```bash
# deuknet-presentation 디렉토리에서 실행
cd /home/kkm06100/IdeaProjects/DeukNet-BE/deuknet-presentation

# Gatling 테스트 실행
../gradlew gatlingRun

# 특정 시뮬레이션만 실행
../gradlew gatlingRun-org.example.deuknetpresentation.simulation.FullApiLoadTest
```

### 3. 결과 확인
테스트가 완료되면 콘솔에 결과 요약이 표시되고, HTML 리포트가 생성됩니다.

리포트 위치: `deuknet-presentation/build/reports/gatling/`

브라우저로 열어서 자세한 결과를 확인할 수 있습니다.

## 테스트 결과 분석

Gatling 리포트는 다음 정보를 제공합니다:
- **응답 시간 분포**: 최소/평균/최대/백분위수
- **요청 성공/실패율**: HTTP 상태 코드별 통계
- **처리량(Throughput)**: 초당 요청 수
- **동시 사용자 수**: 시간별 사용자 증감
- **응답 시간 그래프**: 시간별 응답 시간 추이

## 커스터마이징

### 사용자 수 변경
`FullApiLoadTest.java`에서 `rampUsers()` 파라미터를 수정:
```java
browsePostsScenario.injectOpen(
    rampUsers(50).during(Duration.ofSeconds(60))  // 50명, 60초
),
```

### 테스트 시간 변경
`during()` 파라미터를 수정:
```java
rampUsers(20).during(Duration.ofSeconds(60))  // 60초로 변경
```

### 대상 서버 변경
`BASE_URL` 상수를 수정:
```java
private static final String BASE_URL = "http://your-server:8080";
```

## 주의사항

1. **데이터 준비**: 테스트 전에 충분한 데이터가 있어야 합니다 (게시글, 카테고리 등)
2. **서버 리소스**: 100명 동시 접속 시 서버 리소스를 모니터링하세요
3. **네트워크**: localhost 테스트는 네트워크 지연이 없으므로 실제 환경과 다를 수 있습니다
4. **데이터베이스**: 부하 테스트 후 데이터베이스 상태를 확인하세요

## 트러블슈팅

### 테스트 실패 시
1. 서버가 실행 중인지 확인
2. 포트가 올바른지 확인 (8080)
3. 데이터베이스에 테스트 데이터가 있는지 확인
4. 서버 로그에서 오류 확인

### 성능 개선이 필요한 경우
1. 데이터베이스 인덱스 확인
2. Elasticsearch 동기화 상태 확인
3. 캐싱 전략 검토
4. N+1 쿼리 문제 확인
