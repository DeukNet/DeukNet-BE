# Windows 용 Minikube 통합 배포 스크립트 (개선판)
$ErrorActionPreference = "Stop"

Write-Host "`n[Step 0] Switching to Minikube Docker context..." -ForegroundColor Cyan
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

# ================================================
# 1️⃣ Docker 이미지 빌드
Write-Host "`n[Step 1] Building Docker images inside Minikube..." -ForegroundColor Cyan

# 이미지 빌드 전 파일 존재 확인
$dockerfiles = @(
    ".\k8s\elasticsearch\Dockerfile.elasticsearch",
    ".\k8s\logstash\Dockerfile.logstash",
    ".\k8s\kibana\Dockerfile.kibana",
    ".\k8s\filebeat\Dockerfile.filebeat"
)

foreach ($df in $dockerfiles) {
    if (-not (Test-Path $df)) {
        Write-Host "❌ Dockerfile not found: $df" -ForegroundColor Red
        exit 1
    }
}

docker build -t elasticsearch:local .\k8s\elasticsearch -f .\k8s\elasticsearch\Dockerfile.elasticsearch
docker build -t logstash:local .\k8s\logstash -f .\k8s\logstash\Dockerfile.logstash
docker build -t kibana:local .\k8s\kibana -f .\k8s\kibana\Dockerfile.kibana
docker build -t filebeat:local .\k8s\filebeat -f .\k8s\filebeat\Dockerfile.filebeat

# Spring Boot 애플리케이션 빌드
Write-Host "`n[Step 1-1] Building Spring Boot JAR..." -ForegroundColor Cyan
./gradlew clean bootJar --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Gradle build failed" -ForegroundColor Red
    exit 1
}

Write-Host "`n[Step 1-2] Building application Docker image..." -ForegroundColor Cyan
docker build -t myapp:local -f .\Dockerfile .

# PostgreSQL 이미지
docker pull postgres:15-alpine

# ================================================
# 2️⃣ Namespace 생성 (선택사항)
kubectl create namespace elk-stack --dry-run=client -o yaml | kubectl apply -f -

# ================================================
# 3️⃣ Secret 생성 (Elasticsearch 인증용)
Write-Host "`n[Step 2-1] Creating Elasticsearch secrets..." -ForegroundColor Cyan
kubectl create secret generic elasticsearch-credentials `
    --from-literal=elastic-password=changeme `
    --from-literal=kibana-system-password=changeme `
    --dry-run=client -o yaml | kubectl apply -f -

# ================================================
# 4️⃣ 순차적 배포 및 대기
Write-Host "`n[Step 3] Deploying services in order..." -ForegroundColor Cyan

function Wait-ForDeployment {
    param($name, $timeout = 120)

    $elapsed = 0
    while ($elapsed -lt $timeout) {
        $ready = kubectl get deployment $name -o jsonpath='{.status.readyReplicas}' 2>$null
        $desired = kubectl get deployment $name -o jsonpath='{.spec.replicas}' 2>$null

        if ($ready -eq $desired -and $desired -gt 0) {
            Write-Host "✅ $name is ready ($ready/$desired replicas)" -ForegroundColor Green
            return $true
        }

        Write-Host "⏳ Waiting for $name... ($ready/$desired ready)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
        $elapsed += 5
    }

    Write-Host "❌ Timeout waiting for $name" -ForegroundColor Red
    return $false
}

# PostgreSQL
Write-Host "`n→ Deploying PostgreSQL..." -ForegroundColor Yellow
kubectl apply -f .\k8s\postgres\deployment.yaml
kubectl apply -f .\k8s\postgres\service.yaml
if (-not (Wait-ForDeployment "postgres")) { exit 1 }

# Elasticsearch
Write-Host "`n→ Deploying Elasticsearch..." -ForegroundColor Yellow
kubectl apply -f .\k8s\elasticsearch\deployment.yaml
kubectl apply -f .\k8s\elasticsearch\service.yaml
if (-not (Wait-ForDeployment "elasticsearch")) { exit 1 }

# Elasticsearch 헬스체크
Write-Host "Checking Elasticsearch health..." -ForegroundColor Yellow
Start-Sleep -Seconds 10  # 초기화 대기

# Logstash
Write-Host "`n→ Deploying Logstash..." -ForegroundColor Yellow
if (Test-Path ".\k8s\logstash\pipeline\logstash.conf") {
    kubectl create configmap logstash-pipeline `
        --from-file=.\k8s\logstash\pipeline\logstash.conf `
        --dry-run=client -o yaml | kubectl apply -f -
} else {
    Write-Host "⚠️ logstash.conf not found, skipping ConfigMap" -ForegroundColor Yellow
}
kubectl apply -f .\k8s\logstash\deployment.yaml
kubectl apply -f .\k8s\logstash\service.yaml
if (-not (Wait-ForDeployment "logstash")) { exit 1 }

# Kibana
Write-Host "`n→ Deploying Kibana..." -ForegroundColor Yellow
kubectl apply -f .\k8s\kibana\deployment.yaml
kubectl apply -f .\k8s\kibana\service.yaml
if (-not (Wait-ForDeployment "kibana")) { exit 1 }

# Filebeat
Write-Host "`n→ Deploying Filebeat..." -ForegroundColor Yellow
if (Test-Path ".\k8s\filebeat\filebeat.yml") {
    kubectl create configmap filebeat-config `
        --from-file=.\k8s\filebeat\filebeat.yml `
        --dry-run=client -o yaml | kubectl apply -f -
}
kubectl apply -f .\k8s\filebeat\daemonset.yaml

# Application
Write-Host "`n→ Deploying Application..." -ForegroundColor Yellow
kubectl apply -f .\k8s\app\deployment.yaml
kubectl apply -f .\k8s\app\service.yaml
if (-not (Wait-ForDeployment "myapp")) { exit 1 }

# ================================================
# 5️⃣ 포트포워딩
Write-Host "`n[Step 4] Starting port-forwards..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "kubectl port-forward deployment/kibana 5601:5601" -WindowStyle Hidden
Write-Host "✅ Kibana available at http://localhost:5601" -ForegroundColor Green

# ================================================
# 6️⃣ 최종 상태 확인
Write-Host "`n[Step 5] Final status check:" -ForegroundColor Cyan
kubectl get pods -o wide
Write-Host ""
kubectl get svc -o wide

# ================================================
# 7️⃣ 문제 해결 팁
Write-Host "`n📝 Troubleshooting tips:" -ForegroundColor Magenta
Write-Host "  - Check logs: kubectl logs deployment/kibana" -ForegroundColor Gray
Write-Host "  - Check ES: kubectl logs deployment/elasticsearch" -ForegroundColor Gray
Write-Host "  - Port forward ES: kubectl port-forward deployment/elasticsearch 9200:9200" -ForegroundColor Gray