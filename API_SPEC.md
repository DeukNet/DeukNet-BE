# DeukNet API 명세서

## 1. 인증 (Auth)

### 1.1 OAuth 로그인
- **POST** `/auth/login`
- **설명**: OAuth 인증 코드로 로그인 (GOOGLE, GITHUB 지원)
- **Request Body**: `{ provider: string, authorizationCode: string }`
- **Response**: `{ accessToken: string, refreshToken: string }`

### 1.2 토큰 갱신
- **POST** `/auth/refresh`
- **설명**: 리프레시 토큰으로 액세스 토큰 재발급
- **Request Body**: `{ refreshToken: string }`
- **Response**: `{ accessToken: string, refreshToken: string }`

---

## 2. 게시글 (Post)

### 2.1 게시글 작성
- **POST** `/posts`
- **설명**: 새 게시글 작성 (초안 상태)
- **인증**: 필요
- **Response**: `postId (UUID)`

### 2.2 게시글 수정
- **PUT** `/posts/{postId}`
- **설명**: 게시글 수정 (작성자만 가능)
- **인증**: 필요

### 2.3 게시글 발행
- **POST** `/posts/{postId}/publish`
- **설명**: 초안을 발행 상태로 변경
- **인증**: 필요

### 2.4 게시글 삭제
- **DELETE** `/posts/{postId}`
- **설명**: 게시글 삭제 (Soft Delete)
- **인증**: 필요

### 2.5 조회수 증가
- **POST** `/posts/{postId}/view`
- **설명**: 게시글 조회수 1 증가
- **인증**: 필요

### 2.6 게시글 ID로 조회
- **GET** `/posts/{id}`
- **설명**: 특정 게시글 조회
- **인증**: 불필요
- **Response**: PostSearchResponse

### 2.7 게시글 검색
- **GET** `/posts?keyword=&authorId=&categoryId=&status=&page=0&size=20&sortBy=createdAt&sortOrder=desc`
- **설명**: 여러 조건으로 게시글 검색 (모든 필터 AND 결합)
- **인증**: 불필요
- **Query Parameters**:
  - `keyword` (optional): 검색 키워드 (제목 + 내용)
  - `authorId` (optional): 작성자 ID
  - `categoryId` (optional): 카테고리 ID
  - `status` (optional): 게시글 상태 (DRAFT, PUBLISHED, DELETED)
  - `page` (default: 0): 페이지 번호
  - `size` (default: 20, max: 100): 페이지 크기
  - `sortBy` (default: createdAt): 정렬 기준
  - `sortOrder` (default: desc): 정렬 순서 (asc, desc)
- **Response**: List<PostSearchResponse>

### 2.8 인기 게시글 조회
- **GET** `/posts/popular?page=0&size=20`
- **설명**: 조회수 기준 인기 게시글 조회
- **인증**: 불필요
- **Response**: List<PostSearchResponse>

---

## 3. 댓글 (Comment)

### 3.1 댓글 작성
- **POST** `/posts/{postId}/comments`
- **설명**: 댓글 작성 (parentCommentId 지정 시 대댓글)
- **인증**: 필요
- **Request Body**: `{ content: string, parentCommentId?: UUID }`
- **Response**: `commentId (UUID)`

### 3.2 댓글 수정
- **PUT** `/posts/{postId}/comments/{commentId}`
- **설명**: 댓글 수정 (작성자만 가능)
- **인증**: 필요
- **Request Body**: `{ content: string }`

### 3.3 댓글 삭제
- **DELETE** `/posts/{postId}/comments/{commentId}`
- **설명**: 댓글 삭제 (작성자만 가능)
- **인증**: 필요

---

## 4. 리액션 (Reaction)

### 4.1 리액션 추가
- **POST** `/posts/{postId}/reactions`
- **설명**: 게시글에 리액션 추가
- **인증**: 필요
- **Request Body**: `{ type: "LIKE" | "LOVE" | "HAHA" | "WOW" | "SAD" | "ANGRY" }`
- **Response**: `reactionId (UUID)`

### 4.2 리액션 삭제
- **DELETE** `/reactions/{reactionId}`
- **설명**: 자신의 리액션 삭제
- **인증**: 필요

---

## 5. 카테고리 (Category)

### 5.1 카테고리 생성
- **POST** `/categories`
- **설명**: 새 카테고리 생성 (parentCategoryId로 계층 구조)
- **인증**: 필요
- **Request Body**: `{ name: string, parentCategoryId?: UUID }`
- **Response**: `categoryId (UUID)`

### 5.2 카테고리 수정
- **PUT** `/categories/{categoryId}`
- **설명**: 카테고리명 수정
- **인증**: 필요
- **Request Body**: `{ name: string }`

### 5.3 카테고리 삭제
- **DELETE** `/categories/{categoryId}`
- **설명**: 카테고리 삭제 (하위 카테고리 없어야 함)
- **인증**: 필요

---

## 공통 응답 코드

- **200**: 성공
- **201**: 생성 성공
- **204**: 성공 (응답 본문 없음)
- **400**: 잘못된 요청
- **401**: 인증 실패
- **403**: 권한 없음
- **404**: 리소스를 찾을 수 없음
