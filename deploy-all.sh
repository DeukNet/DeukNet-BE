#!/bin/bash

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}======================================"
echo "DeukNet 전체 배포 스크립트"
echo -e "======================================${NC}"
echo ""

# Minikube 확인
if ! command -v minikube &> /dev/null; then
    echo -e "${RED}Error: minikube가 설치되어 있지 않습니다.${NC}"
    exit 1
fi

# Minikube 상태 확인
if ! minikube status &> /dev/null; then
    echo -e "${YELLOW}Minikube가 실행 중이 아닙니다. 시작합니다...${NC}"
    minikube start
fi

echo -e "${GREEN}✓ Minikube 실행 중${NC}"
echo ""

# Docker 환경 설정
echo -e "${YELLOW}[1/3] Docker 환경 설정...${NC}"
eval $(minikube docker-env)
echo -e "${GREEN}✓ Minikube Docker 환경으로 전환${NC}"
echo ""

# 애플리케이션 빌드
echo -e "${YELLOW}[2/3] 애플리케이션 빌드 및 Docker 이미지 생성...${NC}"
./docker-build-push.sh

if [ $? -ne 0 ]; then
    echo -e "${RED}Error: 애플리케이션 빌드 실패${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 애플리케이션 빌드 완료${NC}"
echo ""

# Helm 배포
echo -e "${YELLOW}[3/3] Helm Chart 배포...${NC}"

# 기존 배포 확인
if helm list | grep -q deuknet-cdc; then
    echo -e "${YELLOW}기존 배포를 발견했습니다. 업그레이드합니다...${NC}"
    helm upgrade deuknet-cdc ./helm/deuknet-cdc
else
    echo -e "${YELLOW}새로운 배포를 시작합니다...${NC}"
    helm install deuknet-cdc ./helm/deuknet-cdc
fi

echo -e "${GREEN}✓ Helm Chart 배포 완료${NC}"
echo ""

# 배포 상태 확인
echo -e "${BLUE}======================================"
echo "배포 상태"
echo -e "======================================${NC}"
kubectl get pods
echo ""
kubectl get svc
echo ""

echo -e "${GREEN}======================================"
echo "배포 완료!"
echo -e "======================================${NC}"
echo ""
echo -e "${YELLOW}다음 단계:${NC}"
echo "  1. Pod 상태 확인: kubectl get pods -w"
echo "  2. Connector 상태 확인: kubectl logs job/register-connectors"
echo "  3. 애플리케이션 접속: kubectl port-forward svc/deuknet-app 8080:8080"
echo ""
echo -e "${YELLOW}유용한 명령어:${NC}"
echo "  - 로그 확인: kubectl logs -f deployment/deuknet-app"
echo "  - Pod 재시작: kubectl rollout restart deployment/deuknet-app"
echo "  - 전체 삭제: helm uninstall deuknet-cdc"
echo ""
