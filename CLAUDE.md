# DeukNet-BE - Development Guide for Claude Code

## Project Overview

DeukNet-BE is a Spring Boot backend service implementing **Hexagonal Architecture** with **CQRS pattern** and **Event Sourcing** using CDC (Change Data Capture).

### Related Projects
- **Frontend**: `/home/kkm06100/IdeaProjects/DeukNet-FE` (Frontend application repository)

### Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.3.5
- **Build Tool**: Gradle 8.10.2
- **Write DB**: PostgreSQL 15 (with WAL level=logical for CDC)
- **Read DB**: Elasticsearch 8.11 (with Nori Korean analyzer plugin)
- **File Storage**: SeaweedFS
- **CDC**: Debezium 2.7.3 Embedded Engine
- **Deployment**: Kubernetes + Helm

## Module Structure

This is a multi-module Gradle project with clean dependency flow:

```
deuknet-domain          (Core business logic - no external dependencies)
    ↓
deuknet-application     (Use cases, ports/interfaces)
    ↓
deuknet-infrastructure  (Adapters: DB, Kafka, SeaweedFS, Security, CDC)
    ↓
deuknet-presentation    (REST API, Controllers, DTOs)
```

### Module Responsibilities

- **deuknet-domain**: Aggregates, entities, domain exceptions, pure business rules
- **deuknet-application**: Use case implementations, port interfaces (in/out), application exceptions
- **deuknet-infrastructure**:
  - Persistence adapters (JPA entities, repositories)
  - CDC engine (Debezium Embedded)
  - External service adapters (SeaweedFS, Elasticsearch)
  - Security configuration
- **deuknet-presentation**: REST controllers, request/response DTOs, exception handlers

## Key Architectural Patterns

### 1. CQRS (Command Query Responsibility Segregation)
- **Commands (Write)**: PostgreSQL → Domain models → JPA persistence
- **Queries (Read)**: Elasticsearch → Search optimized models
- **Synchronization**: Debezium CDC captures PostgreSQL changes → Kafka → Elasticsearch indexing

### 2. Hexagonal Architecture (Ports & Adapters)
- **Ports**: Interfaces in `deuknet-application` layer
  - `port.in.*UseCase`: Input ports (application entry points)
  - `port.out.*Port`: Output ports (infrastructure abstractions)
- **Adapters**: Implementations in `deuknet-infrastructure`
  - Persistence adapters implement output ports
  - REST controllers in `deuknet-presentation` call input ports

### 3. Outbox Pattern with CDC
- Domain events stored in `outbox` table
- Debezium captures changes from PostgreSQL WAL
- Events published to Kafka topics
- Consumers update Elasticsearch for read models

### 4. Aggregate Design Principles
- **Small aggregates**: Single entity or entity + value objects
- **ID-based references**: Aggregates reference others by ID, not direct object references
- **Clear transaction boundaries**: One aggregate per transaction
- **Eventual consistency**: Between aggregates via domain events

### 5. Document/Projection 동일성 원칙 (Critical for CQRS)

**⚠️ 매우 중요: Elasticsearch Document와 Application Projection은 동일한 필드 구조를 유지해야 합니다.**

#### 원칙

1. **필드 구조 동일성**
   - `PostDetailDocument` (Elasticsearch)와 `PostDetailProjection` (Application)은 **정확히 동일한 필드**를 가져야 합니다
   - 필드 추가/제거 시 **반드시 양쪽 모두 수정**해야 합니다
   - 타입도 호환 가능해야 합니다 (UUID ↔ String 변환은 Mapper에서 처리)

2. **ID만 저장, 상세 정보는 별도 조회**
   - ❌ **저장하지 않음**: `authorUsername`, `authorDisplayName`, `authorAvatarUrl`, `categoryName`
   - ✅ **저장함**: `authorId`, `authorType`, `categoryId`
   - 이유: 사용자/카테고리 정보 변경 시 모든 게시글 Document를 업데이트할 필요 없음
   - 상세 정보는 조회 시 별도로 join하거나 Service Layer에서 enrichment

3. **Count 필드는 Projection에 통합**
   - ~~`PostCountProjection` (제거됨)~~
   - `PostDetailProjection`에 `commentCount`, `likeCount`, `dislikeCount`, `viewCount` 포함
   - 이벤트 발행 시 `PostDetailProjection` 하나만 발행

#### 예시: 필드 추가 시

```java
// 1. PostDetailDocument에 필드 추가
@Field(type = FieldType.Keyword)
private String newField;

// 2. PostDetailProjection에도 동일하게 추가
private final String newField;

// 3. PostDetailDocument.create() 메서드 파라미터 추가
public static PostDetailDocument create(..., String newField) {
    document.newField = newField;
}

// 4. PostDetailProjection.Builder에 파라미터 추가
@Builder
public PostDetailProjection(..., String newField) {
    this.newField = newField;
}

// 5. PostDetailDocumentMapper 양방향 변환 수정
// toProjection()과 toDocument() 모두 수정
```

#### 잘못된 예시 (절대 금지)

```java
// ❌ Document에만 필드 추가
@Document
class PostDetailDocument {
    private String onlyInDocument;  // 동기화 실패 원인!
}

// ❌ Projection에만 필드 추가
class PostDetailProjection {
    private String onlyInProjection;  // CDC 이벤트 시 누락!
}

// ❌ 상세 정보 저장 (User/Category 변경 시 업데이트 부담)
class PostDetailDocument {
    private String authorUsername;  // ID만 저장해야 함
    private String categoryName;    // ID만 저장해야 함
}
```

#### 체크리스트

필드 추가/제거 시 다음을 모두 확인하세요:

- [ ] `PostDetailDocument` 필드 수정
- [ ] `PostDetailProjection` 필드 수정
- [ ] `PostDetailDocument.create()` 메서드 수정
- [ ] `PostDetailProjection.Builder` 생성자 수정
- [ ] `PostDetailDocumentMapper.toProjection()` 수정
- [ ] `PostDetailDocumentMapper.toDocument()` 수정
- [ ] `PostProjectionFactory` 메서드들 수정
- [ ] `PostRepositoryAdapter.findDetailById()` 수정 (QueryDSL Projection)
- [ ] Elasticsearch 인덱스 매핑 재생성 (필요 시)

#### 이 원칙을 지켜야 하는 이유

1. **CDC 동기화 보장**: Command Model → Event → Elasticsearch 흐름에서 데이터 누락 방지
2. **일관성**: PostgreSQL과 Elasticsearch 간 데이터 불일치 방지
3. **유지보수성**: 필드 변경 시 체계적으로 추적 가능
4. **성능**: 불필요한 JOIN 없이 Document만으로 조회 가능 (ID만 저장하되, 필수 검색 필드는 비정규화)

### 6. 익명 처리 패턴 (AuthorType Enum 기반)

**⚠️ 매우 중요: Post와 Comment의 익명 처리는 AuthorType Enum을 기반으로 통일되어야 합니다.**

#### 원칙

1. **AuthorType Enum 사용**
   - `AuthorType.REAL`: 실명 작성
   - `AuthorType.ANONYMOUS`: 익명 작성
   - Domain Entity (Post, Comment)에서 `AuthorType` 필드 사용
   - Database에는 `VARCHAR(20)` 타입으로 저장 (`@Enumerated(EnumType.STRING)`)

2. **Projection은 String 타입으로 전달**
   - Projection은 Document와 1:1 매핑되는 개념
   - `PostDetailProjection`, `CommentProjection`에서는 `authorType`을 `String`으로 저장
   - CDC 이벤트 발행 시 `comment.getAuthorType().name()`으로 변환

3. **Response DTO는 Enum 타입 사용**
   - `PostSearchResponse`, `CommentResponse`에서는 `AuthorType` Enum 타입 사용
   - Projection에서 Response 생성 시 `AuthorType.valueOf(projection.getAuthorType())`로 변환
   - `AuthorInfoEnrichable` 인터페이스 구현하여 익명 처리 통일

4. **UserRepository를 통한 익명 처리**
   - `UserRepository.enrichWithUserInfo(AuthorInfoEnrichable response)` 메서드 사용
   - `ANONYMOUS`: authorId를 null로 설정, username/displayName을 "익명"으로 설정
   - `REAL`: DB에서 User 조회하여 실제 정보 설정

#### 구현 예시

```java
// Domain Entity
@Getter
public class Comment extends AggregateRoot {
    private final AuthorType authorType;  // Enum 사용

    public static Comment create(..., AuthorType authorType) {
        return new Comment(..., authorType, ...);
    }
}

// Entity (Persistence)
@Entity
public class CommentEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false, length = 20)
    private AuthorType authorType;  // DB에 "REAL" 또는 "ANONYMOUS" 문자열로 저장
}

// Projection (Document와 1:1)
public class CommentProjection extends Projection {
    private final String authorType;  // String 타입

    public CommentProjection(..., String authorType, ...) {
        this.authorType = authorType;
    }
}

// Service - Projection 생성 시
CommentProjection projection = new CommentProjection(
    ...,
    comment.getAuthorType().name(),  // Enum → String 변환
    ...
);

// Response DTO
public class CommentResponse implements AuthorInfoEnrichable {
    private AuthorType authorType;  // Enum 타입

    public CommentResponse(CommentProjection projection) {
        this.authorType = AuthorType.valueOf(projection.getAuthorType());  // String → Enum 변환
    }
}

// UserRepository 인터페이스
public interface AuthorInfoEnrichable {
    UUID getAuthorId();
    void setAuthorId(UUID authorId);
    AuthorType getAuthorType();
    void setAuthorUsername(String username);
    void setAuthorDisplayName(String displayName);
}

public interface UserRepository {
    void enrichWithUserInfo(AuthorInfoEnrichable response);
}

// UserRepositoryAdapter 구현
@Override
public void enrichWithUserInfo(AuthorInfoEnrichable response) {
    if (AuthorType.ANONYMOUS.equals(response.getAuthorType())) {
        // 익명 작성물은 User 정보 숨김
        response.setAuthorId(null);
        response.setAuthorUsername("익명");
        response.setAuthorDisplayName("익명");
    } else if (AuthorType.REAL.equals(response.getAuthorType())) {
        // 실명 작성물은 User 조회
        findById(response.getAuthorId()).ifPresent(user -> {
            response.setAuthorUsername(user.getUsername());
            response.setAuthorDisplayName(user.getDisplayName());
        });
    }
}

// Service - 익명 처리
private void enrichWithUserInfo(CommentResponse response) {
    userRepository.enrichWithUserInfo(response);  // 통일된 익명 처리
}
```

#### 체크리스트

익명 기능 추가 시 다음을 모두 확인하세요:

- [ ] Domain Entity에 `AuthorType` 필드 추가 (`Comment`, `Post`)
- [ ] Entity에 `@Enumerated(EnumType.STRING)` 컬럼 추가
- [ ] Mapper에서 `AuthorType` 변환 처리
- [ ] Projection에 `String authorType` 필드 추가
- [ ] Service에서 Projection 생성 시 `.name()` 변환
- [ ] Response DTO에 `AuthorType` 필드 추가 및 `AuthorInfoEnrichable` 구현
- [ ] Response 생성자에서 `AuthorType.valueOf()` 변환
- [ ] Service에서 `userRepository.enrichWithUserInfo()` 호출

#### 이 패턴을 지켜야 하는 이유

1. **통일성**: Post와 Comment가 동일한 익명 처리 로직 사용
2. **타입 안정성**: Enum을 사용하여 컴파일 타임에 오류 방지
3. **유지보수성**: 익명 처리 로직이 UserRepository에 집중되어 수정이 용이
4. **확장성**: 새로운 작성물 타입(Reply 등) 추가 시 동일한 패턴 적용 가능

### 7. CQRS 사용 기준과 Service 패턴

**⚠️ 매우 중요: 모든 Entity가 CQRS를 사용하는 것은 아닙니다. CQRS 사용 여부에 따라 Service 패턴이 달라집니다.**

#### CQRS 사용 기준

##### ✅ CQRS를 사용하는 경우 (Elasticsearch 기반 검색 필요)

- **Post**: 전문 검색, 필터링, 정렬 등 복잡한 검색 요구사항
  - PostgreSQL (Write) + Elasticsearch (Read)
  - CDC를 통한 동기화
  - Projection과 Document 사용

##### ❌ CQRS를 사용하지 않는 경우 (단순 CRUD)

- **User**: 단순 조회, 업데이트 작업만 필요
- **Category**: 단순 목록 조회, 생성/수정/삭제
- **Comment**: 게시글별 댓글 목록 조회 (복잡한 검색 불필요)
  - PostgreSQL만 사용 (Write + Read)
  - Projection 사용 안함
  - Domain 객체를 직접 Response로 변환

#### Service 패턴 차이

##### 1. CQRS를 사용하는 Entity (예: Post)

**Repository**:
```java
// Application Layer - Port
public interface PostRepository {
    // Command: Domain 객체 반환
    Post save(Post post);
    Optional<Post> findById(UUID id);

    // Query: Projection 반환
    Optional<PostDetailProjection> findDetailById(UUID id);
    Page<PostSearchProjection> findByFilters(...);
}
```

**Service**:
```java
@Service
@Transactional
public class CreatePostService implements CreatePostUseCase {
    private final PostRepository postRepository;
    private final DataChangeEventPublisher eventPublisher;

    @Override
    public UUID createPost(CreatePostApplicationRequest request) {
        // 1. Domain 객체 생성 및 저장
        Post post = Post.create(...);
        postRepository.save(post);

        // 2. Projection 생성 및 이벤트 발행 (CDC를 통해 Elasticsearch로 전달)
        PostDetailProjection projection = projectionFactory.createDetailProjection(post, ...);
        eventPublisher.publish(EventType.POST_CREATED, post.getId(), projection);

        return post.getId();
    }
}

@Service
@Transactional(readOnly = true)
public class GetPostDetailService implements GetPostDetailUseCase {
    private final PostRepository postRepository;

    @Override
    public PostDetailResponse getPostDetail(UUID postId) {
        // Repository에서 직접 Projection 조회 (Elasticsearch에서 읽음)
        PostDetailProjection projection = postRepository.findDetailById(postId)
                .orElseThrow(PostNotFoundException::new);

        return PostDetailResponse.from(projection);
    }
}
```

**이벤트 발행**:
- `POST_CREATED`: 게시글 생성 시
- `POST_UPDATED`: 게시글 수정 시
- `POST_DELETED`: 게시글 삭제 시
- 모든 이벤트는 Projection을 payload로 전달

##### 2. CQRS를 사용하지 않는 Entity (예: User, Category, Comment)

**Repository**:
```java
// Application Layer - Port
public interface CommentRepository {
    // Command & Query 모두 Domain 객체 반환 (Projection 없음)
    Comment save(Comment comment);
    Optional<Comment> findById(UUID id);
    List<Comment> findByPostId(UUID postId);
    void delete(Comment comment);
}
```

**Service**:
```java
@Service
@Transactional
public class CreateCommentService implements CreateCommentUseCase {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final DataChangeEventPublisher eventPublisher;

    @Override
    public UUID createComment(CreateCommentApplicationRequest request) {
        // 1. Domain 객체 생성 및 저장
        Comment comment = Comment.create(...);
        commentRepository.save(comment);

        // 2. 연관된 CQRS Entity(Post)의 Projection만 발행
        // Comment 자체의 이벤트는 발행하지 않음!
        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(PostNotFoundException::new);
        PostDetailProjection projection = projectionFactory.createDetailProjectionForUpdate(
                post, ..., commentCount + 1, ...);
        eventPublisher.publish(EventType.POST_UPDATED, post.getId(), projection);

        return comment.getId();
    }
}

@Service
@Transactional(readOnly = true)
public class GetCommentsService implements GetCommentsUseCase {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public List<CommentResponse> getCommentsByPostId(UUID postId) {
        // 1. Repository에서 Domain 객체 조회 (PostgreSQL에서 직접 읽음)
        List<Comment> comments = commentRepository.findByPostId(postId);

        // 2. Service Layer에서 toResponse() 메서드로 변환
        return comments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse toResponse(Comment comment) {
        // Domain 객체 → Response DTO 변환 로직
        boolean isAuthor = isCurrentUserAuthor(comment);

        // 익명 처리
        if (comment.getAuthorType() == AuthorType.ANONYMOUS) {
            return new CommentResponse(comment.getId(), ..., null, "익명", ...);
        } else {
            User author = userRepository.findById(comment.getAuthorId()).orElse(null);
            return new CommentResponse(comment.getId(), ...,
                    author.getUsername(), author.getDisplayName(), ...);
        }
    }
}

@Service
@Transactional
public class UpdateCommentService implements UpdateCommentUseCase {
    private final CommentRepository commentRepository;

    @Override
    public void updateComment(UpdateCommentApplicationRequest request) {
        // 1. Domain 객체 조회 및 업데이트
        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(CommentNotFoundException::new);

        comment.updateContent(Content.from(request.getContent()));
        commentRepository.save(comment);

        // 2. 이벤트 발행 안함! (Comment는 CQRS 사용 안함)
        // 필요하면 연관된 Post의 Projection만 업데이트 (댓글 수 등)
    }
}
```

**이벤트 발행**:
- Comment 자체의 이벤트는 발행하지 않음
- 연관된 CQRS Entity(Post)의 통계 업데이트만 발행
  - 댓글 생성/삭제 시 `POST_UPDATED` 이벤트로 댓글 수 업데이트

#### 패턴 비교 요약

| 구분 | CQRS 사용 (Post) | CQRS 미사용 (User, Category, Comment) |
|------|-----------------|-------------------------------------|
| **Read DB** | Elasticsearch | PostgreSQL |
| **Write DB** | PostgreSQL | PostgreSQL |
| **Repository 반환 타입** | Projection (Query), Domain (Command) | Domain (Command & Query) |
| **Service 변환** | `Projection → Response` | `Domain → Response (toResponse())` |
| **이벤트 발행** | Entity 생성/수정/삭제 시 발행 | 발행 안함 (연관 Entity만 업데이트) |
| **Projection 클래스** | 필요 (Document와 1:1 매핑) | 불필요 |
| **Document 클래스** | 필요 (Elasticsearch) | 불필요 |

#### 체크리스트

새로운 Entity 추가 시:

- [ ] **CQRS 필요성 판단**: 복잡한 검색 요구사항이 있는가?
  - Yes → Post 패턴 따르기 (Projection, Document, CDC)
  - No → User/Category/Comment 패턴 따르기 (Domain only)

- [ ] **Repository 인터페이스 설계**
  - CQRS: Query 메서드는 Projection 반환, Command 메서드는 Domain 반환
  - Non-CQRS: 모든 메서드는 Domain 반환

- [ ] **Service 구현**
  - CQRS: Repository에서 Projection 조회 → Response 변환
  - Non-CQRS: Repository에서 Domain 조회 → toResponse() 메서드로 변환

- [ ] **이벤트 발행 여부**
  - CQRS: 생성/수정/삭제 시 해당 Entity의 Projection 발행
  - Non-CQRS: 이벤트 발행 안함 (연관된 CQRS Entity만 업데이트)

#### 이 패턴을 지켜야 하는 이유

1. **복잡도 관리**: CQRS는 필요한 곳에만 적용하여 시스템 복잡도 최소화
2. **일관성**: 동일한 특성을 가진 Entity는 동일한 패턴 사용
3. **유지보수성**: 패턴이 명확하여 새로운 개발자도 쉽게 이해 가능
4. **성능**: 단순 CRUD는 PostgreSQL 직접 조회로 충분 (Elasticsearch 오버헤드 제거)
5. **비용**: Elasticsearch 인덱스는 필요한 데이터만 저장하여 인프라 비용 절감

## Common Commands

### Build & Test
```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :deuknet-application:build

# Run tests
./gradlew test

# Run tests for specific module
./gradlew :deuknet-presentation:test

# Skip tests
./gradlew build -x test

# Clean build
./gradlew clean build
```

### Local Development

#### Start External Services (Docker Compose)
```bash
# Start all external dependencies (PostgreSQL, Elasticsearch, SeaweedFS)
docker compose up -d

# View logs
docker compose logs -f

# Stop services
docker compose down

# Stop and remove volumes (fresh start)
docker compose down -v
```

**Important**: The docker-compose setup is for **external services only**. Run the Spring Boot application in your IDE for debugging.

#### PostgreSQL Configuration for CDC
The docker-compose PostgreSQL is pre-configured with:
- `wal_level=logical` (required for Debezium)
- `max_wal_senders=10`
- `max_replication_slots=10`

#### Elasticsearch with Nori Plugin
Uses custom Dockerfile (`docker/Dockerfile.elasticsearch`) that installs the Korean analyzer plugin:
```dockerfile
FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.0
RUN bin/elasticsearch-plugin install analysis-nori
```

### Kubernetes Deployment (Minikube)

```bash
# Start Minikube
minikube start

# Build image for Minikube
./build-minikube-image.sh

# Deploy with Helm
helm install deuknet ./helm/deuknet

# Upgrade deployment
helm upgrade deuknet ./helm/deuknet

# Port forwarding
kubectl port-forward svc/deuknet-app 8080:8080

# View logs
kubectl logs -f deployment/deuknet-app

# Check CDC status
kubectl exec -it deployment/deuknet-app -- curl localhost:8080/actuator/health
```

## Important Development Notes

### File Upload Security
File validation is implemented at the **infrastructure boundary** (adapter layer):

- **SeaweedFSFileStorageAdapter** performs:
  - MIME type whitelist validation
  - File extension validation
  - Path traversal attack prevention (blocks `..`, `/`, `\\`)

- **FileUploadService** (application layer) handles:
  - Business rule validation (file size limits: 10MB max)
  - URL generation

**Security principle**: Technical validation at infrastructure layer, business rules at application layer.

### Database Constraints
- **No unique constraint on reactions**: Decided to allow duplicate reactions for simplicity
  - User quote: "같은 계정의 유저가 동시에 좋아요를 요청하는일이 잘 일어나지는 않으니까"
  - Trade-off: Simplicity over strict uniqueness enforcement

### CDC Pipeline Flow
```
PostgreSQL WAL
  → Debezium Embedded Engine (in-process)
  → Debezium Event Handler(in codebase)
  → ProjectionCommenPOrt
  → Elasticsearch Indices (with Nori tokenization)
```

### Exception Handling
- **Domain exceptions**: Extend `DomainException` in `deuknet-domain`
- **Application exceptions**: Extend `ApplicationException` in `deuknet-application`
- **Global handler**: `GlobalExceptionHandler` in `deuknet-presentation` catches all `DeukNetException`

### Dependency Management
- Kafka version **forced to 3.7.0** in root `build.gradle` for Debezium 2.7.3 compatibility
- Elasticsearch requires Nori plugin installation (not available in base image)

## Troubleshooting

### PostgreSQL Replication Slot Error
If Debezium fails with "replication slot already exists":
```bash
docker compose down -v  # Remove volumes to reset WAL
docker compose up -d
```

### Elasticsearch Nori Plugin Not Found
If you see "Unknown tokenizer type [nori_tokenizer]":
- Ensure `docker/Dockerfile.elasticsearch` is present
- Rebuild the image: `docker compose build elasticsearch`
- Restart: `docker compose up -d elasticsearch`

### Minikube Image Not Found
After building the app, images must be loaded into Minikube:
```bash
./build-minikube-image.sh
```

## Development Workflow

1. **Start external services**: `docker compose up -d`
2. **Run Spring Boot app** in IDE with debug mode
3. **Make changes** following hexagonal architecture layers
4. **Write tests** at appropriate layers (unit tests for domain/application, integration tests for infrastructure)
5. **Build**: `./gradlew build`
6. **Deploy to Minikube** (optional): Build image → Helm upgrade

## Key Configuration Files

- `application.yaml`: Spring Boot configuration (DB, Kafka, SeaweedFS connection strings)
- `docker-compose.yaml`: Local development external services
- `helm/deuknet/values.yaml`: Kubernetes deployment configuration
- `build.gradle`: Dependency versions and module configuration

## Coding Standards and Best Practices

### 1. 현재 사용자 정보 조회

**⚠️ 중요: Service 레이어에서 현재 사용자 정보는 `CurrentUserPort`를 통해 조회해야 합니다.**

#### 원칙

- **Controller에서 userId를 파라미터로 전달하지 않습니다**
- **Service에서 `CurrentUserPort.getCurrentUserId()`를 직접 호출합니다**
- **UseCase 인터페이스에 userId 파라미터를 포함하지 않습니다**

#### 올바른 예시

```java
// ✅ GOOD - Service에서 CurrentUserPort 사용
@Service
@Transactional
public class UpdateCategoryService implements UpdateCategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;

    @Override
    public void updateCategory(UUID categoryId, UpdateCategoryApplicationRequest request) {
        // CurrentUserPort로 현재 사용자 ID 조회
        UUID currentUserId = currentUserPort.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(UserNotFoundException::new);

        // 권한 검증 및 업데이트 로직
        validateUpdatePermission(category, user);
        // ...
    }
}

// ✅ GOOD - Controller는 userId를 전달하지 않음
@PutMapping("/{categoryId}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void updateCategory(@PathVariable UUID categoryId, @RequestBody UpdateCategoryRequest request) {
    updateCategoryUseCase.updateCategory(categoryId, request);
}
```

#### 잘못된 예시

```java
// ❌ BAD - Controller에서 userId를 추출하여 전달
@PutMapping("/{categoryId}")
public void updateCategory(@PathVariable UUID categoryId, @RequestBody UpdateCategoryRequest request) {
    UUID userId = currentUserPort.getCurrentUserId();  // Controller에서 하면 안됨!
    updateCategoryUseCase.updateCategory(categoryId, userId, request);
}

// ❌ BAD - UseCase에 userId 파라미터 포함
public interface UpdateCategoryUseCase {
    void updateCategory(UUID categoryId, UUID userId, UpdateCategoryApplicationRequest request);
}
```

#### 이유

1. **책임 분리**: 인증/인가는 Service 레이어의 책임
2. **테스트 용이성**: Service 테스트 시 CurrentUserPort를 Mock으로 대체 가능
3. **일관성**: 모든 Service에서 동일한 패턴 사용
4. **보안**: Controller에서 userId를 조작할 가능성 제거

### 2. 예외 처리 원칙

**⚠️ 중요: `DeukNetException`을 상속하지 않는 표준 Java 예외 사용을 자제해야 합니다.**

#### 원칙

- **Domain 예외**: `DomainException` 상속
- **Application 예외**: `ApplicationException` 상속
- **`IllegalArgumentException`, `IllegalStateException` 등 표준 예외 사용 금지**

#### 올바른 예시

```java
// ✅ GOOD - UserNotFoundException 사용 (DomainException 상속)
User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

// ✅ GOOD - CategoryNotFoundException 사용 (DomainException 상속)
Category category = categoryRepository.findById(categoryId)
        .orElseThrow(CategoryNotFoundException::new);
```

#### 잘못된 예시

```java
// ❌ BAD - IllegalArgumentException 사용
User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

// ❌ BAD - IllegalStateException 사용
if (category == null) {
    throw new IllegalStateException("Category is required");
}
```

#### 예외 생성 가이드

새로운 예외가 필요한 경우:

```java
// Domain 예외 예시
package org.example.deuknetdomain.domain.user.exception;

import org.example.deuknetdomain.common.exception.DomainException;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException() {
        super(404, "USER_NOT_FOUND", "User not found");
    }
}
```

#### 이유

1. **일관된 예외 처리**: `GlobalExceptionHandler`에서 모든 `DeukNetException` 일괄 처리
2. **명확한 에러 코드**: HTTP 상태 코드와 에러 코드를 명시적으로 정의
3. **API 응답 일관성**: 클라이언트가 예측 가능한 에러 응답 형식 제공
4. **디버깅 용이성**: 예외 타입만으로도 어떤 문제인지 즉시 파악 가능

### 3. Import 문 작성 원칙

**⚠️ 중요: 와일드카드(`*`) import를 사용하지 마세요.**

#### 원칙

- **개별 클래스를 명시적으로 import합니다**
- **`import org.example.*` 같은 와일드카드 import 금지**

#### 올바른 예시

```java
// ✅ GOOD - 명시적 import
import org.example.deuknetapplication.port.in.category.CategoryRankingResponse;
import org.example.deuknetapplication.port.in.category.CategoryResponse;
import org.example.deuknetapplication.port.in.category.CreateCategoryUseCase;
import org.example.deuknetapplication.port.in.category.DeleteCategoryUseCase;
import org.example.deuknetapplication.port.in.category.GetAllCategoriesUseCase;
```

#### 잘못된 예시

```java
// ❌ BAD - 와일드카드 import
import org.example.deuknetapplication.port.in.category.*;
import org.example.deuknetdomain.domain.user.exception.*;
```

#### 이유

1. **가독성**: 어떤 클래스를 사용하는지 명확히 파악 가능
2. **네이밍 충돌 방지**: 다른 패키지의 동일한 클래스명 사용 시 문제 방지
3. **IDE 지원**: 자동 완성 및 리팩토링 도구가 더 정확하게 동작
4. **코드 리뷰**: 의존성 변경 사항을 명확히 확인 가능

### 4. 정적 팩토리 메서드 사용 원칙

**⚠️ 중요: Service, Adapter 등의 로직에서 생성자나 Builder 패턴을 직접 사용하지 말고, DTO에 정적 팩토리 메서드를 제공하세요.**

#### 원칙

- **Response/Request DTO는 정적 팩토리 메서드(`from()`, `of()`)를 제공합니다**
- **Service, Adapter에서는 생성자나 Builder를 직접 호출하지 않습니다**
- **정적 팩토리 메서드 명명 규칙**:
  - `from(Domain)`: Domain 객체로부터 DTO 생성
  - `of(...)`: 여러 파라미터로부터 DTO 생성
  - `fromProjection(Projection)`: Projection 객체로부터 DTO 생성

#### 올바른 예시

```java
// ✅ GOOD - Response DTO에 정적 팩토리 메서드 제공
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private UserRole role;

    /**
     * Domain 객체로부터 Response 생성
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getRole()
        );
    }
}

// ✅ GOOD - Service에서 정적 팩토리 메서드 사용
@Service
@Transactional(readOnly = true)
public class GetUserByIdService implements GetUserByIdUseCase {
    private final UserRepository userRepository;

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return UserResponse.from(user);  // 정적 팩토리 메서드 사용
    }
}

// ✅ GOOD - CommentResponse with AuthorType 처리
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private UUID id;
    private UUID postId;
    private String content;
    private UUID authorId;
    private String authorUsername;
    // ... other fields

    /**
     * Domain 객체와 User 정보로부터 Response 생성
     */
    public static CommentResponse from(Comment comment, User author, boolean isAuthor) {
        if (comment.getAuthorType() == AuthorType.ANONYMOUS) {
            return new CommentResponse(
                    comment.getId(),
                    comment.getPostId(),
                    comment.getContent().getValue(),
                    null,  // authorId 숨김
                    "익명",
                    "익명",
                    null,  // avatarUrl 숨김
                    comment.getParentCommentId().orElse(null),
                    comment.isReply(),
                    comment.getAuthorType(),
                    isAuthor,
                    comment.getCreatedAt(),
                    comment.getUpdatedAt()
            );
        } else {
            return new CommentResponse(
                    comment.getId(),
                    comment.getPostId(),
                    comment.getContent().getValue(),
                    author.getId(),
                    author.getUsername(),
                    author.getDisplayName(),
                    author.getAvatarUrl(),
                    comment.getParentCommentId().orElse(null),
                    comment.isReply(),
                    comment.getAuthorType(),
                    isAuthor,
                    comment.getCreatedAt(),
                    comment.getUpdatedAt()
            );
        }
    }
}

// ✅ GOOD - Service에서 정적 팩토리 메서드 사용
@Service
@Transactional(readOnly = true)
public class GetCommentsService implements GetCommentsUseCase {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public List<CommentResponse> getCommentsByPostId(UUID postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse toResponse(Comment comment) {
        boolean isAuthor = isCurrentUserAuthor(comment);

        if (comment.getAuthorType() == AuthorType.ANONYMOUS) {
            return CommentResponse.from(comment, null, isAuthor);  // 정적 팩토리 메서드
        } else {
            User author = userRepository.findById(comment.getAuthorId()).orElse(null);
            return CommentResponse.from(comment, author, isAuthor);  // 정적 팩토리 메서드
        }
    }
}
```

#### 잘못된 예시

```java
// ❌ BAD - Service에서 생성자 직접 호출
@Service
public class GetUserByIdService implements GetUserByIdUseCase {
    @Override
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 생성자 직접 호출 - 가독성이 떨어지고 코드 중복 발생
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getRole()
        );
    }
}

// ❌ BAD - Builder 패턴 직접 사용 (간단한 DTO의 경우)
@Service
public class GetUserByIdService implements GetUserByIdUseCase {
    @Override
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // Builder 직접 사용 - 불필요하게 장황함
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }
}

// ❌ BAD - Service에서 복잡한 변환 로직 (DTO에 있어야 함)
@Service
public class GetCommentsService implements GetCommentsUseCase {
    private CommentResponse toResponse(Comment comment) {
        boolean isAuthor = isCurrentUserAuthor(comment);

        // 변환 로직이 Service에 흩어져 있음 - DTO의 정적 팩토리 메서드로 이동해야 함
        if (comment.getAuthorType() == AuthorType.ANONYMOUS) {
            return new CommentResponse(
                    comment.getId(),
                    comment.getPostId(),
                    comment.getContent().getValue(),
                    null,
                    "익명",
                    "익명",
                    null,
                    // ... 20개 파라미터
            );
        } else {
            User author = userRepository.findById(comment.getAuthorId()).orElse(null);
            return new CommentResponse(
                    comment.getId(),
                    comment.getPostId(),
                    comment.getContent().getValue(),
                    author.getId(),
                    author.getUsername(),
                    // ... 20개 파라미터
            );
        }
    }
}
```

#### 정적 팩토리 메서드의 장점

1. **가독성**: `UserResponse.from(user)`는 의도가 명확함
2. **코드 중복 제거**: 동일한 변환 로직이 여러 Service에서 재사용됨
3. **캡슐화**: DTO 생성 로직이 DTO 클래스 내부에 캡슐화됨
4. **유지보수성**: 필드 추가/제거 시 정적 팩토리 메서드만 수정하면 됨
5. **테스트 용이성**: DTO 변환 로직을 독립적으로 테스트 가능

#### Builder 패턴은 언제 사용하는가?

Builder 패턴은 다음 경우에만 사용하세요:

- **선택적 파라미터가 많은 경우** (10개 이상)
- **파라미터 조합이 복잡한 경우**
- **불변 객체 생성이 필요하고 필드가 많은 경우**

대부분의 간단한 DTO는 정적 팩토리 메서드로 충분합니다.

#### Record 타입 사용

Java 14+의 Record는 이미 간결하므로 정적 팩토리 메서드가 선택사항입니다:

```java
// ✅ GOOD - Record는 간결하므로 그대로 사용 가능
public record FileUploadResponse(
    String fileName,
    String fileUrl,
    long size
) {}

// Service에서 직접 생성 가능
return new FileUploadResponse(fileName, fileUrl, size);  // OK
```

#### 체크리스트

새로운 Response DTO 작성 시:

- [ ] 정적 팩토리 메서드(`from()`, `of()`) 제공
- [ ] Service에서 생성자/Builder 직접 호출 금지
- [ ] 복잡한 변환 로직은 DTO 내부로 캡슐화
- [ ] Record 타입은 선택적으로 정적 팩토리 메서드 추가

## Further Reading

- Main README.md: Detailed architecture explanation and aggregate design principles
- helm/deuknet/README.md: CDC pipeline details and Kubernetes deployment guide
