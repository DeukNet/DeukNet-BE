# DeukNet CDC Helm Chart

CQRS + Event Sourcing + CDC (Change Data Capture) 파이프라인을 위한 완전한 Helm Chart입니다.

## 🏗️ 아키텍처

```
┌─────────────────────┐
│  DeukNet Application│
│   (Spring Boot)     │
└──────────┬──────────┘
           │ Write (Outbox Pattern)
           ↓
┌─────────────────────┐
│    PostgreSQL       │
│  (outbox_events)    │
└──────────┬──────────┘
           │ CDC (Debezium)
           ↓
┌─────────────────────┐
│   Kafka (KRaft)     │
│  outbox.events.*    │
└──────────┬──────────┘
           │ Elasticsearch Sink
           ↓
┌─────────────────────┐
│   Elasticsearch     │
│   (Read Model)      │
└──────────┬──────────┘
           │ Search Query
           ↑
┌─────────────────────┐
│  DeukNet Application│
│   (Search API)      │
└─────────────────────┘
```

## 📦 포함된 컴포넌트

### 핵심 인프라
- **PostgreSQL 15** (WAL 활성화, Debezium Ready)
- **Kafka 3.8.1** (KRaft 모드 - Zookeeper 불필요!)
- **Kafka Connect 2.5** (Debezium + Elasticsearch Sink)
- **Elasticsearch 8.11**

### 애플리케이션
- **DeukNet Spring Boot Application**

### 자동화
- **Connector Registration Job**: Debezium Source + Elasticsearch Sink 자동 등록

## 🎯 주요 기능

### 1. **Outbox Pattern + Event Router**
- Outbox 테이블의 `payload` 필드를 읽어 Kafka로 전송
- Event Router가 `aggregate_type`에 따라 자동으로 토픽 라우팅
- 예: `aggregate_type=PostDetail` → 토픽: `outbox.events.PostDetail`

### 2. **Payload 기반 Elasticsearch 동기화**
- Kafka Connect의 `ExtractField` Transform으로 `payload` 추출
- JSON payload를 그대로 Elasticsearch에 저장
- Schema 없이 동적 매핑

### 3. **KRaft Mode Kafka**
- Zookeeper 없이 경량화된 Kafka 배포
- 빠른 시작 시간과 적은 리소스 사용

## 🚀 빠른 시작

### 전제 조건
- Minikube 실행 중
- kubectl 설치
- Helm 3.x 설치

### 1단계: 이미지 빌드

```bash
# Minikube Docker 환경 사용
eval $(minikube docker-env)

# 애플리케이션 빌드
./gradlew clean build -x test

# Docker 이미지 빌드
docker build -t deuknet-app:latest .
```

### 2단계: 한 번에 배포 (자동화 스크립트)

```bash
chmod +x deploy-deuknet.sh
./deploy-deuknet.sh
```

### 2단계 (수동): Helm 설치

```bash
# Chart 설치
helm install deuknet-cdc ./helm/deuknet-cdc

# 상태 확인
kubectl get pods
kubectl get svc
```

## 📊 배포 후 확인

### Pod 상태 확인
```bash
kubectl get pods

# 예상 출력:
# NAME                            READY   STATUS
# postgres-xxx                    1/1     Running
# kafka-xxx                       1/1     Running
# kafka-connect-xxx               1/1     Running
# elasticsearch-xxx               1/1     Running
# deuknet-app-xxx                 1/1     Running
# register-connectors-xxx         0/1     Completed
```

### Connector 상태 확인

```bash
# Connector 목록
kubectl exec -it deploy/kafka-connect -- curl -s http://localhost:8083/connectors

# Debezium Source Connector 상태
kubectl exec -it deploy/kafka-connect -- curl -s http://localhost:8083/connectors/debezium-postgres-source/status | jq

# Elasticsearch Sink Connector 상태
kubectl exec -it deploy/kafka-connect -- curl -s http://localhost:8083/connectors/elasticsearch-sink-post-detail/status | jq
```

### Elasticsearch 인덱스 확인

```bash
kubectl exec -it deploy/elasticsearch -- curl -s http://localhost:9200/_cat/indices

# 데이터 확인
kubectl exec -it deploy/elasticsearch -- curl -s http://localhost:9200/posts-detail/_search | jq
```

## 🔌 애플리케이션 접속

```bash
# Port Forward
kubectl port-forward svc/deuknet-app 8080:8080

# Health Check
curl http://localhost:8080/actuator/health

# API 테스트
curl http://localhost:8080/api/posts
```

## ⚙️ 설정 커스터마이징

### values.yaml 주요 설정

```yaml
# Outbox 테이블 설정
debezium:
  connector:
    tableIncludeList: public.outbox_events
    # Outbox 필드 매핑
    # - id: 이벤트 ID
    # - aggregate_id: 집합체 ID (Kafka Key)
    # - aggregate_type: 집합체 타입 (토픽 라우팅)
    # - event_type: 이벤트 타입
    # - payload: JSON 데이터 (Elasticsearch로 저장)
    # - occurred_on: 발생 시간

# Elasticsearch 인덱스 설정
elasticsearchSink:
  connector:
    topics: outbox.events.PostDetail  # 구독할 토픽
    indexName: posts-detail            # Elasticsearch 인덱스명
```

## 🧪 데이터 흐름 테스트

### 1. 게시글 생성
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Post",
    "content": "This is a test"
  }'
```

### 2. Outbox 확인
```bash
kubectl exec -it deploy/postgres -- psql -U deuknet_user -d deuknet -c \
  "SELECT id, aggregate_type, event_type, payload FROM outbox_events ORDER BY occurred_on DESC LIMIT 5;"
```

### 3. Kafka 메시지 확인
```bash
kubectl exec -it deploy/kafka -- kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic outbox.events.PostDetail \
  --from-beginning \
  --max-messages 5
```

### 4. Elasticsearch 데이터 확인
```bash
kubectl exec -it deploy/elasticsearch -- curl -s \
  "http://localhost:9200/posts-detail/_search?pretty"
```

## 🔧 트러블슈팅

### Connector가 등록되지 않을 때

```bash
# Job 로그 확인
kubectl logs job/register-connectors

# Kafka Connect 로그 확인
kubectl logs deploy/kafka-connect

# Job 재실행
kubectl delete job register-connectors
helm upgrade deuknet-cdc ./helm/deuknet-cdc
```

### Elasticsearch에 데이터가 없을 때

```bash
# Sink Connector 상태 확인
kubectl exec -it deploy/kafka-connect -- \
  curl -s http://localhost:8083/connectors/elasticsearch-sink-post-detail/status | jq

# Kafka 메시지가 있는지 확인
kubectl exec -it deploy/kafka -- \
  kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic outbox.events.PostDetail --from-beginning --max-messages 1
```

### PostgreSQL WAL이 너무 커질 때

```bash
# Replication Slot 확인
kubectl exec -it deploy/postgres -- psql -U deuknet_user -d deuknet -c \
  "SELECT slot_name, active, restart_lsn FROM pg_replication_slots;"

# Slot 삭제 (주의!)
kubectl exec -it deploy/postgres -- psql -U deuknet_user -d deuknet -c \
  "SELECT pg_drop_replication_slot('debezium_slot');"
```

## 🗑️ 삭제

```bash
# Helm 릴리스 삭제
helm uninstall deuknet-cdc

# PVC 삭제 (데이터 완전 삭제)
kubectl delete pvc --all

# 모든 Pod 종료 대기
kubectl wait --for=delete pod --all --timeout=60s
```

## 📝 참고 사항

### Outbox 이벤트 포맷

```json
{
  "id": "uuid",
  "aggregate_id": "uuid",
  "aggregate_type": "PostDetail",
  "event_type": "PostCreated",
  "payload": {
    "id": "uuid",
    "title": "제목",
    "content": "내용",
    "authorId": "uuid",
    "status": "PUBLISHED",
    "viewCount": 0,
    "commentCount": 0,
    "likeCount": 0,
    "createdAt": "2025-01-01T00:00:00",
    "updatedAt": "2025-01-01T00:00:00"
  },
  "occurred_on": "2025-01-01T00:00:00"
}
```

### Elasticsearch 인덱스 매핑

Connector가 자동으로 payload의 구조를 파악하여 동적 매핑을 생성합니다.
커스텀 매핑이 필요한 경우 인덱스를 미리 생성하세요:

```bash
kubectl exec -it deploy/elasticsearch -- curl -X PUT \
  http://localhost:9200/posts-detail \
  -H 'Content-Type: application/json' \
  -d '{
    "mappings": {
      "properties": {
        "id": { "type": "keyword" },
        "title": { "type": "text" },
        "content": { "type": "text" },
        "createdAt": { "type": "date" }
      }
    }
  }'
```

## 🏷️ 버전

- Chart Version: 1.0.0
- App Version: 1.0
- Debezium: 2.5
- Elasticsearch: 8.11
- PostgreSQL: 15

## 📄 라이센스

MIT
