# Debezium Embedded Engine 가이드

## 개요

DeukNet 프로젝트에서는 Outbox 패턴과 함께 **Debezium Embedded Engine**을 사용하여 CDC(Change Data Capture)를 구현했습니다.

## 아키텍처

```
PostgreSQL (outbox_events)
    ↓ (CDC)
Debezium Embedded Engine (Spring Boot 내장)
    ↓
DebeziumEventHandler
    ↓
PostSearchAdapter
    ↓
Elasticsearch
```

## 주요 컴포넌트

### 1. DebeziumProperties
- `application.yaml`의 Debezium 설정을 바인딩
- Database 연결 정보, 테이블 필터 등

### 2. DebeziumEngineConfig
- Debezium Engine 생성 및 시작
- Outbox Event Router Transform 설정
- 별도 스레드에서 CDC 실행

### 3. DebeziumEventHandler
- CDC 이벤트를 받아서 처리
- 이벤트 타입별 분기 (PostCreated, PostUpdated 등)
- PostSearchAdapter를 통해 Elasticsearch 동기화

### 4. DebeziumOffsetStorage
- Debezium의 오프셋을 DB에 저장
- Pod 재시작 시에도 마지막 처리 위치 기억

### 5. DebeziumHealthIndicator
- Actuator Health Check
- Kubernetes liveness/readiness probe용

## 설정 방법

### application.yaml

```yaml
debezium:
  enabled: true
  connector-name: deuknet-outbox-connector
  offset-storage-file-name: /tmp/debezium-offsets.dat
  database:
    hostname: postgres
    port: 5432
    name: app_db
    username: app_user
    password: app_pass
    schema-include-list: public
    table-include-list: public.outbox_events
```

### PostgreSQL 설정

```sql
-- 1. WAL 레벨 확인/설정
ALTER SYSTEM SET wal_level = logical;
-- PostgreSQL 재시작 필요

-- 2. Publication 생성
CREATE PUBLICATION deuknet_outbox_publication
FOR TABLE outbox_events;

-- 3. Offset Storage 테이블
CREATE TABLE debezium_offset_storage (
    id VARCHAR(255) PRIMARY KEY,
    offset_key TEXT,
    offset_value TEXT
);
```

## Kubernetes 배포 시 주의사항

### ⚠️ 반드시 단일 인스턴스만 실행

```yaml
# helm/deuknet-cdc/values.yaml
app:
  replicas: 1  # 반드시 1개만!
```

**이유:**
- 여러 인스턴스가 동일한 CDC 스트림을 처리하면 중복 이벤트 발생
- Outbox 레코드가 여러 번 처리됨

**해결책:**
- Scale-out이 필요하면 Kafka Connect로 전환

## 이벤트 흐름

### 1. Post 생성 시

```
CreatePostService
  ↓ (Outbox에 이벤트 저장)
outbox_events 테이블 INSERT
  ↓ (PostgreSQL WAL)
Debezium Embedded Engine (CDC 감지)
  ↓
DebeziumEventHandler.handleEvent()
  ↓ (JSON 파싱)
PostSearchAdapter.indexPostDetail()
  ↓
Elasticsearch에 인덱싱
```

### 2. Reaction 추가 시

```
AddReactionService
  ↓ (PostCountProjection 이벤트 발행)
outbox_events 테이블 INSERT
  ↓
Debezium Embedded Engine
  ↓
DebeziumEventHandler.handleReactionEvent()
  ↓
PostSearchAdapter.updatePostCounts()
  ↓
Elasticsearch partial update
```

## 모니터링

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

응답:
```json
{
  "status": "UP",
  "components": {
    "debeziumHealthIndicator": {
      "status": "UP",
      "details": {
        "status": "Debezium Engine is running"
      }
    }
  }
}
```

### Offset 확인

```sql
SELECT * FROM debezium_offset_storage;
```

### Replication Slot 확인

```sql
SELECT * FROM pg_replication_slots
WHERE slot_name = 'deuknet_outbox_slot';
```

## 트러블슈팅

### 1. Debezium Engine이 시작되지 않음

**증상:** 애플리케이션은 실행되지만 CDC 이벤트가 처리되지 않음

**해결:**
```bash
# PostgreSQL wal_level 확인
psql -U app_user -d app_db -c "SHOW wal_level;"

# logical이 아니면 설정 변경
ALTER SYSTEM SET wal_level = logical;
-- PostgreSQL 재시작 필요
```

### 2. Publication이 없음

**증상:** `ERROR: publication "deuknet_outbox_publication" does not exist`

**해결:**
```sql
CREATE PUBLICATION deuknet_outbox_publication
FOR TABLE outbox_events;
```

### 3. Offset이 저장되지 않음

**증상:** Pod 재시작 후 처음부터 다시 처리

**해결:**
```sql
-- Offset Storage 테이블 생성 확인
CREATE TABLE IF NOT EXISTS debezium_offset_storage (
    id VARCHAR(255) PRIMARY KEY,
    offset_key TEXT,
    offset_value TEXT
);
```

### 4. 중복 이벤트 발생

**증상:** 동일한 Post가 Elasticsearch에 여러 번 인덱싱됨

**원인:** replicas > 1

**해결:**
```yaml
# values.yaml
app:
  replicas: 1  # 반드시 1로 설정
```

## Kafka Connect로 전환하기

Scale-out이 필요해지면 다음 단계로 전환:

### 1. Debezium 비활성화

```yaml
# application.yaml
debezium:
  enabled: false
```

### 2. Kafka Connect 배포

```bash
helm upgrade --install deuknet ./helm/deuknet-cdc \
  --set kafkaConnect.enabled=true
```

### 3. Connector 등록

```bash
kubectl apply -f helm/deuknet-cdc/templates/connector-job.yaml
```

### 4. 애플리케이션 Scale-out

```yaml
# values.yaml
app:
  replicas: 3  # 이제 안전하게 스케일 가능
```

## 성능 최적화

### 1. Offset Flush 간격 조정

```java
// DebeziumEngineConfig.java
.with("offset.flush.interval.ms", "1000")  // 1초 (기본값)
// 더 자주 플러시하려면 값을 낮춤
```

### 2. Batch 크기 조정

```java
.with("max.batch.size", "2048")  // 배치 크기
.with("poll.interval.ms", "1000")  // 폴링 간격
```

### 3. Snapshot 모드

```java
.with("snapshot.mode", "initial")  // 초기 스냅샷 후 CDC
// 옵션: never, initial, always, when_needed
```

## 참고 자료

- [Debezium Documentation](https://debezium.io/documentation/)
- [Debezium Embedded Engine](https://debezium.io/documentation/reference/stable/development/engine.html)
- [Outbox Pattern](https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html)
