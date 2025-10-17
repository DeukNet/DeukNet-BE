# DeukNet Helm Chart

이 Helm 차트는 DeukNet Spring Boot 애플리케이션과 ELK 스택을 Kubernetes에 배포합니다.

## 구성 요소

- **Application**: Spring Boot 애플리케이션 (myapp)
- **PostgreSQL**: 데이터베이스
- **Elasticsearch**: 로그 저장소
- **Logstash**: 로그 처리 파이프라인
- **Kibana**: 로그 시각화 대시보드
- **Filebeat**: 로그 수집기 (DaemonSet)

## 사전 요구 사항

- Kubernetes 1.19+
- Helm 3.0+
- kubectl 설정 완료

## 설치

### 환경별 설치

DeukNet은 dev, staging, prod 3가지 환경을 지원합니다.

#### Development 환경

```bash
helm install deuknet ./helm/deuknet -f ./helm/deuknet/values-dev.yaml -n deuknet-dev --create-namespace
```

#### Staging 환경

```bash
helm install deuknet ./helm/deuknet -f ./helm/deuknet/values-staging.yaml -n deuknet-staging --create-namespace
```

#### Production 환경

**⚠️ 주의: Production 배포 전 반드시 비밀번호를 변경하세요!**

```bash
# values-prod.yaml 파일에서 비밀번호를 실제 값으로 변경한 후:
helm install deuknet ./helm/deuknet -f ./helm/deuknet/values-prod.yaml -n deuknet-prod --create-namespace
```

### 기본 설치

```bash
helm install deuknet ./helm/deuknet
```

### 커스텀 값으로 설치

```bash
helm install deuknet ./helm/deuknet -f my-values.yaml
```

## 업그레이드

### 환경별 업그레이드

```bash
# Development
helm upgrade deuknet ./helm/deuknet -f ./helm/deuknet/values-dev.yaml -n deuknet-dev

# Staging
helm upgrade deuknet ./helm/deuknet -f ./helm/deuknet/values-staging.yaml -n deuknet-staging

# Production
helm upgrade deuknet ./helm/deuknet -f ./helm/deuknet/values-prod.yaml -n deuknet-prod
```

### 기본 업그레이드

```bash
helm upgrade deuknet ./helm/deuknet
```

## 삭제

```bash
helm uninstall deuknet
```

## 설정

주요 설정 가능한 파라미터들은 `values.yaml` 파일에 정의되어 있습니다.

### 애플리케이션 설정

| 파라미터 | 설명 | 기본값 |
|---------|------|--------|
| `app.replicaCount` | 애플리케이션 복제본 수 | `1` |
| `app.image.repository` | 이미지 저장소 | `myapp` |
| `app.image.tag` | 이미지 태그 | `local` |
| `app.service.type` | 서비스 타입 | `NodePort` |
| `app.service.nodePort` | NodePort 번호 | `30080` |

### PostgreSQL 설정

| 파라미터 | 설명 | 기본값 |
|---------|------|--------|
| `postgres.enabled` | PostgreSQL 활성화 | `true` |
| `postgres.env.database` | 데이터베이스 이름 | `app_db` |
| `postgres.env.username` | 사용자 이름 | `app_user` |
| `postgres.env.password` | 비밀번호 | `app_pass` |

### Elasticsearch 설정

| 파라미터 | 설명 | 기본값 |
|---------|------|--------|
| `elasticsearch.enabled` | Elasticsearch 활성화 | `true` |
| `elasticsearch.env.elasticPassword` | Elastic 비밀번호 | `changeme` |

### 환경별 비밀번호 설정

각 환경별 values 파일에는 다음 비밀번호들이 설정되어 있습니다:

- **Development** (`values-dev.yaml`):
  - PostgreSQL: `dev_postgres_password_2024`
  - Elasticsearch: `dev_elastic_password_2024`

- **Staging** (`values-staging.yaml`):
  - PostgreSQL: `staging_postgres_password_2024`
  - Elasticsearch: `staging_elastic_password_2024`

- **Production** (`values-prod.yaml`):
  - PostgreSQL: `CHANGE_ME_PROD_POSTGRES_PASSWORD` ⚠️ **반드시 변경 필요**
  - Elasticsearch: `CHANGE_ME_PROD_ELASTIC_PASSWORD` ⚠️ **반드시 변경 필요**

**⚠️ 보안 권장사항:**
- Production 환경에서는 반드시 강력한 비밀번호로 변경하세요
- Kubernetes Secrets을 사용하여 비밀번호를 관리하는 것을 권장합니다
- values 파일을 Git에 커밋하기 전에 민감한 정보를 확인하세요

### ELK 스택 비활성화

특정 컴포넌트를 비활성화하려면:

```bash
helm install deuknet ./helm/deuknet \
  --set elasticsearch.enabled=false \
  --set logstash.enabled=false \
  --set kibana.enabled=false \
  --set filebeat.enabled=false
```

## 접근 방법

### 애플리케이션

Minikube 환경:
```bash
minikube service myapp-service --url
```

일반 클러스터 (NodePort):
```bash
# 노드 IP 확인
kubectl get nodes -o wide

# 접근: http://<NODE-IP>:30080
```

### Kibana

Port forwarding 사용:
```bash
kubectl port-forward svc/kibana 5601:5601
# 접근: http://localhost:5601
```

Minikube 환경:
```bash
minikube service kibana --url
```

## 문제 해결

### Pod 상태 확인

```bash
kubectl get pods
kubectl describe pod <pod-name>
kubectl logs <pod-name>
```

### Helm 릴리스 상태 확인

```bash
helm status deuknet
helm get values deuknet
helm get manifest deuknet
```

### 로그 확인

```bash
# 애플리케이션 로그
kubectl logs -f deployment/myapp

# Filebeat 로그
kubectl logs -f daemonset/filebeat

# Elasticsearch 로그
kubectl logs -f deployment/elasticsearch
```

## 라이선스

DeukNet Team
