# Aggregate 기반 패키지 구조 개선

## 📋 변경 사항

Infrastructure Layer의 data/command 패키지를 Aggregate 단위로 재구성했습니다.

### Before (모든 파일이 post에 혼재)
```
deuknet-infrastructure/
└── data/command/
    └── post/
        ├── PostEntity.java
        ├── CommentEntity.java              ❌ Post가 아닌데 post에 있음
        ├── ReactionEntity.java             ❌ Post가 아닌데 post에 있음
        ├── PostCategoryAssignmentEntity.java
        ├── (Mappers, Repositories...)
```

### After (Aggregate 단위로 분리)
```
deuknet-infrastructure/
└── data/command/
    ├── auth/                               ✅ Auth Aggregate
    │   ├── AuthCredentialEntity.java
    │   ├── AuthCredentialMapper.java
    │   ├── AuthCredentialRepositoryAdapter.java
    │   └── JpaAuthCredentialRepository.java
    │
    ├── user/                               ✅ User Aggregate
    │   ├── UserEntity.java
    │   ├── UserMapper.java
    │   ├── UserRepositoryAdapter.java
    │   └── JpaUserRepository.java
    │
    ├── category/                           ✅ Category Aggregate
    │   ├── CategoryEntity.java
    │   └── CategoryMapper.java
    │
    ├── post/                               ✅ Post Aggregate
    │   ├── PostEntity.java
    │   ├── PostMapper.java
    │   ├── PostRepositoryAdapter.java
    │   ├── JpaPostRepository.java
    │   ├── PostCategoryAssignmentEntity.java
    │   ├── PostCategoryAssignmentMapper.java
    │   ├── PostCategoryAssignmentRepositoryAdapter.java
    │   └── JpaPostCategoryAssignmentRepository.java
    │
    ├── comment/                            ✅ Comment Aggregate
    │   ├── CommentEntity.java
    │   ├── CommentMapper.java
    │   ├── CommentRepositoryAdapter.java
    │   └── JpaCommentRepository.java
    │
    └── reaction/                           ✅ Reaction Aggregate
        ├── ReactionEntity.java
        ├── ReactionMapper.java
        ├── ReactionRepositoryAdapter.java
        └── JpaReactionRepository.java
```

## 🎯 개선 효과

### 1. 명확한 경계 (Clear Boundaries)
- 각 Aggregate의 책임이 명확하게 분리됨
- 패키지만 보고도 어떤 도메인인지 즉시 파악 가능

### 2. 응집도 향상 (High Cohesion)
- 관련된 Entity, Mapper, Repository가 같은 패키지에 위치
- 변경 시 해당 Aggregate 패키지만 수정하면 됨

### 3. 결합도 감소 (Low Coupling)
- Aggregate 간 의존성이 명확하게 드러남
- 다른 Aggregate의 내부 구현에 영향받지 않음

### 4. 유지보수성 향상 (Better Maintainability)
- 새로운 Aggregate 추가 시 독립적인 패키지로 추가
- 특정 Aggregate 삭제 시 해당 패키지만 제거

## 📊 Aggregate 정의

### Auth Aggregate
- **Root Entity**: AuthCredential
- **책임**: 사용자 인증 정보 관리
- **범위**: OAuth 제공자별 인증 정보

### User Aggregate  
- **Root Entity**: User
- **책임**: 사용자 프로필 관리
- **범위**: 사용자 기본 정보, 프로필

### Category Aggregate
- **Root Entity**: Category
- **책임**: 카테고리 계층 구조 관리
- **범위**: 카테고리 생성, 수정, 계층 관계

### Post Aggregate
- **Root Entity**: Post
- **포함**: PostCategoryAssignment
- **책임**: 게시글 및 카테고리 할당 관리
- **범위**: 게시글 생명주기, 카테고리 연결

### Comment Aggregate
- **Root Entity**: Comment
- **책임**: 댓글 및 대댓글 관리
- **범위**: 댓글 생명주기, 계층 구조

### Reaction Aggregate
- **Root Entity**: Reaction
- **책임**: 리액션(좋아요 등) 관리
- **범위**: 게시글/댓글에 대한 반응

## 🔄 Aggregate 간 관계

```
User ←────── Auth
  ↓
Post ←────── Category
  ↓
Comment
  ↓
Reaction
```

- User는 Auth를 통해 인증
- Category는 Post에 할당됨
- User가 Post 작성
- User가 Comment 작성  
- User가 Reaction 추가
- Post/Comment가 Reaction 대상

## 📝 DDD 원칙 준수

### Aggregate 설계 원칙
1. ✅ **작은 Aggregate**: 각 Aggregate는 단일 책임
2. ✅ **ID로 참조**: Aggregate 간에는 Entity 객체가 아닌 ID로 참조
3. ✅ **일관성 경계**: 각 Aggregate는 독립적인 트랜잭션 경계
4. ✅ **명확한 루트**: 각 Aggregate는 명확한 Root Entity 보유

### 패키지 네이밍 규칙
- `data/command/{aggregate}`: Command 모델 (Write)
- `data/query/{aggregate}`: Query 모델 (Read) - 추후 구현
- 각 Aggregate 패키지는 자체 포함 (Self-contained)

## 🚀 빌드 결과
- ✅ BUILD SUCCESSFUL in 21s
- ✅ 21 tasks executed  
- ✅ 모든 모듈 컴파일 성공
- ✅ 패키지 구조 변경 반영 완료

## 💡 추후 작업

### Query Model 분리
```
deuknet-infrastructure/
└── data/
    ├── command/     (Write Model - 현재 구현됨)
    │   ├── auth/
    │   ├── user/
    │   ├── post/
    │   ├── comment/
    │   └── reaction/
    ```

CQRS 패턴 적용을 위해 Query용 별도 모델 구성 예정
