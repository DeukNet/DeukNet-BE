# Spring Security & JWT 인증 구현

## 📋 개요
Spring Security를 활용한 JWT 기반 인증 시스템이 구현되었습니다.
모든 Controller는 헤더에서 직접 userId를 받지 않고, Spring Security Context에서 현재 인증된 사용자 정보를 가져옵니다.

## 🔐 인증 흐름

```
1. 클라이언트 → Authorization: Bearer {JWT_TOKEN}
2. JwtAuthenticationFilter → JWT 검증
3. SecurityContext에 UserPrincipal 저장
4. Controller 호출
5. Service (UseCase 구현체) → CurrentUserPort.getCurrentUserId()
6. SecurityUtil → SecurityContext에서 userId 추출
```

## 📦 구현 내역

### 1. Security 설정
- **SecurityConfig**: Spring Security 설정
  - CSRF 비활성화 (Stateless API)
  - Session 사용 안함 (STATELESS)
  - `/api/auth/**` 경로는 인증 불필요
  - 나머지 모든 요청은 인증 필요

### 2. JWT 인증 Filter
- **JwtAuthenticationFilter**: JWT 토큰 검증 및 인증 처리
  - `Authorization: Bearer {token}` 헤더에서 토큰 추출
  - JwtPort를 통해 토큰 검증 및 userId 추출
  - SecurityContext에 인증 정보 저장

### 3. 인증 주체
- **UserPrincipal**: 인증된 사용자 정보 저장
  - userId만 포함 (필요시 확장 가능)

### 4. 현재 사용자 조회
- **CurrentUserPort** (Application Layer): 인터페이스
- **SecurityUtil** (Infrastructure Layer): 구현체
  - SecurityContext에서 현재 인증된 사용자의 userId 추출

## 🏗️ 아키텍처

```
Presentation Layer (Controller)
        ↓
Application Layer (UseCase/Service)
        ↓
    CurrentUserPort.getCurrentUserId()
        ↓
Infrastructure Layer (SecurityUtil)
        ↓
    Spring SecurityContext
```

## 📝 사용 예시

### Before (헤더에서 직접 받기)
```java
@PostMapping
public ResponseEntity<UUID> createPost(
        @RequestBody CreatePostRequest request,
        @RequestHeader("X-User-Id") UUID userId  // ❌ 직접 받음
) {
    CreatePostCommand command = new CreatePostCommand(
            title, content, userId, categoryIds
    );
    return ResponseEntity.ok(createPostUseCase.createPost(command));
}
```

### After (SecurityContext 사용)
```java
@PostMapping
public ResponseEntity<UUID> createPost(
        @RequestBody CreatePostRequest request  // ✅ userId 파라미터 없음
) {
    CreatePostCommand command = new CreatePostCommand(
            title, content, categoryIds  // ✅ userId 제거
    );
    return ResponseEntity.ok(createPostUseCase.createPost(command));
}

// Service에서 처리
@Service
public class CreatePostService implements CreatePostUseCase {
    private final CurrentUserPort currentUserPort;
    
    public UUID createPost(CreatePostCommand command) {
        UUID userId = currentUserPort.getCurrentUserId();  // ✅ 여기서 조회
        // ...
    }
}
```

## 🔑 변경 사항

### 1. UseCase Command 수정
모든 Command에서 `userId` 또는 `authorId` 파라미터 제거:
- ✅ `CreatePostCommand`: authorId 제거
- ✅ `UpdatePostCommand`: authorId 제거
- ✅ `CreateCommentCommand`: authorId 제거
- ✅ `UpdateCommentCommand`: authorId 제거
- ✅ `AddReactionCommand`: userId 제거

### 2. UseCase 인터페이스 수정
모든 메서드 시그니처에서 userId 파라미터 제거:
- ✅ `publishPost(UUID postId)` - authorId 제거
- ✅ `deletePost(UUID postId)` - authorId 제거
- ✅ `deleteComment(UUID commentId)` - authorId 제거
- ✅ `removeReaction(UUID reactionId)` - userId 제거

### 3. Service 구현체 수정
모든 Service에 `CurrentUserPort` 주입:
```java
@Service
public class CreatePostService implements CreatePostUseCase {
    private final CurrentUserPort currentUserPort;
    
    public UUID createPost(CreatePostCommand command) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        // 비즈니스 로직에서 currentUserId 사용
    }
}
```

### 4. Controller 수정
모든 Controller에서 `@RequestHeader("X-User-Id")` 제거:
- ✅ PostController
- ✅ CommentController  
- ✅ ReactionController

## 🛡️ 보안 이점

1. **토큰 위변조 방지**: JWT 서명 검증으로 토큰 무결성 보장
2. **사용자 위장 불가**: 헤더에서 직접 userId를 받지 않아 위조 불가
3. **중앙 집중 인증**: Filter에서 한 번만 검증
4. **권한 확인 자동화**: Service 계층에서 자동으로 현재 사용자 확인

## ⚙️ 설정

### application.yaml
```yaml
jwt:
  secret: your-secret-key-here
  access-token-validity-ms: 3600000  # 1시간
  refresh-token-validity-ms: 604800000  # 7일
```

## 🔄 API 사용법

### 1. 로그인 (인증 불필요)
```bash
POST /api/auth/oauth/login
{
  "code": "authorization-code",
  "provider": "GOOGLE"
}

# Response
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc..."
}
```

### 2. 인증이 필요한 API 호출
```bash
POST /api/posts
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "title": "게시글 제목",
  "content": "게시글 내용",
  "categoryIds": ["uuid1", "uuid2"]
}
```

## 📊 패키지 구조

```
deuknet-application/
└── port/out/security/
    ├── JwtPort.java              # JWT 생성/검증
    └── CurrentUserPort.java      # 현재 사용자 조회 (NEW)

deuknet-infrastructure/
└── security/
    ├── SecurityConfig.java       # Spring Security 설정 (NEW)
    ├── JwtAuthenticationFilter.java  # JWT 필터 (NEW)
    ├── UserPrincipal.java        # 인증 주체 (NEW)
    ├── SecurityUtil.java         # CurrentUserPort 구현 (NEW)
    └── JwtAdapter.java           # JwtPort 구현
```

## ⚠️ 주의사항

1. **JWT Secret**: 프로덕션에서는 강력한 시크릿 키 사용 필수
2. **토큰 만료**: Access Token은 짧게, Refresh Token은 길게 설정
3. **HTTPS 사용**: 프로덕션에서는 반드시 HTTPS 사용
4. **에러 처리**: 인증 실패 시 적절한 HTTP 상태 코드 반환 (401)

## 🔄 향후 개선 사항

1. Refresh Token Rotation
2. 토큰 블랙리스트 (로그아웃)
3. Role 기반 권한 관리
4. Rate Limiting
5. IP 기반 접근 제어
