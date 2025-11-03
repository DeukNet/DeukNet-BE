#!/bin/bash

set -e

echo "🚀 DeukNet 애플리케이션 접속"
echo "============================"
echo

# 컬러 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Pod 상태 확인
echo "📊 Pod 상태 확인 중..."
if ! kubectl get pod -l app=deuknet-app > /dev/null 2>&1; then
    echo -e "${RED}❌ DeukNet 애플리케이션이 배포되지 않았습니다.${NC}"
    echo "먼저 ./deploy-deuknet.sh 를 실행하세요."
    exit 1
fi

# Pod 준비 대기
POD_NAME=$(kubectl get pod -l app=deuknet-app -o jsonpath='{.items[0].metadata.name}')
echo "Pod: $POD_NAME"

if ! kubectl wait --for=condition=ready pod -l app=deuknet-app --timeout=10s > /dev/null 2>&1; then
    echo -e "${YELLOW}⏳ Pod가 준비되지 않았습니다. 대기 중...${NC}"
    kubectl wait --for=condition=ready pod -l app=deuknet-app --timeout=120s
fi

echo -e "${GREEN}✅ Pod 준비 완료${NC}"
echo

# Port Forward 실행
echo -e "${YELLOW}📡 Port Forward 시작 (localhost:8080 → deuknet-app:8080)${NC}"
echo
echo "애플리케이션 URL: http://localhost:8080"
echo "Health Check: http://localhost:8080/actuator/health"
echo
echo -e "${YELLOW}종료하려면 Ctrl+C 를 누르세요${NC}"
echo

kubectl port-forward svc/deuknet-app 8080:8080
