# Debezium Outbox Pattern 설정 가이드

## 개요

DeukNet 프로젝트에서 Debezium의 Outbox Pattern을 사용하여 데이터 변경 이벤트를 Kafka로 발행합니다.

### 아키텍처

```
Application → Outbox Table → Debezium CDC → Kafka → Consumer Services
```

1. Application이 비즈니스 트랜잭션과 함께 outbox 테이블에 이벤트 저장
2. Debezium이 PostgreSQL의 WAL(Write-Ahead Log)을 모니터링
3. Outbox 테이블의 변경사항을 감지하여 Kafka로 발행
4. Kafka Topic: `{aggregatetype}.events` (예: `Post.events`, `Comment.events`)

## Outbox 테이블 구조

Debezium 표준 형식:

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | UUID | 이벤트 고유 식별자 |
| aggregatetype | VARCHAR(255) | Aggregate 타입 (토픽 라우팅) |
| aggregateid | VARCHAR(255) | Aggregate ID (Kafka 파티션 키) |
| type | VARCHAR(255) | 이벤트 타입 |
| payload | TEXT | 이벤트 페이로드 (JSON) |
| timestamp | BIGINT | 이벤트 발생 시각 (epoch millis) |

### 예시 데이터

```json
{
  "id": "406c07f3-26f0-4eea-a50c-109940064b8f",
  "aggregateid": "1",
  "aggregatetype": "Post",
  "payload": "{\"id\": \"1\", \"title\": \"Hello World\", \"content\": \"...\"}",
  "timestamp": 1556890294344,
  "type": "PostCreated"
}
```

## 설정 단계

### 1. PostgreSQL 설정

WAL 레벨을 `logical`로 설정:

```sql
-- postgresql.conf 또는 실행 시 설정
ALTER SYSTEM SET wal_level = logical;
```

재시작 후 확인:

```sql
SHOW wal_level;
```

### 2. Publication 생성

```sql
-- Debezium이 사용할 publication 생성
CREATE PUBLICATION deuknet_outbox_publication FOR TABLE outbox;

-- 확인
SELECT * FROM pg_publication;
```

### 3. Debezium Connector 등록

Kafka Connect REST API 사용:

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @docs/debezium-outbox-connector.json
```

### 4. Connector 상태 확인

```bash
# Connector 목록 확인
curl http://localhost:8083/connectors

# 특정 Connector 상태 확인
curl http://localhost:8083/connectors/deuknet-outbox-connector/status
```

## Kafka Topic 구조

Debezium Outbox Event Router는 다음과 같이 토픽을 생성합니다:

- `Post.events` - Post 관련 이벤트
- `Comment.events` - Comment 관련 이벤트
- `User.events` - User 관련 이벤트

### 메시지 형식

**Key**: aggregateid (문자열)
```
"123e4567-e89b-12d3-a456-426614174000"
```

**Value**: payload (JSON)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "title": "New Post",
  "content": "Content here...",
  "authorId": "author-uuid",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

**Headers**:
- `eventType`: "PostCreated"

## Consumer 예시

### Spring Kafka Consumer

```java
@KafkaListener(topics = "Post.events")
public void handlePostEvent(
    @Payload String payload,
    @Header("eventType") String eventType,
    @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String aggregateId
) {
    log.info("Received event: type={}, aggregateId={}", eventType, aggregateId);

    // 이벤트 타입에 따라 처리
    switch (eventType) {
        case "PostCreated" -> handlePostCreated(payload);
        case "PostUpdated" -> handlePostUpdated(payload);
        case "PostDeleted" -> handlePostDeleted(aggregateId);
    }
}
```

## 모니터링

### Outbox 테이블 확인

```sql
-- 최근 이벤트 조회
SELECT * FROM outbox ORDER BY timestamp DESC LIMIT 10;

-- 특정 Aggregate의 이벤트
SELECT * FROM outbox WHERE aggregateid = 'some-uuid';

-- 이벤트 타입별 통계
SELECT type, COUNT(*) FROM outbox GROUP BY type;
```

### Debezium 메트릭

Kafka Connect에서 제공하는 JMX 메트릭:

- `debezium.connector.postgresql:type=connector-metrics,context=snapshot`
- `debezium.connector.postgresql:type=connector-metrics,context=streaming`

## 문제 해결

### Connector가 시작되지 않는 경우

1. PostgreSQL 연결 확인
```bash
psql -h postgres -U app_user -d app_db
```

2. WAL 레벨 확인
```sql
SHOW wal_level;  -- 결과가 'logical'이어야 함
```

3. Publication 확인
```sql
SELECT * FROM pg_publication WHERE pubname = 'deuknet_outbox_publication';
```

### 이벤트가 발행되지 않는 경우

1. Outbox 테이블에 데이터가 있는지 확인
```sql
SELECT * FROM outbox ORDER BY timestamp DESC LIMIT 5;
```

2. Connector 로그 확인
```bash
curl http://localhost:8083/connectors/deuknet-outbox-connector/status
```

3. Kafka Topic 확인
```bash
kafka-topics --bootstrap-server localhost:9092 --list
kafka-console-consumer --bootstrap-server localhost:9092 --topic Post.events --from-beginning
```

### Replication Slot 정리

Connector를 삭제한 후 Replication Slot도 정리:

```sql
-- Slot 확인
SELECT slot_name, active FROM pg_replication_slots;

-- Slot 삭제
SELECT pg_drop_replication_slot('deuknet_outbox_slot');
```

## Docker Compose 예시

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    command: postgres -c wal_level=logical
    environment:
      POSTGRES_USER: app_user
      POSTGRES_PASSWORD: app_pass
      POSTGRES_DB: app_db
    ports:
      - "5432:5432"

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"

  kafka-connect:
    image: debezium/connect:2.5
    depends_on:
      - kafka
      - postgres
    environment:
      BOOTSTRAP_SERVERS: kafka:9092
      GROUP_ID: deuknet-connect-cluster
      CONFIG_STORAGE_TOPIC: deuknet_connect_configs
      OFFSET_STORAGE_TOPIC: deuknet_connect_offsets
      STATUS_STORAGE_TOPIC: deuknet_connect_statuses
    ports:
      - "8083:8083"
```

## 성능 최적화

### 1. Outbox 테이블 정리

이벤트는 Debezium이 읽은 후에도 테이블에 남아있습니다. 주기적으로 정리:

```sql
-- 1일 이상 된 이벤트 삭제
DELETE FROM outbox WHERE timestamp < EXTRACT(EPOCH FROM NOW() - INTERVAL '1 day') * 1000;
```

또는 스케줄러로 자동화:

```java
@Scheduled(cron = "0 0 2 * * *")  // 매일 새벽 2시
public void cleanupOldOutboxEvents() {
    long oneDayAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
    outboxEventRepository.deleteByTimestampBefore(oneDayAgo);
}
```

### 2. 배치 크기 조정

Connector 설정:

```json
{
  "max.batch.size": "2048",
  "max.queue.size": "8192"
}
```

### 3. 인덱스 최적화

```sql
-- aggregateid로 조회가 많은 경우
CREATE INDEX idx_outbox_aggregateid ON outbox(aggregateid);

-- timestamp로 정렬하는 경우
CREATE INDEX idx_outbox_timestamp ON outbox(timestamp);
```

## 참고 자료

- [Debezium Outbox Event Router](https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html)
- [PostgreSQL Logical Decoding](https://www.postgresql.org/docs/current/logicaldecoding.html)
- [Kafka Connect REST API](https://docs.confluent.io/platform/current/connect/references/restapi.html)
