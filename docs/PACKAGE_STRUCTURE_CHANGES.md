# 패키지 구조 변경 완료

## 변경 사항

### ✅ 기존 구조 (Before)
```
deuknet-infrastructure/
└── persistence/
    ├── adapter/
    │   ├── AuthCredentialRepositoryAdapter.java
    │   └── UserRepositoryAdapter.java
    ├── entity/
    │   ├── AuthCredentialEntity.java
    │   └── UserEntity.java
    ├── mapper/
    │   ├── AuthCredentialMapper.java
    │   └── UserMapper.java
    └── repository/
        ├── JpaAuthCredentialRepository.java
        └── JpaUserRepository.java
```

### ✅ 새로운 구조 (After)
```
deuknet-infrastructure/
└── data/
    └── command/
        ├── auth/
        │   ├── AuthCredentialEntity.java
        │   ├── AuthCredentialMapper.java
        │   ├── AuthCredentialRepositoryAdapter.java
        │   └── JpaAuthCredentialRepository.java
        └── user/
            ├── UserEntity.java
            ├── UserMapper.java
            ├── UserRepositoryAdapter.java
            └── JpaUserRepository.java
```

## 변경된 패키지 경로

### Auth 관련
- **Entity**: `org.example.deuknetinfrastructure.data.command.auth.AuthCredentialEntity`
- **Mapper**: `org.example.deuknetinfrastructure.data.command.auth.AuthCredentialMapper`
- **Repository**: `org.example.deuknetinfrastructure.data.command.auth.JpaAuthCredentialRepository`
- **Adapter**: `org.example.deuknetinfrastructure.data.command.auth.AuthCredentialRepositoryAdapter`

### User 관련
- **Entity**: `org.example.deuknetinfrastructure.data.command.user.UserEntity`
- **Mapper**: `org.example.deuknetinfrastructure.data.command.user.UserMapper`
- **Repository**: `org.example.deuknetinfrastructure.data.command.user.JpaUserRepository`
- **Adapter**: `org.example.deuknetinfrastructure.data.command.user.UserRepositoryAdapter`

## 주요 개선사항

1. **기존 프로젝트 구조 준수**: `data/command` 패턴 사용
2. **도메인별 패키지 분리**: auth, user로 명확히 구분
3. **응집도 향상**: 관련된 모든 클래스가 같은 패키지에 위치
4. **Mapper Bean 등록**: `@Component` 어노테이션 추가로 Spring Bean으로 등록

## 이점

- 🎯 **명확한 구조**: 도메인별로 패키지가 분리되어 파일 찾기 쉬움
- 📦 **높은 응집도**: Entity, Mapper, Repository, Adapter가 모두 한 곳에
- 🔄 **일관성**: 기존 프로젝트의 `data/command/post` 구조와 동일
- 🚀 **확장성**: 새로운 도메인 추가 시 동일한 패턴 적용 가능
