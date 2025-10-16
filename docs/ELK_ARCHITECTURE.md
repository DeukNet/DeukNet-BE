# DeukNet ELK 로그 수집 아키텍처

## 📊 전체 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Kubernetes Cluster                           │
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │   myapp      │  │ log-generator│  │  Other Pods  │              │
│  │ (Spring Boot)│  │  (Test Pod)  │  │              │              │
│  │              │  │              │  │              │              │
│  │ Label:       │  │ Label:       │  │ No Label     │              │
│  │ collect-logs │  │ collect-logs │  │ (Excluded)   │              │
│  │   = true     │  │   = true     │  │              │              │
│  └──────┬───────┘  └──────┬───────┘  └──────────────┘              │
│         │                 │                                          │
│         │ stdout/stderr   │ stdout/stderr                           │
│         ▼                 ▼                                          │
│  ┌──────────────────────────────────────────────────────┐           │
│  │        /var/log/containers/*.log (Node)              │           │
│  │        (Docker/containerd log files)                 │           │
│  └──────────────────────┬───────────────────────────────┘           │
│                         │                                            │
│                         │ File Reading                               │
│                         ▼                                            │
│  ┌─────────────────────────────────────────────────────────┐        │
│  │              Filebeat (DaemonSet)                        │        │
│  │  ┌─────────────────────────────────────────────────┐    │        │
│  │  │ Autodiscovery:                                   │    │        │
│  │  │  - Kubernetes API를 통한 Pod 모니터링          │    │        │
│  │  │  - Label 기반 필터링 (collect-logs: true)     │    │        │
│  │  │  - Kubernetes 메타데이터 추가                  │    │        │
│  │  │    * Namespace, Pod name, Container name       │    │        │
│  │  │    * Labels, Annotations                        │    │        │
│  │  └─────────────────────────────────────────────────┘    │        │
│  │                                                           │        │
│  │  Processors:                                              │        │
│  │  - add_kubernetes_metadata                                │        │
│  │  - add_cloud_metadata                                     │        │
│  │  - drop_event (Job, kube-system 제외)                   │        │
│  └──────────────────────────┬────────────────────────────────┘        │
│                             │                                         │
│                             │ Beats Protocol (Port 5044)              │
│                             │ (구조화된 JSON 이벤트 전송)            │
│                             ▼                                         │
│  ┌─────────────────────────────────────────────────────────┐         │
│  │                Logstash (Deployment)                     │         │
│  │  ┌─────────────────────────────────────────────────┐    │         │
│  │  │ Input:                                           │    │         │
│  │  │  - beats { port => 5044 }                       │    │         │
│  │  │  - Filebeat로부터 이벤트 수신                  │    │         │
│  │  └─────────────────────────────────────────────────┘    │         │
│  │                                                           │         │
│  │  ┌─────────────────────────────────────────────────┐    │         │
│  │  │ Filter: (선택적 - 추가 가능)                   │    │         │
│  │  │  - grok: 로그 파싱                             │    │         │
│  │  │  - mutate: 필드 추가/수정/삭제                 │    │         │
│  │  │  - date: 타임스탬프 파싱                       │    │         │
│  │  │  - drop: 특정 이벤트 제외                      │    │         │
│  │  └─────────────────────────────────────────────────┘    │         │
│  │                                                           │         │
│  │  ┌─────────────────────────────────────────────────┐    │         │
│  │  │ Output:                                          │    │         │
│  │  │  elasticsearch {                                 │    │         │
│  │  │    hosts => ["http://elasticsearch:9200"]       │    │         │
│  │  │    user => "logstash_writer"                    │    │         │
│  │  │    password => "${ELASTICSEARCH_PASSWORD}"      │    │         │
│  │  │    index => "logs-%{+YYYY.MM.dd}"               │    │         │
│  │  │  }                                               │    │         │
│  │  └─────────────────────────────────────────────────┘    │         │
│  └──────────────────────────┬────────────────────────────────┘         │
│                             │                                         │
│                             │ HTTP REST API (Port 9200)               │
│                             │ (Bulk API로 배치 인덱싱)                │
│                             ▼                                         │
│  ┌─────────────────────────────────────────────────────────┐         │
│  │            Elasticsearch (Deployment)                    │         │
│  │  ┌─────────────────────────────────────────────────┐    │         │
│  │  │ 인덱싱:                                          │    │         │
│  │  │  - 인덱스: logs-YYYY.MM.dd                      │    │         │
│  │  │  - 샤드: 1 primary, 1 replica                  │    │         │
│  │  │  - 매핑: 자동 생성 (ECS 호환)                  │    │         │
│  │  └─────────────────────────────────────────────────┘    │         │
│  │                                                           │         │
│  │  ┌─────────────────────────────────────────────────┐    │         │
│  │  │ 사용자 관리:                                     │    │         │
│  │  │  - elastic: 슈퍼관리자                          │    │         │
│  │  │  - kibana_system: Kibana 전용                  │    │         │
│  │  │  - logstash_writer: 로그 쓰기 + 템플릿 생성   │    │         │
│  │  │  - beats_system: Beats 모니터링                │    │         │
│  │  └─────────────────────────────────────────────────┘    │         │
│  │                                                           │         │
│  │  저장된 데이터 구조:                                      │         │
│  │  {                                                        │         │
│  │    "@timestamp": "2025-10-15T00:00:00.000Z",            │         │
│  │    "message": "Application log message",                 │         │
│  │    "kubernetes": {                                       │         │
│  │      "namespace": "default",                             │         │
│  │      "pod": { "name": "myapp-xxx" },                    │         │
│  │      "container": { "name": "myapp" },                  │         │
│  │      "labels": { "app": "myapp" }                       │         │
│  │    }                                                      │         │
│  │  }                                                        │         │
│  └──────────────────────────┬────────────────────────────────┘         │
│                             │                                         │
│                             │ HTTP REST API (Port 9200)               │
│                             │ (검색, 집계 쿼리)                       │
│                             ▼                                         │
│  ┌─────────────────────────────────────────────────────────┐         │
│  │                 Kibana (Deployment)                      │         │
│  │  ┌─────────────────────────────────────────────────┐    │         │
│  │  │ 기능:                                            │    │         │
│  │  │  - Discover: 로그 검색 및 필터링               │    │         │
│  │  │  - Dashboard: 시각화 대시보드                  │    │         │
│  │  │  - Visualize: 차트/그래프 생성                 │    │         │
│  │  │  - Dev Tools: Elasticsearch 쿼리              │    │         │
│  │  │  - Alerting: 알람 규칙 설정                   │    │         │
│  │  └─────────────────────────────────────────────────┘    │         │
│  │                                                           │         │
│  │  User: elastic / kibana_system                            │         │
│  │  Password: deuknet2024                                    │         │
│  └───────────────────────────────────────────────────────────┘         │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘

             ▲
             │
             │ Port-Forward / NodePort / LoadBalancer
             │ (http://localhost:5601)
             │
        ┌────┴────┐
        │  User   │
        │ Browser │
        └─────────┘
```

## 🔄 데이터 흐름 상세 설명

### 1️⃣ **로그 생성 단계**
```
Application → stdout/stderr
```
- Spring Boot 애플리케이션이 로그 출력
- System.out.println(), logger.info() 등
- Kubernetes가 자동으로 파일로 저장
  * 위치: `/var/log/containers/<pod>_<namespace>_<container>-<id>.log`
  * 포맷: JSON Lines (각 줄이 JSON 객체)

**파일 예시:**
```json
{"log":"2025-10-15 00:00:00 INFO  Application started\n","stream":"stdout","time":"2025-10-15T00:00:00.123456789Z"}
```

### 2️⃣ **로그 수집 단계 (Filebeat)**
```
Log Files → Filebeat → Structured Events
```

**Filebeat의 역할:**
- **Autodiscovery**: Kubernetes API를 모니터링하여 새로운 Pod 감지
- **필터링**: `collect-logs: true` 라벨이 있는 Pod만 선택
- **메타데이터 추가**: 
  ```json
  {
    "kubernetes": {
      "namespace": "default",
      "pod": {"name": "myapp-xxx", "uid": "..."},
      "container": {"name": "myapp"},
      "labels": {"app": "myapp", "collect-logs": "true"},
      "node": {"name": "minikube"}
    }
  }
  ```
- **이벤트 전송**: Beats Protocol로 Logstash에 전송

**Filebeat가 보내는 이벤트 구조:**
```json
{
  "@timestamp": "2025-10-15T00:00:00.000Z",
  "message": "Application started",
  "log": {"file": {"path": "/var/log/containers/..."}},
  "kubernetes": {...},
  "host": {"name": "minikube"},
  "agent": {"type": "filebeat", "version": "8.11.0"}
}
```

### 3️⃣ **로그 처리 단계 (Logstash)**
```
Filebeat Events → Logstash Pipeline → Elasticsearch
```

**Logstash Pipeline 구조:**
```ruby
input {
  # Filebeat로부터 이벤트 수신
  beats {
    port => 5044
    # Beats Protocol (압축, 암호화 지원)
  }
}

filter {
  # 현재는 비어있음 (추가 가능)
  # 예: grok으로 로그 파싱
  # 예: mutate로 필드 변환
  # 예: date로 타임스탬프 파싱
}

output {
  # Elasticsearch에 벌크로 전송
  elasticsearch {
    hosts => ["http://elasticsearch:9200"]
    user => "logstash_writer"
    password => "${ELASTICSEARCH_PASSWORD}"
    index => "logs-%{+YYYY.MM.dd}"  # 날짜별 인덱스
    # 예: logs-2025.10.15
  }
}
```

**Logstash의 주요 기능:**
- **버퍼링**: 일시적인 Elasticsearch 장애 대응
- **배치 처리**: 여러 이벤트를 모아서 한 번에 전송 (성능 최적화)
- **변환**: 필요시 로그 형식 변환
- **라우팅**: 조건에 따라 다른 인덱스로 전송 가능

### 4️⃣ **로그 저장 단계 (Elasticsearch)**
```
Logstash → Elasticsearch REST API → Index → Shard
```

**인덱싱 프로세스:**
1. **인덱스 생성**: `logs-2025.10.15` (날짜별 자동 생성)
2. **매핑 생성**: 필드 타입 자동 추론
   ```json
   {
     "mappings": {
       "properties": {
         "@timestamp": {"type": "date"},
         "message": {"type": "text"},
         "kubernetes.pod.name": {"type": "keyword"}
       }
     }
   }
   ```
3. **문서 저장**: JSON 문서를 인덱스에 저장
4. **역색인 생성**: 빠른 검색을 위한 색인 구조

**인덱스 구조:**
```
logs-2025.10.15
├── Primary Shard 0
│   ├── Document 1 (myapp log)
│   ├── Document 2 (log-generator log)
│   └── Document 3 (myapp log)
└── Replica Shard 0 (백업)
```

**Elasticsearch의 역할:**
- **인덱싱**: 로그를 검색 가능한 형태로 저장
- **검색**: 전문 검색 (Full-text search)
- **집계**: 통계, 그룹화, 평균 등 계산
- **분석**: 텍스트 분석, 토큰화

### 5️⃣ **로그 시각화 단계 (Kibana)**
```
User → Kibana → Elasticsearch Query → Results
```

**Kibana의 주요 기능:**

1. **Discover (검색)**
   - 실시간 로그 검색
   - KQL (Kibana Query Language) 사용
   - 예: `kubernetes.pod.name: "myapp-*" AND message: "error"`

2. **Visualize (시각화)**
   - Line Chart: 시간대별 로그 개수
   - Pie Chart: 애플리케이션별 로그 비율
   - Table: 에러 로그 목록

3. **Dashboard (대시보드)**
   - 여러 시각화를 한 화면에 배치
   - 실시간 자동 갱신

4. **Dev Tools (개발 도구)**
   - Elasticsearch에 직접 쿼리
   ```json
   GET logs-*/_search
   {
     "query": {
       "match": {"message": "error"}
     }
   }
   ```

5. **Alerting (알람)**
   - 특정 조건 만족 시 알림
   - 예: 에러 로그가 분당 10개 초과 시

## 🔐 보안 및 인증

### 사용자별 권한

```
┌─────────────────┬──────────────────┬─────────────────────────┐
│ 사용자          │ 역할             │ 권한                    │
├─────────────────┼──────────────────┼─────────────────────────┤
│ elastic         │ 슈퍼관리자       │ 모든 권한               │
│ kibana_system   │ Kibana 전용      │ Kibana 내부 통신        │
│ logstash_writer │ 로그 쓰기        │ 인덱스 생성/쓰기        │
│                 │                  │ 템플릿 관리             │
│ beats_system    │ Beats 모니터링   │ Beats 상태 전송         │
└─────────────────┴──────────────────┴─────────────────────────┘
```

### 인증 흐름

```
Logstash → Elasticsearch
  ├─ User: logstash_writer
  ├─ Password: deuknet2024 (Secret)
  └─ Action: POST /logs-2025.10.15/_bulk
      └─ 권한 확인: logstash_writer role
          └─ indices:write on logs-*  ✅ Allowed

Kibana → Elasticsearch  
  ├─ User: kibana_system
  ├─ Password: deuknet2024 (Secret)
  └─ Action: GET /.kibana/*
      └─ 권한 확인: kibana_system role  ✅ Allowed

User → Kibana
  ├─ User: elastic
  ├─ Password: deuknet2024
  └─ Session: Cookie-based authentication
```

## 📊 데이터 라이프사이클

```
Day 1: logs-2025.10.15
  ├─ 생성: 첫 로그 도착 시 자동 생성
  ├─ 쓰기: 하루 종일 로그 수집
  └─ 크기: ~100MB - 10GB (로그량에 따라)

Day 2: logs-2025.10.16
  ├─ 생성: 새로운 인덱스 자동 생성
  ├─ 이전 인덱스: 읽기 전용으로 변경 가능
  └─ 검색: logs-* 패턴으로 모든 인덱스 검색

Day 30: logs-2025.09.15
  ├─ 오래된 인덱스 삭제 (선택사항)
  └─ Index Lifecycle Management (ILM) 사용 가능
```

## 🚀 성능 최적화

### 1. **Filebeat 레벨**
- Label 기반 필터링으로 불필요한 로그 제외
- Multiline 설정으로 스택트레이스 그룹화
- 버퍼링으로 네트워크 효율성 향상

### 2. **Logstash 레벨**
- Pipeline 워커 수 조정 (`pipeline.workers`)
- 배치 크기 최적화 (`batch.size`, `batch.delay`)
- 필터 최소화 (무거운 grok 패턴 회피)

### 3. **Elasticsearch 레벨**
- 샤드 개수 최적화 (일일 로그량에 따라)
- 리플리카 설정 (고가용성 vs 디스크 공간)
- 인덱스 템플릿으로 매핑 최적화

### 4. **Kibana 레벨**
- 시간 범위 제한 (최근 15분, 1시간 등)
- 필드 필터링 (필요한 필드만 표시)
- 대시보드 캐싱

## 🔍 트러블슈팅 체크리스트

### 로그가 Kibana에 안 보일 때:

```
1. Filebeat 확인
   kubectl logs -f daemonset/filebeat
   → "Events sent" 메시지 확인
   → Label 필터링 확인 (collect-logs: true)

2. Logstash 확인
   kubectl logs -f deployment/logstash
   → "Connection from" 메시지 확인
   → Elasticsearch 연결 확인

3. Elasticsearch 확인
   kubectl exec deployment/elasticsearch -- \
     curl -u elastic:deuknet2024 \
     http://localhost:9200/_cat/indices/logs-*?v
   → 인덱스 생성 확인
   → 문서 개수 확인

4. Kibana 확인
   → Data View 생성 확인 (logs-*)
   → 시간 범위 확인 (Last 15 minutes)
   → 필터 확인
```

## 📈 모니터링 지표

en/beats/filebeat/
- Logstash 파이프라인: https://www.elastic.co/guide/en/logstash/
- Kibana 가이드: https://www.elastic.co/guide/en/kibana/

## 💡 실전 활용 예시

### 시나리오 1: 에러 로그 추적

```
문제: 프로덕션에서 간헐적으로 NullPointerException 발생

1. Kibana Discover에서 검색:
   kubernetes.pod.name: "myapp-*" AND message: "NullPointerException"

2. 시간대 분석:
   - Line Chart로 에러 발생 패턴 확인
   - 특정 시간대에 집중되는지 확인

3. 컨텍스트 확인:
   - 해당 로그의 전후 로그 확인
   - Request ID로 전체 트랜잭션 추적

4. 근본 원인 분석:
   - 어떤 API 호출에서 발생했는지
   - 어떤 사용자/요청에서 발생했는지
```

### 시나리오 2: 성능 모니터링

```
목표: API 응답 시간 모니터링

1. 구조화된 로깅:
   logger.info("API call completed",
     kv("endpoint", "/api/users"),
     kv("method", "GET"),
     kv("duration_ms", 234),
     kv("status", 200)
   );

2. Logstash 필터로 숫자 변환:
   mutate {
     convert => { "duration_ms" => "integer" }
   }

3. Kibana Visualize:
   - Metric: Average of duration_ms
   - Line Chart: duration_ms over time
   - Heatmap: endpoint별 응답 시간

4. 알람 설정:
   - duration_ms > 1000ms일 때 알림
   - Slack으로 즉시 통보
```

### 시나리오 3: 사용자 행동 분석

```
목표: 사용자 로그인 패턴 분석

1. 로그 수집:
   logger.info("User login",
     kv("userId", userId),
     kv("loginTime", timestamp),
     kv("ipAddress", ip)
   );

2. Kibana 분석:
   - Pie Chart: 시간대별 로그인 분포
   - Data Table: 가장 활동적인 사용자 Top 10
   - Geo Map: 지역별 로그인 (IP 기반)

3. 이상 탐지:
   - 동일 userId로 짧은 시간 내 여러 IP에서 로그인
   - 비정상적인 시간대 로그인 (새벽 2-4시)
```

### 시나리오 4: 디버깅 세션

```
문제: 특정 기능이 간헐적으로 실패

1. 임시로 DEBUG 레벨 활성화:
   kubectl set env deployment/myapp LOGGING_LEVEL_ROOT=DEBUG

2. 상세 로그 수집:
   - 메소드 진입/종료 로그
   - 변수 값 로그
   - 데이터베이스 쿼리 로그

3. Kibana에서 분석:
   - Request ID로 전체 플로우 추적
   - 각 단계별 소요 시간 확인
   - 실패 지점 특정

4. 문제 해결 후 로그 레벨 복구:
   kubectl set env deployment/myapp LOGGING_LEVEL_ROOT=INFO
```

## 🎨 Kibana 대시보드 예시

### 대시보드 1: 애플리케이션 건강 상태

```
┌─────────────────────────────────────────────────────────┐
│            Application Health Dashboard                 │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  [Total Logs: 125,234]  [Errors: 45]  [Warnings: 289]  │
│                                                          │
│  ┌──────────────────────────────────────────┐          │
│  │  Log Volume (Last 24h)                   │          │
│  │  [Line Chart showing log count over time]│          │
│  └──────────────────────────────────────────┘          │
│                                                          │
│  ┌─────────────────────┐  ┌─────────────────────┐      │
│  │  Error Rate         │  │  Top Errors         │      │
│  │  [Gauge: 0.036%]    │  │  1. NPE: 12 times  │      │
│  │                     │  │  2. SQL: 8 times   │      │
│  │                     │  │  3. IO: 6 times    │      │
│  └─────────────────────┘  └─────────────────────┘      │
│                                                          │
│  ┌──────────────────────────────────────────┐          │
│  │  Logs by Pod                              │          │
│  │  [Pie Chart: myapp-xxx 60%, myapp-yyy 40%]│          │
│  └──────────────────────────────────────────┘          │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 대시보드 2: 성능 모니터링

```
┌─────────────────────────────────────────────────────────┐
│              Performance Monitoring                      │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  [Avg Response Time: 145ms]  [P95: 340ms]  [P99: 890ms]│
│                                                          │
│  ┌──────────────────────────────────────────┐          │
│  │  Response Time Trend                      │          │
│  │  [Area Chart showing response time]       │          │
│  └──────────────────────────────────────────┘          │
│                                                          │
│  ┌──────────────────────────────────────────┐          │
│  │  Slowest Endpoints                        │          │
│  │  /api/reports: 2.3s                      │          │
│  │  /api/analytics: 1.8s                    │          │
│  │  /api/export: 1.2s                       │          │
│  └──────────────────────────────────────────┘          │
│                                                          │
│  ┌──────────────────────────────────────────┐          │
│  │  Request Volume Heatmap                   │          │
│  │  [Heatmap by hour and endpoint]           │          │
│  └──────────────────────────────────────────┘          │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

## 🔧 고급 Logstash 필터 예시

### Java 애플리케이션 로그 파싱

```ruby
filter {
  # Multiline: 스택트레이스 합치기
  if [kubernetes][container][name] == "myapp" {
    
    # JSON 로그 파싱
    if [message] =~ /^{.*}$/ {
      json {
        source => "message"
        target => "app"
      }
    }
    
    # 일반 텍스트 로그 파싱
    else {
      grok {
        match => {
          "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:log_level} \[%{DATA:thread}\] %{JAVACLASS:logger} : %{GREEDYDATA:log_message}"
        }
      }
      
      # 타임스탬프 파싱
      date {
        match => ["timestamp", "ISO8601"]
        target => "@timestamp"
      }
    }
    
    # 에러 레벨에 따라 필드 추가
    if [log_level] == "ERROR" {
      mutate {
        add_field => { "severity" => "high" }
        add_tag => ["error"]
      }
    }
    
    # 민감한 정보 마스킹
    mutate {
      gsub => [
        "message", "password=[^&\s]+", "password=***",
        "message", "token=[^&\s]+", "token=***"
      ]
    }
    
    # Request ID 추출
    if [message] =~ /request_id=/ {
      grok {
        match => { "message" => "request_id=%{UUID:request_id}" }
      }
    }
    
    # 불필요한 필드 제거
    mutate {
      remove_field => ["agent", "ecs", "input"]
    }
  }
}
```

### 조건부 라우팅

```ruby
output {
  # 에러 로그는 별도 인덱스로
  if "error" in [tags] {
    elasticsearch {
      hosts => ["${ELASTICSEARCH_HOSTS}"]
      user => "${ELASTICSEARCH_USERNAME}"
      password => "${ELASTICSEARCH_PASSWORD}"
      index => "errors-%{+YYYY.MM.dd}"
    }
  }
  
  # 일반 로그
  else {
    elasticsearch {
      hosts => ["${ELASTICSEARCH_HOSTS}"]
      user => "${ELASTICSEARCH_USERNAME}"
      password => "${ELASTICSEARCH_PASSWORD}"
      index => "logs-%{+YYYY.MM.dd}"
    }
  }
  
  # 개발 환경에서는 stdout 출력
  if [kubernetes][namespace] == "dev" {
    stdout {
      codec => rubydebug
    }
  }
}
```

## 📊 인덱스 템플릿 최적화

### 커스텀 인덱스 템플릿

```json
PUT _index_template/logs-template
{
  "index_patterns": ["logs-*"],
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 1,
      "index.lifecycle.name": "logs-policy",
      "index.lifecycle.rollover_alias": "logs"
    },
    "mappings": {
      "properties": {
        "@timestamp": {
          "type": "date"
        },
        "message": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "log_level": {
          "type": "keyword"
        },
        "duration_ms": {
          "type": "long"
        },
        "kubernetes": {
          "properties": {
            "pod": {
              "properties": {
                "name": {"type": "keyword"}
              }
            },
            "namespace": {"type": "keyword"},
            "container": {
              "properties": {
                "name": {"type": "keyword"}
              }
            }
          }
        }
      }
    }
  }
}
```

### Index Lifecycle Management (ILM)

```json
PUT _ilm/policy/logs-policy
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "rollover": {
            "max_size": "50GB",
            "max_age": "1d"
          },
          "set_priority": {
            "priority": 100
          }
        }
      },
      "warm": {
        "min_age": "7d",
        "actions": {
          "readonly": {},
          "forcemerge": {
            "max_num_segments": 1
          },
          "set_priority": {
            "priority": 50
          }
        }
      },
      "cold": {
        "min_age": "30d",
        "actions": {
          "set_priority": {
            "priority": 0
          }
        }
      },
      "delete": {
        "min_age": "90d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

## 🎯 운영 체크리스트

### 일일 점검 항목

```
□ Elasticsearch 클러스터 상태 (Green)
  kubectl exec deployment/elasticsearch -- \
    curl -u elastic:deuknet2024 http://localhost:9200/_cluster/health

□ 디스크 사용량 (<80%)
  kubectl exec deployment/elasticsearch -- \
    df -h

□ 인덱스 크기 확인
  kubectl exec deployment/elasticsearch -- \
    curl -u elastic:deuknet2024 \
    'http://localhost:9200/_cat/indices/logs-*?v&h=index,store.size&s=index:desc'

□ Filebeat 정상 작동
  kubectl get pods -l app=filebeat

□ Logstash 처리량 확인
  kubectl logs deployment/logstash | grep "events"

□ 에러 로그 검토 (Kibana)
  - 새로운 에러 패턴 확인
  - 에러 증가 추세 확인
```

### 주간 점검 항목

```
□ 인덱스 정리
  - 90일 이상 된 인덱스 삭제
  - ILM 정책 실행 상태 확인

□ 성능 리뷰
  - 평균 응답 시간 추이
  - 느린 쿼리 분석
  - 리소스 사용률 검토

□ 보안 점검
  - 비밀번호 변경 필요성 검토
  - 접근 로그 검토
  - 비정상 로그인 시도 확인

□ 백업 확인
  - 스냅샷 생성 상태
  - 복구 테스트
```

## 🚨 일반적인 문제 해결

### 문제 1: Elasticsearch 디스크 풀

```bash
# 증상
cluster.routing.allocation.disk.threshold_exceeded

# 해결
1. 오래된 인덱스 삭제
   kubectl exec deployment/elasticsearch -- \
     curl -X DELETE -u elastic:deuknet2024 \
     'http://localhost:9200/logs-2025.01.*'

2. 디스크 임계값 임시 조정
   PUT _cluster/settings
   {
     "transient": {
       "cluster.routing.allocation.disk.watermark.low": "95%",
       "cluster.routing.allocation.disk.watermark.high": "97%"
     }
   }

3. 볼륨 확장 (영구 해결)
```

### 문제 2: Logstash 메모리 부족

```bash
# 증상
OutOfMemoryError in Logstash logs

# 해결
1. JVM 힙 크기 조정
   env:
   - name: LS_JAVA_OPTS
     value: "-Xms1g -Xmx1g"

2. 리소스 제한 증가
   resources:
     limits:
       memory: "2Gi"

3. Pipeline 워커 수 감소
   pipeline.workers: 2
```

### 문제 3: Filebeat 로그 유실

```bash
# 증상
로그가 간헐적으로 누락됨

# 해결
1. Filebeat registry 확인
   kubectl exec daemonset/filebeat -- \
     cat /usr/share/filebeat/data/registry/filebeat/log.json

2. 백프레셔 확인
   kubectl logs daemonset/filebeat | grep "publish"

3. Logstash 큐 크기 증가
   queue.type: persisted
   queue.max_bytes: 1gb
```

### 문제 4: Kibana 느린 응답

```bash
# 해결
1. 시간 범위 제한
   - Last 15 minutes 대신 Last 1 hour

2. 필드 수 제한
   - 필요한 필드만 선택 표시

3. Kibana 캐시 클리어
   kubectl delete pod -l app=kibana

4. 인덱스 패턴 최적화
   - logs-* 대신 logs-2025.10.* 사용
```

## 📚 추가 학습 자료

### ELK 스택 심화

1. **Elasticsearch Deep Dive**
   - 샤드와 리플리카 전략
   - 검색 최적화
   - 집계 쿼리 고급 기법

2. **Logstash Patterns**
   - Grok 패턴 작성법
   - 커스텀 플러그인 개발
   - 성능 튜닝

3. **Kibana Advanced**
   - Canvas로 프레젠테이션
   - Machine Learning 이상 탐지
   - 알람 및 알림 설정

4. **Beats 생태계**
   - Metricbeat: 시스템 메트릭
   - Packetbeat: 네트워크 패킷
   - Heartbeat: 업타임 모니터링

### Kubernetes + ELK

1. **운영 자동화**
   - Helm Chart로 배포
   - Operator 패턴
   - GitOps with ArgoCD

2. **고가용성 구성**
   - Multi-node Elasticsearch
   - StatefulSet vs Deployment
   - PersistentVolume 전략

3. **보안 강화**
   - TLS/SSL 설정
   - RBAC 세밀화
   - NetworkPolicy 적용
   - Secrets 관리 (Vault 연동)

## 🎓 마무리

이 문서에서 다룬 내용:

✅ ELK 스택의 전체 아키텍처
✅ 각 컴포넌트의 역할과 동작 원리
✅ 데이터 흐름과 처리 과정
✅ 보안 및 인증 체계
✅ 필터링 전략 (Label 기반)
✅ 트러블슈팅 가이드
✅ 실전 활용 예시
✅ 성능 최적화 방법
✅ 운영 체크리스트

### 핵심 요약

1. **Filebeat**: Label 기반으로 필요한 로그만 수집
2. **Logstash**: 변환/라우팅 (현재는 pass-through)
3. **Elasticsearch**: 인덱싱/검색/저장
4. **Kibana**: 시각화/분석/알람

### 다음 단계

1. Logstash filter 추가하여 로그 파싱
2. Kibana 대시보드 구성
3. 알람 설정으로 프로액티브 모니터링
4. ILM으로 인덱스 라이프사이클 자동화
5. 프로덕션 환경으로 확장 (고가용성)

---

**작성일**: 2025-10-15
**프로젝트**: DeukNet
**버전**: 1.0
**문의**: [your-email]

