#!/bin/bash

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 설정
IMAGE_NAME="deuknet-app"
REGISTRY="${DOCKER_REGISTRY:-localhost:5000}"  # 기본값: localhost:5000
TAG="${VERSION:-latest}"
FULL_IMAGE="${REGISTRY}/${IMAGE_NAME}:${TAG}"

echo -e "${GREEN}======================================"
echo "DeukNet 애플리케이션 빌드 및 푸시"
echo -e "======================================${NC}"
echo "Image: ${FULL_IMAGE}"
echo ""

# 1. Gradle 빌드
echo -e "${YELLOW}[1/4] Gradle 빌드 시작...${NC}"
./gradlew clean :deuknet-infrastructure:build -x test

if [ ! -f "deuknet-infrastructure/build/libs"/*.jar ]; then
    echo -e "${RED}Error: JAR 파일을 찾을 수 없습니다.${NC}"
    exit 1
fi

JAR_FILE=$(ls deuknet-infrastructure/build/libs/*.jar | head -n 1)
echo -e "${GREEN}✓ JAR 빌드 완료: ${JAR_FILE}${NC}"
echo ""

# 2. Docker 이미지 빌드
echo -e "${YELLOW}[2/4] Docker 이미지 빌드 중...${NC}"
docker build -t ${FULL_IMAGE} .

if [ $? -ne 0 ]; then
    echo -e "${RED}Error: Docker 이미지 빌드 실패${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker 이미지 빌드 완료${NC}"
echo ""

# 3. Docker 이미지 푸시
echo -e "${YELLOW}[3/4] Docker 이미지 푸시 중...${NC}"
docker push ${FULL_IMAGE}

if [ $? -ne 0 ]; then
    echo -e "${RED}Error: Docker 이미지 푸시 실패${NC}"
    echo -e "${YELLOW}Registry에 접근할 수 없습니다. 로컬에서 테스트하려면:${NC}"
    echo "  minikube start"
    echo "  eval \$(minikube docker-env)"
    echo "  ./docker-build-push.sh"
    exit 1
fi

echo -e "${GREEN}✓ Docker 이미지 푸시 완료${NC}"
echo ""

# 4. 이미지 정보 출력
echo -e "${YELLOW}[4/4] 빌드 완료!${NC}"
echo -e "${GREEN}======================================"
echo "이미지: ${FULL_IMAGE}"
echo "크기: $(docker images ${FULL_IMAGE} --format "{{.Size}}")"
echo -e "======================================${NC}"
echo ""
echo "다음 단계:"
echo "  helm upgrade --install deuknet-cdc ./helm/deuknet-cdc --set app.image.repository=${REGISTRY}/${IMAGE_NAME} --set app.image.tag=${TAG}"
echo ""
