# OAuth 인증 기능

## 📋 개요
DeukNet 프로젝트에 OAuth 2.0 기반 소셜 로그인 및 JWT 토큰 기반 인증 시스템이 구현되었습니다.

## 🚀 주요 기능

### 1. OAuth 로그인
- **Google OAuth 2.0** 지원
- **GitHub OAuth 2.0** 지원
- 자동 회원가입 (첫 로그인 시)
- 고유한 username 자동 생성

### 2. 토큰 리프레시
- Access Token 재발급
- Refresh Token 재발급

## 📦 패키지 구조

```
deuknet-domain/
├── model/command/auth/
│   ├── AuthCredential.java      # 인증 정보 도메인 모델
│   ├── AuthProvider.java        # OAuth 제공자 Enum
│   ├── OAuthUserInfo.java       # OAuth 사용자 정보
│   └── TokenPair.java            # JWT 토큰 쌍
└── model/command/user/
    └── User.java                 # 사용자 도메인 모델

deuknet-application/
├── port/in/auth/
│   ├── OAuthLoginUseCase.java    # OAuth 로그인 UseCase
│   └── RefreshTokenUseCase.java  # 토큰 리프레시 UseCase
├── port/out/external/
│   └── OAuthPort.java            # OAuth 외부 연동 포트
├── port/out/repository/
│   ├── AuthCredentialRepository.java
│   └── UserRepository.java
├── port/out/security/
│   └── JwtPort.java              # JWT 생성/검증 포트
└── service/auth/
    ├── OAuthLoginService.java    # OAuth 로그인 서비스
    └── RefreshTokenService.java  # 토큰 리프레시 서비스

deuknet-infrastructure/
├── persistence/
│   ├── adapter/
│   │   ├── AuthCredentialRepositoryAdapter.java
│   │   └── UserRepositoryAdapter.java
│   └── repository/
│       ├── JpaAuthCredentialRepository.java
│       └── JpaUserRepository.java
├── security/
│   └── JwtAdapter.java           # JWT 구현
├── external/oauth/
│   ├── OAuthAdapter.java         # OAuth 어댑터
│   ├── OAuthClient.java          # OAuth 클라이언트 인터페이스
│   ├── GoogleOAuthClient.java    # Google OAuth 구현
│   └── GithubOAuthClient.java    # GitHub OAuth 구현
└── config/
    └── RestTemplateConfig.java   # RestTemplate 설정

deuknet-presentation/
└── controller/auth/
    ├── AuthController.java       # 인증 API 컨트롤러
    └── dto/
        ├── OAuthLoginRequest.java
        ├── RefreshTokenRequest.java
        └── TokenResponse.java
```

## 🔧 설정

### application.yaml

```yaml
# JWT Configuration
jwt:
  secret: your-secret-key-here-please-change-this-to-a-secure-random-string-at-least-256-bits
  access-token-validity-ms: 3600000  # 1 hour
  refresh-token-validity-ms: 604800000  # 7 days

# OAuth Configuration
oauth:
  google:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    redirect-uri: http://localhost:8080/api/auth/oauth/callback/google
  github:
    client-id: ${GITHUB_CLIENT_ID}
    client-secret: ${GITHUB_CLIENT_SECRET}
    redirect-uri: http://localhost:8080/api/auth/oauth/callback/github
```

### 환경 변수 설정

```bash
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret
export GITHUB_CLIENT_ID=your-github-client-id
export GITHUB_CLIENT_SECRET=your-github-client-secret
```

## 📡 API 엔드포인트

### 1. OAuth 로그인

**Endpoint:** `POST /api/auth/oauth/login`

**Request Body:**
```json
{
  "code": "authorization-code-from-oauth-provider",
  "provider": "GOOGLE"  // or "GITHUB"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 2. 토큰 리프레시

**Endpoint:** `POST /api/auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## 🔐 OAuth 플로우

### Google OAuth
1. 프론트엔드에서 Google 로그인 페이지로 리다이렉트
2. 사용자가 Google 계정으로 로그인
3. Google이 authorization code를 redirect_uri로 전달
4. 프론트엔드가 authorization code를 백엔드로 전송
5. 백엔드가 Google에서 access token 획득
6. 백엔드가 Google에서 사용자 정보 조회
7. 백엔드가 JWT 토큰 생성 및 반환

### GitHub OAuth
Google과 동일한 플로우, GitHub API 사용

## 🗄️ 데이터베이스 스키마

### auth_credentials 테이블
- `id` (UUID, PK)
- `user_id` (UUID)
- `auth_provider` (VARCHAR) - GOOGLE, GITHUB, LOCAL
- `email` (VARCHAR, UNIQUE)

### users 테이블
- `id` (UUID, PK)
- `auth_credential_id` (UUID, FK)
- `username` (VARCHAR, UNIQUE)
- `display_name` (VARCHAR)
- `bio` (TEXT)
- `avatar_url` (VARCHAR)

## 🛠️ 의존성

```gradle
// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

// Spring Web (for RestTemplate)
implementation 'org.springframework.boot:spring-boot-starter-web'
```

## 🧪 테스트 방법

### 1. Google OAuth 설정
1. [Google Cloud Console](https://console.cloud.google.com/)에서 프로젝트 생성
2. OAuth 2.0 클라이언트 ID 생성
3. 승인된 리디렉션 URI 추가: `http://localhost:8080/api/auth/oauth/callback/google`
4. Client ID와 Client Secret을 환경 변수로 설정

### 2. GitHub OAuth 설정
1. GitHub Settings > Developer settings > OAuth Apps
2. New OAuth App 생성
3. Authorization callback URL: `http://localhost:8080/api/auth/oauth/callback/github`
4. Client ID와 Client Secret을 환경 변수로 설정

### 3. Postman으로 테스트
1. 브라우저에서 OAuth 로그인 진행
2. Authorization code 획득
3. `/api/auth/oauth/login` 엔드포인트 호출
4. 받은 토큰으로 `/api/auth/refresh` 테스트

## 📝 주요 특징

1. **헥사고날 아키텍처**: 포트와 어댑터 패턴 적용
2. **도메인 중심 설계**: 비즈니스 로직은 도메인 계층에 집중
3. **확장 가능**: 새로운 OAuth Provider 추가 용이
4. **보안**: JWT 기반 stateless 인증
5. **자동 회원가입**: OAuth 로그인 시 자동으로 사용자 생성

## ⚠️ 주의사항

1. **JWT Secret**: 프로덕션에서는 반드시 안전한 Secret Key 사용
2. **HTTPS**: 프로덕션 환경에서는 반드시 HTTPS 사용
3. **토큰 만료**: Access Token은 짧게, Refresh Token은 길게 설정
4. **환경 변수**: OAuth 클라이언트 정보는 환경 변수로 관리
5. **에러 처리**: 프로덕션에서는 더 세밀한 에러 처리 필요

## 🔄 향후 개선 사항

1. 토큰 블랙리스트 (로그아웃 구현)
2. 소셜 로그인 연동 해제 기능
3. 추가 OAuth Provider (Kakao, Naver 등)
4. Rate Limiting
5. 로그인 이력 추적
