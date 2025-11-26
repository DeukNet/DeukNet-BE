#!/bin/bash

set -e

echo "🚀 DeukNet 배포 스크립트"
echo "=========================="
echo

# 컬러 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Minikube 실행 확인
if ! minikube status > /dev/null 2>&1; then
    echo -e "${RED}❌ Minikube가 실행되지 않았습니다.${NC}"
    echo "minikube start 명령으로 Minikube를 시작하세요."
    exit 1
fi

echo -e "${GREEN}✅ Minikube 실행 중${NC}"
echo

# Docker 환경 설정
echo "🔧 Minikube Docker 환경 설정..."
eval $(minikube docker-env)
echo

# Gradle 빌드
echo "📦 애플리케이션 빌드 중..."
./gradlew clean build -x test
echo

# Docker 이미지 빌드
echo "🐳 Docker 이미지 빌드 중..."
docker build -t deuknet-app:latest .
echo -e "${GREEN}✅ Docker 이미지 빌드 완료${NC}"
echo

# 기존 배포 확인 및 삭제
if helm list | grep -q "deuknet-cdc"; then
    echo "🗑️  기존 배포 제거 중..."
    echo "⏳ Pod 종료 대기 중..."
    kubectl wait --for=delete pod --all --timeout=60s 2>/dev/null || true
    echo
fi

# Helm 차트 설치
echo "📊 Helm 차트 설치 중..."
helm install deuknet-cdc ./helm/deuknet-cdc
echo -e "${GREEN}✅ Helm 차트 설치 완료${NC}"
echo

# Pod 준비 대기
echo "⏳ Pod 준비 대기 중..."
echo "   (PostgreSQL, Kafka, Elasticsearch 등이 시작됩니다...)"
kubectl wait --for=condition=ready pod -l app=postgres --timeout=120s
kubectl wait --for=condition=ready pod -l app=kafka --timeout=120s
kubectl wait --for=condition=ready pod -l app=elasticsearch --timeout=120s
echo -e "${GREEN}✅ 인프라 Pod 준비 완료${NC}"
echo

kubectl wait --for=condition=ready pod -l app=kafka-connect --timeout=120s
echo -e "${GREEN}✅ Kafka Connect 준비 완료${NC}"
echo

kubectl wait --for=condition=ready pod -l app=deuknet-app --timeout=180s
echo -e "${GREEN}✅ 애플리케이션 준비 완료${NC}"
echo

# Connector 등록 Job 대기
echo "⏳ Debezium Connector 등록 대기 중..."
kubectl wait --for=condition=complete job/register-connectors --timeout=120s
echo -e "${GREEN}✅ Connector 등록 완료${NC}"
echo

# 배포 상태 확인
echo "📋 배포 상태:"
echo "============="
kubectl get pods
echo
kubectl get svc
echo

# 접속 정보 출력
echo "🎉 배포 완료!"
echo "============="
echo
echo -e "${YELLOW}📡 애플리케이션 접속 방법:${NC}"
echo "   ./start-app.sh                      # 8080 포트로 자동 접속"
echo "   kubectl port-forward svc/deuknet-app 8080:8080"
echo
echo -e "${YELLOW}🔍 상태 확인 명령어:${NC}"
echo "   kubectl get pods                    # Pod 상태"
echo "   kubectl logs -f <pod-name>          # 로그 확인"
echo "   kubectl exec -it <pod-name> -- bash # Pod 접속"
echo
echo -e "${YELLOW}🗑️  삭제 명령어:${NC}"
echo "   helm uninstall deuknet-cdc"
echo
echo -e "${GREEN}✨ DeukNet이 성공적으로 배포되었습니다!${NC}"
echo

# 사용자에게 Port Forward 실행 여부 묻기
read -p "지금 바로 애플리케이션을 8080 포트로 여시겠습니까? (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo
    echo -e "${GREEN}🚀 Port Forward 시작...${NC}"
    ./start-app.sh
fi
