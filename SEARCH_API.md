# Search API 가이드

통합 검색 API를 사용하면 모든 필터를 AND 조건으로 조합하여 검색할 수 있습니다.

## 🎯 핵심 개념

### 통합 검색 엔드포인트

기존에 흩어진 여러 엔드포인트를 **단일 엔드포인트**로 통합하여 사용자 편의성을 높였습니다.

**장점:**
- 모든 필터를 자유롭게 조합 가능 (AND 조건)
- URL 하나로 복잡한 검색 가능
- 일관된 API 구조

## 📋 Post 검색 API

### 기본 엔드포인트

```
GET /api/search/posts
```

### 파라미터

| 파라미터 | 타입 | 설명 | 기본값 |
|---------|------|------|--------|
| `keyword` | String | 제목/내용 검색어 | optional |
| `authorId` | UUID | 작성자 필터 | optional |
| `categoryId` | UUID | 카테고리 필터 | optional |
| `status` | String | 상태 필터 (DRAFT, PUBLISHED, DELETED) | optional |
| `page` | int | 페이지 번호 | 0 |
| `size` | int | 페이지 크기 (max: 100) | 20 |
| `sortField` | String | 정렬 필드 (createdAt, viewCount, likeCount, commentCount) | createdAt |
| `sortOrder` | String | 정렬 순서 (asc, desc) | desc |

### 사용 예시

#### 1. 키워드 검색
```bash
GET /api/search/posts?keyword=Spring

# 결과: 제목 또는 내용에 "Spring"이 포함된 게시글
```

#### 2. 특정 작성자의 게시글
```bash
GET /api/search/posts?authorId=123e4567-e89b-12d3-a456-426614174000

# 결과: 해당 작성자의 모든 게시글
```

#### 3. 카테고리 필터링
```bash
GET /api/search/posts?categoryId=987fcdeb-51a2-43d7-9876-543210fedcba

# 결과: 해당 카테고리의 모든 게시글
```

#### 4. 복합 필터 (AND 조합)
```bash
GET /api/search/posts?keyword=Java&categoryId=...&status=PUBLISHED&sortField=likeCount&sortOrder=desc

# 결과:
# - 제목/내용에 "Java" 포함
# - AND 특정 카테고리
# - AND 게시 상태
# - 좋아요 수 내림차순 정렬
```

#### 5. 인기 게시글 (조회수 기준)
```bash
GET /api/search/posts?status=PUBLISHED&sortField=viewCount&sortOrder=desc

# 또는 shortcut 사용:
GET /api/search/posts/trending
```

#### 6. 최신 게시글
```bash
GET /api/search/posts?status=PUBLISHED&sortField=createdAt&sortOrder=desc

# 또는 shortcut 사용:
GET /api/search/posts/recent
```

#### 7. 좋아요 많은 게시글
```bash
GET /api/search/posts?status=PUBLISHED&sortField=likeCount&sortOrder=desc

# 또는 shortcut 사용:
GET /api/search/posts/popular
```

### Shortcut 엔드포인트

자주 사용하는 검색을 위한 shortcut:

```bash
GET /api/search/posts/popular   # 좋아요 많은 순
GET /api/search/posts/recent    # 최신 순
GET /api/search/posts/trending  # 조회수 많은 순
```

## 👥 User 검색 API

### 기본 엔드포인트

```
GET /api/search/users
```

### 파라미터

| 파라미터 | 타입 | 설명 | 기본값 |
|---------|------|------|--------|
| `keyword` | String | displayName/username/bio 검색어 | optional |
| `minPostCount` | Long | 최소 게시글 수 필터 | optional |
| `minFollowerCount` | Long | 최소 팔로워 수 필터 | optional |
| `page` | int | 페이지 번호 | 0 |
| `size` | int | 페이지 크기 (max: 100) | 20 |
| `sortField` | String | 정렬 필드 (createdAt, postCount, followerCount) | followerCount |
| `sortOrder` | String | 정렬 순서 (asc, desc) | desc |

### 사용 예시

#### 1. 키워드 검색
```bash
GET /api/search/users?keyword=John

# 결과: displayName, username, bio에 "John"이 포함된 사용자
```

#### 2. 활동적인 사용자 (게시글 많은 순)
```bash
GET /api/search/users?minPostCount=50&sortField=postCount&sortOrder=desc

# 또는 shortcut 사용:
GET /api/search/users/active
```

#### 3. 인기 사용자 (팔로워 많은 순)
```bash
GET /api/search/users?minFollowerCount=1000&sortField=followerCount&sortOrder=desc

# 또는 shortcut 사용:
GET /api/search/users/popular
```

#### 4. 복합 필터 (AND 조합)
```bash
GET /api/search/users?keyword=developer&minPostCount=10&minFollowerCount=100&sortField=followerCount

# 결과:
# - "developer" 키워드 포함
# - AND 게시글 10개 이상
# - AND 팔로워 100명 이상
# - 팔로워 수 내림차순 정렬
```

#### 5. 최근 가입 사용자
```bash
GET /api/search/users?sortField=createdAt&sortOrder=desc

# 또는 shortcut 사용:
GET /api/search/users/recent
```

### Shortcut 엔드포인트

```bash
GET /api/search/users/active   # 활동적인 사용자 (게시글 수 기준)
GET /api/search/users/popular  # 인기 사용자 (팔로워 수 기준)
GET /api/search/users/recent   # 최근 가입 사용자
```

## 🔍 ID 기반 조회

### Post 조회
```bash
GET /api/search/posts/{postId}

# 응답: PostDetailSearchResponse (단일 객체)
```

### User 조회
```bash
GET /api/search/users/{userId}

# 응답: UserSearchResponse (단일 객체)
```

### Username으로 User 조회
```bash
GET /api/search/users/by-username/{username}

# 응답: UserSearchResponse (단일 객체)
```

## 📊 응답 형식

### Post 검색 응답

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "title": "Spring Boot Tutorial",
    "content": "Learn Spring Boot...",
    "authorId": "987fcdeb-51a2-43d7-9876-543210fedcba",
    "authorUsername": "john_doe",
    "authorDisplayName": "John Doe",
    "authorAvatarUrl": "https://...",
    "status": "PUBLISHED",
    "viewCount": 1234,
    "commentCount": 56,
    "likeCount": 789,
    "categoryIds": ["..."],
    "categoryNames": ["Backend", "Java"],
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-20T14:45:00"
  }
]
```

### User 검색 응답

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "john_doe",
    "displayName": "John Doe",
    "bio": "Backend developer",
    "avatarUrl": "https://...",
    "postCount": 42,
    "commentCount": 156,
    "followerCount": 1234,
    "followingCount": 567,
    "createdAt": "2024-01-01T00:00:00"
  }
]
```

## 🎨 프론트엔드 통합 예시

### React 예시

```typescript
// 통합 검색 Hook
const usePostSearch = () => {
  const searchPosts = async (filters: {
    keyword?: string;
    authorId?: string;
    categoryId?: string;
    status?: string;
    page?: number;
    size?: number;
    sortField?: string;
    sortOrder?: 'asc' | 'desc';
  }) => {
    const params = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params.append(key, String(value));
      }
    });

    const response = await fetch(`/api/search/posts?${params}`);
    return response.json();
  };

  return { searchPosts };
};

// 사용 예시
const { searchPosts } = usePostSearch();

// 키워드만
const posts1 = await searchPosts({ keyword: 'Spring' });

// 복합 필터
const posts2 = await searchPosts({
  keyword: 'Java',
  categoryId: '...',
  status: 'PUBLISHED',
  sortField: 'likeCount',
  sortOrder: 'desc'
});

// 인기 게시글 (shortcut)
const popularPosts = await fetch('/api/search/posts/popular').then(r => r.json());
```

### Vue 예시

```typescript
// Composable
export const useSearch = () => {
  const searchPosts = async (filters: SearchFilters) => {
    const params = new URLSearchParams();

    for (const [key, value] of Object.entries(filters)) {
      if (value != null) {
        params.append(key, String(value));
      }
    }

    const { data } = await useFetch(`/api/search/posts?${params}`);
    return data.value;
  };

  return { searchPosts };
};

// 사용
const { searchPosts } = useSearch();

const posts = await searchPosts({
  keyword: 'Spring',
  status: 'PUBLISHED',
  sortField: 'createdAt',
  sortOrder: 'desc'
});
```

## 🔗 실제 사용 시나리오

### 1. 메인 페이지 - 인기 게시글
```bash
GET /api/search/posts/popular?page=0&size=10
```

### 2. 카테고리 페이지
```bash
GET /api/search/posts?categoryId={categoryId}&status=PUBLISHED&sortField=createdAt&sortOrder=desc
```

### 3. 사용자 프로필 - 작성 글 목록
```bash
GET /api/search/posts?authorId={userId}&status=PUBLISHED&sortField=createdAt&sortOrder=desc
```

### 4. 검색 페이지
```bash
GET /api/search/posts?keyword={query}&status=PUBLISHED&page=0&size=20
```

### 5. 고급 검색 (여러 필터 조합)
```bash
GET /api/search/posts?keyword=Spring&categoryId={id}&status=PUBLISHED&sortField=likeCount&sortOrder=desc
```

### 6. 추천 사용자 (팔로워 많은 순)
```bash
GET /api/search/users/popular?page=0&size=5
```

### 7. 활동적인 사용자 찾기
```bash
GET /api/search/users?minPostCount=10&minFollowerCount=100&sortField=postCount&sortOrder=desc
```

## 📈 성능 최적화 팁

1. **페이지 크기 제한**: `size`는 최대 100으로 제한됩니다
2. **필요한 필터만 사용**: 불필요한 파라미터는 생략하세요
3. **Shortcut 활용**: 자주 사용하는 검색은 shortcut 엔드포인트 사용
4. **캐싱**: 인기 게시글, 최신 게시글 등은 프론트엔드에서 캐싱 권장

## 🔧 문제 해결

### Q: 필터를 여러 개 조합했는데 결과가 없습니다
A: 모든 필터는 AND 조건입니다. 필터를 하나씩 제거하면서 테스트해보세요.

### Q: keyword 검색이 정확하지 않습니다
A: Elasticsearch 전문 검색을 사용합니다. 완전 일치가 아닌 부분 일치/유사도 기반 검색입니다.

### Q: sortField에 잘못된 값을 넣으면?
A: 기본값(createdAt/followerCount)으로 자동 설정됩니다.

### Q: 100개 이상의 결과를 가져올 수 있나요?
A: 한 번에 최대 100개까지만 가능합니다. 페이징을 사용하세요.

## 🎯 마이그레이션 가이드

기존 API에서 통합 API로 마이그레이션:

```bash
# Before
GET /api/search/posts/by-author/{authorId}
GET /api/search/posts/by-category/{categoryId}
GET /api/search/posts/by-status/{status}

# After - 모두 통합
GET /api/search/posts?authorId={id}&categoryId={id}&status={status}
```

**주의**: 기존 엔드포인트도 호환성을 위해 유지되지만, 새로운 통합 API 사용을 권장합니다.
