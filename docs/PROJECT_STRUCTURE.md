# DeukNet 프로젝트 구조

## 아키텍처 개요
Hexagonal Architecture (Clean Architecture) 기반
- **Domain**: 핵심 비즈니스 로직, 엔티티, 값 객체
- **Application**: 유스케이스, 포트 인터페이스, 서비스
- **Infrastructure**: 외부 시스템 연동 (DB, Elasticsearch, Kafka)
- **Presentation**: REST API, 컨트롤러, DTO

## 모듈 구조

```
DeukNet-BE/
├── deuknet-domain/                    # 도메인 계층
│   └── src/main/java/org/example/deuknetdomain/
│       ├── domain/
│       │   ├── post/
│       │   │   ├── Post.java                    # 게시글 집합 루트
│       │   │   ├── PostStatus.java              # DRAFT, PUBLISHED, ARCHIVED, DELETED
│       │   │   └── PostCategoryAssignment.java  # 게시글-카테고리 연결
│       │   ├── category/
│       │   │   └── Category.java                # 카테고리 엔티티
│       │   ├── user/
│       │   │   └── User.java                    # 사용자 엔티티
│       │   ├── comment/
│       │   │   └── Comment.java                 # 댓글 엔티티
│       │   └── reaction/
│       │       ├── Reaction.java                # 반응 엔티티
│       │       └── ReactionType.java            # LIKE, DISLIKE
│       └── common/
│           ├── vo/
│           │   ├── Title.java                   # 제목 값 객체
│           │   └── Content.java                 # 내용 값 객체
│           └── seedwork/
│               ├── AggregateRoot.java
│               └── Entity.java
│
├── deuknet-application/               # 애플리케이션 계층
│   └── src/main/java/org/example/deuknetapplication/
│       ├── port/
│       │   ├── in/                              # 인바운드 포트 (유스케이스)
│       │   │   ├── post/
│       │   │   │   ├── CreatePostUseCase.java
│       │   │   │   ├── UpdatePostUseCase.java
│       │   │   │   ├── PublishPostUseCase.java
│       │   │   │   ├── DeletePostUseCase.java
│       │   │   │   ├── GetPostByIdUseCase.java        # PostgreSQL 조회
│       │   │   │   ├── IncrementViewCountUseCase.java
│       │   │   │   ├── PostSearchResponse.java        # DTO
│       │   │   │   └── PostSearchRequest.java         # DTO
│       │   │   ├── category/
│       │   │   │   ├── GetAllCategoriesUseCase.java
│       │   │   │   └── CategoryResponse.java          # DTO (id, name, parentCategoryId)
│       │   │   └── user/
│       │   │       └── UpdateUserProfileUseCase.java
│       │   └── out/                             # 아웃바운드 포트 (리포지토리)
│       │       └── repository/
│       │           ├── PostRepository.java              # save, findById, delete
│       │           ├── PostCategoryAssignmentRepository.java  # save, deleteByPostId, findByPostId
│       │           ├── CategoryRepository.java          # findRootCategories, findById
│       │           ├── UserRepository.java              # findById, findByAuthCredentialId
│       │           ├── CommentRepository.java           # countByPostId
│       │           └── ReactionRepository.java          # countByTargetIdAndReactionType
│       └── service/
│           ├── post/
│           │   ├── CreatePostService.java
│           │   ├── UpdatePostService.java
│           │   ├── PublishPostService.java
│           │   ├── DeletePostService.java
│           │   ├── GetPostByIdService.java              # PostgreSQL 조회 서비스
│           │   ├── IncrementViewCountService.java
│           │   └── PostSearchService.java               # Elasticsearch 검색
│           ├── category/
│           │   └── GetAllCategoriesService.java
│           └── user/
│               └── UpdateUserProfileService.java
│
├── deuknet-infrastructure/            # 인프라 계층
│   └── src/main/java/org/example/deuknetinfrastructure/
│       ├── data/                                # PostgreSQL JPA
│       │   ├── post/
│       │   │   ├── PostEntity.java
│       │   │   ├── PostCategoryAssignmentEntity.java
│       │   │   ├── JpaPostRepository.java
│       │   │   ├── JpaPostCategoryAssignmentRepository.java
│       │   │   └── PostRepositoryAdapter.java
│       │   ├── category/
│       │   │   ├── CategoryEntity.java
│       │   │   ├── JpaCategoryRepository.java
│       │   │   └── CategoryRepositoryAdapter.java
│       │   ├── user/
│       │   │   ├── UserEntity.java
│       │   │   ├── JpaUserRepository.java
│       │   │   └── UserRepositoryAdapter.java
│       │   ├── comment/
│       │   │   ├── CommentEntity.java
│       │   │   ├── JpaCommentRepository.java
│       │   │   └── CommentRepositoryAdapter.java
│       │   └── reaction/
│       │       ├── ReactionEntity.java
│       │       ├── JpaReactionRepository.java
│       │       └── ReactionRepositoryAdapter.java
│       ├── external/
│       │   └── search/                          # Elasticsearch
│       │       ├── document/
│       │       │   └── PostDetailDocument.java
│       │       ├── repository/
│       │       │   └── ElasticsearchPostRepository.java
│       │       └── adapter/
│       │           └── PostSearchAdapter.java
│       ├── security/
│       │   ├── SecurityConfig.java              # Spring Security 설정
│       │   ├── JwtAuthenticationFilter.java     # JWT 필터
│       │   └── SecurityUtil.java                # CurrentUserPort 구현
│       └── DeuknetApplication.java              # Spring Boot 메인 클래스
│
└── deuknet-presentation/              # 프레젠테이션 계층
    └── src/main/java/org/example/deuknetpresentation/
        ├── controller/
        │   ├── post/
        │   │   ├── PostApi.java                        # Swagger 문서
        │   │   ├── PostController.java                 # REST 컨트롤러
        │   │   └── dto/
        │   │       ├── CreatePostRequest.java
        │   │       └── UpdatePostRequest.java
        │   ├── category/
        │   │   ├── CategoryApi.java
        │   │   └── CategoryController.java
        │   └── user/
        │       ├── UserApi.java
        │       ├── UserController.java
        │       └── dto/
        │           └── UpdateUserProfileRequest.java
        └── security/
            └── UserPrincipal.java                      # 인증 주체 (presentation에 위치)

```

## 주요 패턴

### 1. 도메인 엔티티 생성 패턴
```java
// 새로운 엔티티 생성
Post post = Post.create(title, content, authorId);

// 기존 엔티티 복원 (DB에서 로드)
Post post = Post.restore(id, title, content, authorId, status, viewCount, createdAt, updatedAt);
```

### 2. 리포지토리 인터페이스 패턴
```java
// Application Layer (port/out/repository)
public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(UUID id);
    void delete(Post post);
}

// Infrastructure Layer (어댑터 구현)
@Component
public class PostRepositoryAdapter implements PostRepository {
    private final JpaPostRepository jpaRepository;
    // 구현...
}
```

### 3. 유스케이스 패턴
```java
// Application Layer (port/in)
public interface GetPostByIdUseCase {
    PostSearchResponse getPostById(UUID postId);
}

// Application Layer (service)
@Service
@Transactional(readOnly = true)
public class GetPostByIdService implements GetPostByIdUseCase {
    // 구현...
}
```

### 4. 컨트롤러 패턴
```java
// Presentation Layer
@RestController
@RequestMapping("/api/posts")
public class PostController implements PostApi {
    private final GetPostByIdUseCase getPostByIdUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<PostSearchResponse> getPostById(@PathVariable UUID id) {
        PostSearchResponse response = getPostByIdUseCase.getPostById(id);
        return ResponseEntity.ok(response);
    }
}
```

## 데이터 흐름

### 게시글 조회 (PostgreSQL)
```
1. HTTP GET /api/posts/{id}
2. PostController.getPostById()
3. GetPostByIdService.getPostById()
4. PostRepository.findById() → PostgreSQL
5. UserRepository.findById() → PostgreSQL
6. PostCategoryAssignmentRepository.findByPostId() → PostgreSQL
7. CategoryRepository.findById() → PostgreSQL
8. CommentRepository.countByPostId() → PostgreSQL
9. ReactionRepository.countByTargetIdAndReactionType() → PostgreSQL
10. PostSearchResponse 생성 및 반환
```

### 게시글 검색 (Elasticsearch)
```
1. HTTP GET /api/posts?keyword=...
2. PostController.searchPosts()
3. PostSearchService.search()
4. PostSearchPort → Elasticsearch
5. List<PostSearchResponse> 반환
```

## Security 설정

### SecurityConfig (Infrastructure)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // CORS: WebMvcConfigurer 사용
    .cors(cors -> {})

    // 인증 불필요:
    // - GET /api/posts/**
    // - GET /api/categories
    // - OAuth endpoints
    // - Swagger UI

    // 인증 필요:
    // - POST/PUT/DELETE /api/posts
    // - PUT /api/users/me
    // - POST /api/categories
}
```

### UserPrincipal 위치
- **위치**: `deuknet-presentation/security/UserPrincipal.java`
- **이유**: Infrastructure와 Presentation 모두에서 사용되므로 Presentation에 배치
- **중요**: Infrastructure는 Presentation에 의존 가능

## 중요 의존성 규칙

1. **Domain** → 의존성 없음 (순수 비즈니스 로직)
2. **Application** → Domain
3. **Infrastructure** → Domain, Application, **Presentation** (컨트롤러 스캔 필요)
4. **Presentation** → Domain, Application

## Spring Security 설정 주의사항

1. **SecurityConfig는 Infrastructure에만 존재**
2. **Presentation module의 spring-security는 `compileOnly`**
   - `implementation`으로 설정하면 SecurityAutoConfiguration 활성화됨
3. **DeuknetApplication에서 UserDetailsServiceAutoConfiguration 제외**
   ```java
   @SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
   ```

## DTO 위치 규칙

- **Request DTO**: `presentation/controller/*/dto/`
- **Response DTO**: `application/port/in/*/`
- **이유**: Response는 여러 컨트롤러에서 재사용 가능, Request는 컨트롤러 특화

## 데이터베이스

### PostgreSQL 테이블
- `posts` - 게시글
- `post_category_assignments` - 게시글-카테고리 연결 (N:M)
- `categories` - 카테고리
- `users` - 사용자
- `comments` - 댓글
- `reactions` - 반응 (좋아요/싫어요)

### Elasticsearch 인덱스
- `posts` - 게시글 검색용 (Debezium CDC로 동기화)

## 배포

### Docker 이미지 빌드
```bash
./build-minikube-image.sh
```

### Kubernetes 배포
```bash
kubectl rollout restart deployment/deuknet-app
kubectl rollout status deployment/deuknet-app
```

### 로그 확인
```bash
kubectl logs -l app=deuknet-app --tail=50
```

## 테스트

### 단위 테스트 실행
```bash
./gradlew :deuknet-presentation:test --tests PostControllerTest
```

### 전체 빌드 (테스트 제외)
```bash
./gradlew build -x test
```

## OAuth 설정

### Google OAuth
- Client ID와 Secret은 Kubernetes Secret에 저장
- Secret 이름: `google-oauth-secret`
- 환경변수: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`

### OAuth 플로우
1. `GET /api/auth/oauth/google` - OAuth 시작
2. Google 인증 페이지로 리다이렉트
3. `GET /api/auth/oauth/callback/google` - 콜백 처리
4. JWT 토큰 발급 (accessToken, refreshToken)

### 주의사항
- OAuth state는 메모리에 저장 (ConcurrentHashMap)
- Pod 재시작 시 state 초기화됨
- 프로덕션에서는 Redis 등 외부 저장소 사용 권장

## 최근 구현 내역

### 2025-11-04
- **GET /api/posts/{id}** 엔드포인트 추가
  - PostgreSQL에서 직접 조회 (기존: Elasticsearch)
  - GetPostByIdUseCase, GetPostByIdService 구현
  - PostController 업데이트
  - 통합 테스트 추가 (getPostById, getPostById_notFound)
