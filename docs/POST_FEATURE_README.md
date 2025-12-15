# Post 기능 구현 완료

## 📋 개요
게시글(Post), 댓글(Comment), 리액션(Reaction) 관련 Command 기능이 구현되었습니다.
Query 부분은 별도로 구현 예정입니다.

## 🚀 주요 기능

### 1. Post 관리
- ✅ 게시글 생성 (초안 상태)
- ✅ 게시글 수정
- ✅ 게시글 발행
- ✅ 게시글 삭제 (Soft Delete)
- ✅ 카테고리 다중 할당

### 2. Comment 관리
- ✅ 댓글 작성
- ✅ 대댓글 작성 (parentCommentId)
- ✅ 댓글 수정
- ✅ 댓글 삭제

### 3. Reaction 관리
- ✅ 리액션 추가 (좋아요 등)
- ✅ 리액션 삭제

## 📦 패키지 구조

```
deuknet-domain/
└── model/command/
    ├── post/
    │   ├── post/
    │   │   ├── Post.java
    │   │   └── PostStatus.java
    │   └── postcategory/
    │       └── PostCategoryAssignment.java
    ├── comment/
    │   └── Comment.java
    └── reaction/
        ├── Reaction.java
        ├── ReactionType.java
        └── TargetType.java

deuknet-application/
├── port/in/post/
│   ├── CreatePostUseCase.java
│   ├── UpdatePostUseCase.java
│   ├── PublishPostUseCase.java
│   ├── DeletePostUseCase.java
│   ├── CreateCommentUseCase.java
│   ├── UpdateCommentUseCase.java
│   ├── DeleteCommentUseCase.java
│   ├── AddReactionUseCase.java
│   └── RemoveReactionUseCase.java
├── port/out/repository/
│   ├── PostRepository.java
│   ├── CommentRepository.java
│   ├── ReactionRepository.java
│   └── PostCategoryAssignmentRepository.java
└── service/post/
    ├── CreatePostService.java
    ├── UpdatePostService.java
    ├── PublishPostService.java
    ├── DeletePostService.java
    ├── CreateCommentService.java
    ├── UpdateCommentService.java
    ├── DeleteCommentService.java
    ├── AddReactionService.java
    └── RemoveReactionService.java

deuknet-infrastructure/
└── data/command/post/
    ├── PostEntity.java
    ├── PostMapper.java
    ├── PostRepositoryAdapter.java
    ├── JpaPostRepository.java
    ├── CommentEntity.java
    ├── CommentMapper.java
    ├── CommentRepositoryAdapter.java
    ├── JpaCommentRepository.java
    ├── ReactionEntity.java
    ├── ReactionMapper.java
    ├── ReactionRepositoryAdapter.java
    ├── JpaReactionRepository.java
    ├── PostCategoryAssignmentEntity.java
    ├── PostCategoryAssignmentMapper.java
    ├── PostCategoryAssignmentRepositoryAdapter.java
    └── JpaPostCategoryAssignmentRepository.java

deuknet-presentation/
└── controller/post/
    ├── PostController.java
    ├── CommentController.java
    ├── ReactionController.java
    └── dto/
        ├── CreatePostRequest.java
        ├── UpdatePostRequest.java
        ├── CreateCommentRequest.java
        ├── UpdateCommentRequest.java
        └── AddReactionRequest.java
```

## 📡 API 엔드포인트

### Post API

#### 1. 게시글 생성
**Endpoint:** `POST /api/posts`
**Headers:** `X-User-Id: {userId}`
**Request Body:**
```json
{
  "title": "게시글 제목",
  "content": "게시글 내용",
  "categoryIds": ["uuid1", "uuid2"]
}
```
**Response:** `UUID` (생성된 게시글 ID)

#### 2. 게시글 수정
**Endpoint:** `PUT /api/posts/{postId}`
**Headers:** `X-User-Id: {userId}`
**Request Body:**
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "categoryIds": ["uuid1", "uuid2"]
}
```

#### 3. 게시글 발행
**Endpoint:** `POST /api/posts/{postId}/publish`
**Headers:** `X-User-Id: {userId}`

#### 4. 게시글 삭제
**Endpoint:** `DELETE /api/posts/{postId}`
**Headers:** `X-User-Id: {userId}`

### Comment API

#### 1. 댓글 작성
**Endpoint:** `POST /api/posts/{postId}/comments`
**Headers:** `X-User-Id: {userId}`
**Request Body:**
```json
{
  "content": "댓글 내용",
  "parentCommentId": "uuid"  // 대댓글인 경우, null이면 일반 댓글
}
```
**Response:** `UUID` (생성된 댓글 ID)

#### 2. 댓글 수정
**Endpoint:** `PUT /api/posts/{postId}/comments/{commentId}`
**Headers:** `X-User-Id: {userId}`
**Request Body:**
```json
{
  "content": "수정된 댓글 내용"
}
```

#### 3. 댓글 삭제
**Endpoint:** `DELETE /api/posts/{postId}/comments/{commentId}`
**Headers:** `X-User-Id: {userId}`

### Reaction API

#### 1. 리액션 추가
**Endpoint:** `POST /api/posts/{postId}/reactions`
**Headers:** `X-User-Id: {userId}`
**Request Body:**
```json
{
  "reactionType": "LIKE"
}
```

#### 2. 리액션 삭제
**Endpoint:** `DELETE /api/posts/{postId}/reactions/{reactionId}`
**Headers:** `X-User-Id: {userId}`

## 🗄️ 데이터베이스 스키마

### posts 테이블
- `id` (UUID, PK)
- `title` (VARCHAR(200))
- `content` (TEXT)
- `author_id` (UUID)
- `status` (VARCHAR(20)) - DRAFT, PUBLISHED, ARCHIVED, DELETED
- `view_count` (BIGINT)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### comments 테이블
- `id` (UUID, PK)
- `post_id` (UUID, FK)
- `author_id` (UUID)
- `content` (TEXT)
- `parent_comment_id` (UUID, nullable) - 대댓글인 경우
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### reactions 테이블
- `id` (UUID, PK)
- `reaction_type` (VARCHAR(20)) - LIKE, LOVE, HAHA, WOW, SAD, ANGRY
- `target_type` (VARCHAR(20)) - POST, COMMENT
- `target_id` (UUID)
- `user_id` (UUID)
- `created_at` (TIMESTAMP)

### post_category_assignments 테이블
- `id` (UUID, PK)
- `post_id` (UUID, FK)
- `category_id` (UUID, FK)

## 🔐 권한 관리

모든 엔드포인트는 `X-User-Id` 헤더를 통해 사용자 인증을 수행합니다.

- **작성자만 가능**: 게시글/댓글 수정, 삭제, 게시글 발행
- **모든 사용자 가능**: 게시글/댓글 작성, 리액션 추가
- **본인만 가능**: 자신의 리액션 삭제

## 📝 비즈니스 로직

### Post 상태 전환
```
DRAFT (초안) → PUBLISHED (발행됨) → ARCHIVED (보관됨)
                             ↓
                        DELETED (삭제됨)
```

- 초안 상태에서만 발행 가능
- 발행된 게시글은 보관 또는 삭제 가능
- 삭제는 Soft Delete (상태만 변경)

### 카테고리 할당
- 게시글은 여러 카테고리에 속할 수 있음
- 게시글 수정 시 카테고리 재할당 (기존 삭제 후 새로 생성)

### 대댓글
- `parentCommentId`가 null이면 일반 댓글
- `parentCommentId`가 있으면 대댓글

## ⚠️ 주의사항

1. **Query 부분 미구현**: 조회 기능은 별도로 구현 예정
2. **인증**: 실제 프로덕션에서는 JWT 토큰 기반 인증 필요
3. **권한 검증**: 모든 수정/삭제 작업에서 작성자 확인
4. **Soft Delete**: Post는 상태만 변경, Comment와 Reaction은 실제 삭제

## 🔄 향후 개선 사항

1. Query 기능 구현 (조회, 검색, 페이징)
2. 게시글 좋아요 수, 댓글 수 집계
3. 대댓글 depth 제한
4. 이미지 첨부 기능
5. 게시글 임시저장
6. 알림 기능 (댓글, 리액션 알림)
