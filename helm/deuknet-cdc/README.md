# DeukNet CDC Helm Chart

CDC (Change Data Capture) 파이프라인 + 애플리케이션을 위한 Helm Chart입니다.

## 아키텍처

```
DeukNet Application
    ↓ (Outbox Pattern)
PostgreSQL (Outbox Table)
    ↓ (Debezium)
Kafka
    ↓ (Elasticsearch Sink)
Elasticsearch
    ↑
DeukNet Application (Search API)
```

## 포함된 컴포넌트

- **DeukNet Application**: Spring Boot 애플리케이션
- **PostgreSQL**: WAL 활성화된 데이터베이스
- **Zookeeper**: Kafka 코디네이션
- **Kafka**: 메시지 브로커
- **Kafka Connect**: Debezium 이미지 사용
- **Elasticsearch**: 검색 엔진
- **Connector Registration Job**: 자동으로 Source/Sink Connector 등록

## 빠른 시작 (Minikube)

### 1. Docker 이미지 빌드

```bash
# Minikube Docker 환경 사용
eval $(minikube docker-env)

# 애플리케이션 빌드 및 이미지 생성
./docker-build-push.sh
```

### 2. Helm Chart 설치

```bash
# Chart 설치 (애플리케이션 포함)
helm install deuknet-cdc ./helm/deuknet-cdc

# 상태 확인
kubectl get pods
kubectl get svc
```

### 3. 애플리케이션 접속

```bash
# Port Forward
kubectl port-forward svc/deuknet-app 8080:8080

# API 테스트
curl http://localhost:8080/actuator/health
```

## 삭제

```bash
helm uninstall deuknet-cdc
```

## Connector 상태 확인

```bash
# Kafka Connect Pod 이름 가져오기
CONNECT_POD=$(kubectl get pod -l app=kafka-connect -o jsonpath='{.items[0].metadata.name}')

# 등록된 Connector 목록 확인
kubectl exec -it $CONNECT_POD -- curl -s http://localhost:8083/connectors

# Debezium Connector 상태 확인
kubectl exec -it $CONNECT_POD -- curl -s http://localhost:8083/connectors/debezium-postgres-source/status

# Elasticsearch Sink Connector 상태 확인
kubectl exec -it $CONNECT_POD -- curl -s http://localhost:8083/connectors/elasticsearch-sink-post-detail/status
```

## 설정 변경

`values.yaml` 파일을 수정하여 설정을 변경할 수 있습니다:

```yaml
postgres:
  database: deuknet
  username: deuknet_user
  password: deuknet_pass

debezium:
  connector:
    tableIncludeList: public.outbox_events

elasticsearchSink:
  connector:
    topics: outbox.events.PostDetail
    indexName: posts-detail
```

## 트러블슈팅

### Connector가 등록되지 않을 때

```bash
# Job 로그 확인
kubectl logs job/register-connectors

# Job 재실행
kubectl delete job register-connectors
helm upgrade deuknet-cdc ./helm/deuknet-cdc
```

### Elasticsearch 인덱스 확인

```bash
ES_POD=$(kubectl get pod -l app=elasticsearch -o jsonpath='{.items[0].metadata.name}')
kubectl exec -it $ES_POD -- curl -s http://localhost:9200/_cat/indices
```

### Kafka 토픽 확인

```bash
KAFKA_POD=$(kubectl get pod -l app=kafka -o jsonpath='{.items[0].metadata.name}')
kubectl exec -it $KAFKA_POD -- kafka-topics --bootstrap-server localhost:9092 --list
```
