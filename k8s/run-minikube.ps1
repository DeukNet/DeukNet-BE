## Windows 용 Minikube 통합 배포 스크립트
$ErrorActionPreference = "Stop"

Write-Host "`n[Step 0] Switching to Minikube Docker context..." -ForegroundColor Cyan
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

# ================================================
# 1️⃣ Docker 이미지 빌드
Write-Host "`n[Step 1] Building Docker images inside Minikube..." -ForegroundColor Cyan

docker build -t elasticsearch:local .\k8s\elasticsearch -f .\k8s\elasticsearch\Dockerfile.elasticsearch
docker build -t logstash:local .\k8s\logstash -f .\k8s\logstash\Dockerfile.logstash
docker build -t kibana:local .\k8s\kibana -f .\k8s\kibana\Dockerfile.kibana
docker build -t filebeat:local .\k8s\filebeat -f .\k8s\filebeat\Dockerfile.filebeat

# Spring Boot 애플리케이션 빌드
Write-Host "`n[Step 1-1] Building Spring Boot JAR..." -ForegroundColor Cyan
./gradlew clean bootJar --no-daemon

Write-Host "`n[Step 1-2] Building application Docker image..." -ForegroundColor Cyan
docker build -t myapp:local -f .\Dockerfile .

# PostgreSQL 이미지는 Docker Hub 사용 (alpine)
docker pull postgres:15-alpine

# ================================================
# 2️⃣ Kubernetes 리소스 적용

Write-Host "`n[Step 2] Applying Kubernetes manifests..." -ForegroundColor Cyan

# PostgreSQL (Deployment + Service)
kubectl apply -f .\k8s\postgres\deployment.yaml
kubectl apply -f .\k8s\postgres\service.yaml

# Elasticsearch
kubectl apply -f .\k8s\elasticsearch\deployment.yaml
kubectl apply -f .\k8s\elasticsearch\service.yaml

# Logstash (ConfigMap + Deployment + Service)
kubectl create configmap logstash-pipeline `
    --from-file=.\k8s\logstash\pipeline\logstash.conf `
    --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f .\k8s\logstash\deployment.yaml
kubectl apply -f .\k8s\logstash\service.yaml

# Kibana
kubectl apply -f .\k8s\kibana\deployment.yaml
kubectl apply -f .\k8s\kibana\service.yaml

# Filebeat (ConfigMap + DaemonSet)
kubectl create configmap filebeat-config `
    --from-file=.\k8s\filebeat\filebeat.yml `
    --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f .\k8s\filebeat\daemonset.yaml

# Spring Boot 애플리케이션 (Deployment + Service)
kubectl apply -f .\k8s\app\deployment.yaml
kubectl apply -f .\k8s\app\service.yaml

# ================================================
# 3️⃣ Pod Ready 대기
Write-Host "`n[Step 3] Waiting for Pods to be Ready..." -ForegroundColor Cyan
$allReady = $false

for ($i = 0; $i -lt 30; $i++) {
    $pods = kubectl get pods --no-headers
    if ($pods -match "Running" -and $pods -notmatch "0/") {
        Write-Host "✅ All pods are running!" -ForegroundColor Green
        $allReady = $true
        break
    } else {
        Write-Host "⏳ Waiting for pods to be ready... (Attempt $($i+1)/30)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
}

if (-not $allReady) {
    Write-Host "❌ Some pods did not become ready in time." -ForegroundColor Red
    kubectl get pods -o wide
    exit 1
}

# ================================================
# 4️⃣ Kibana 포트포워딩
Write-Host "`n[Step 4] Starting Kibana port-forward..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "kubectl port-forward deployment/kibana 5601:5601"

# ================================================
# 5️⃣ Pod 상태 출력
Write-Host "`n[Step 5] Pods status:" -ForegroundColor Cyan
kubectl get pods -o wide

# ================================================
# 6️⃣ 서비스 상태 출력
Write-Host "`n[Step 6] Services status:" -ForegroundColor Cyan
kubectl get svc -o wide
