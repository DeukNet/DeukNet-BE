# DeukNet ELK 스택 가이드

> Kubernetes 기반 중앙 집중식 로깅 시스템

## 🚀 빠른 시작

```powershell
# 전체 스택 배포
cd C:\DeukNet\k8s
.\run-minikube.ps1

# Kibana 접속
minikube service kibana --url
# Username: elastic
# Password: deuknet2024
```

## 📊 아키텍처 개요

```
Application Pods → Filebeat → Logstash → Elasticsearch → Kibana
```

**상세 아키텍처**: [ELK_ARCHITECTURE.md](./ELK_ARCHITECTURE.md) 참고

## 🎯 주요 기능

- ✅ **자동 로그 수집**: Filebeat DaemonSet으로 모든 노드에서 수집
- ✅ **Label 기반 필터링**: `collect-logs: true` 라벨이 있는 Pod만 수집
- ✅ **날짜별 인덱싱**: `logs-YYYY.MM.dd` 형식으로 자동 분리
- ✅ **실시간 검색**: Kibana에서 즉시 검색 가능
- ✅ **보안**: 사용자별 권한 분리

## 📁 디렉토리 구조

```
k8s/
├── elasticsearch/     # Elasticsearch 설정
├── logstash/         # Logstash 파이프라인
├── filebeat/         # Filebeat 수집 설정
├── kibana/           # Kibana 시각화
└── app/              # 애플리케이션 (로그 생성)
```

## 🔧 로그 수집 설정

### 애플리케이션에 라벨 추가

```yaml
# deployment.yaml
metadata:
  labels:
    collect-logs: "true"  # ← 이 라벨 추가
```

### 로그 수집 확인

```powershell
# Filebeat 로그 확인
kubectl logs -f daemonset/filebeat

# Elasticsearch 인덱스 확인
kubectl exec deployment/elasticsearch -- \
  curl -u elastic:deuknet2024 \
  'http://localhost:9200/_cat/indices/logs-*?v'
```

## 🔐 자격증명

| 사용자 | 비밀번호 | 용도 |
|--------|----------|------|
| elastic | deuknet2024 | 슈퍼관리자 |
| kibana_system | deuknet2024 | Kibana 내부 |
| logstash_writer | deuknet2024 | 로그 쓰기 |
| beats_system | deuknet2024 | Beats 모니터링 |

## 📖 가이드

- [전체 아키텍처 설명](./ELK_ARCHITECTURE.md)
- [Filebeat 테스트](./test-filebeat.ps1)
- [로그 생성기](./test-log-generator.yaml)

## 🎨 Kibana 사용법

1. **Data View 생성**
   - Management → Stack Management → Kibana → Data Views
   - Pattern: `logs-*`
   - Timestamp field: `@timestamp`

2. **로그 검색**
   - Analytics → Discover
   - KQL: `kubernetes.pod.name: "myapp-*" AND message: "error"`

3. **대시보드 생성**
   - Analytics → Dashboard → Create dashboard
   - Visualizations 추가

## 🔍 트러블슈팅

### 로그가 안 보일 때

```powershell
# 1. Filebeat 확인
kubectl logs daemonset/filebeat

# 2. Logstash 확인
kubectl logs deployment/logstash

# 3. Elasticsearch 확인
kubectl exec deployment/elasticsearch -- \
  curl -u elastic:deuknet2024 \
  'http://localhost:9200/_cat/indices/logs-*?v'

# 4. Pod 라벨 확인
kubectl get pods --show-labels | grep collect-logs
```

### 일반적인 문제

| 문제 | 원인 | 해결 |
|------|------|------|
| 401 에러 | 인증 실패 | Secret 확인 |
| 403 에러 | 권한 부족 | setup-job 재실행 |
| 로그 누락 | 라벨 없음 | collect-logs: true 추가 |
| 디스크 풀 | 인덱스 과다 | 오래된 인덱스 삭제 |

## 🚀 운영 명령어

```powershell
# 전체 상태 확인
kubectl get pods

# 로그 실시간 확인
kubectl logs -f deployment/logstash

# 인덱스 삭제 (90일 이상)
kubectl exec deployment/elasticsearch -- \
  curl -X DELETE -u elastic:deuknet2024 \
  'http://localhost:9200/logs-2025.01.*'

# 재배포
kubectl delete pod -l app=logstash
kubectl delete pod -l app=filebeat
```

## 📊 모니터링

### 주요 메트릭

- Filebeat: 수집률, 실패율
- Logstash: 처리량, 큐 크기
- Elasticsearch: 인덱싱 속도, 디스크 사용량
- Kibana: 응답 시간

### 헬스 체크

```powershell
# Elasticsearch 클러스터 상태
kubectl exec deployment/elasticsearch -- \
  curl -u elastic:deuknet2024 \
  http://localhost:9200/_cluster/health?pretty
```

## 🎓 학습 자료

- [Elasticsearch 가이드](https://www.elastic.co/guide/en/elasticsearch/)
- [Logstash 가이드](https://www.elastic.co/guide/en/logstash/)
- [Filebeat 가이드](https://www.elastic.co/guide/en/beats/filebeat/)
- [Kibana 가이드](https://www.elastic.co/guide/en/kibana/)

## 🤝 기여

문제가 발생하거나 개선 사항이 있으면 이슈를 생성해주세요.

---

**프로젝트**: DeukNet
**버전**: 1.0.0
**업데이트**: 2025-10-15
