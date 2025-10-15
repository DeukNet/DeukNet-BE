# Windows용 Minikube 통합 배포 스크립트 (SSL + 고정 비밀번호)
$ErrorActionPreference = "Stop"

Write-Host "`n=== DeukNet Minikube Deployment ===" -ForegroundColor Green
Write-Host ""

# Minikube Docker context 전환
Write-Host "[Step 0] Switching to Minikube Docker context..." -ForegroundColor Cyan
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

# ================================================
# 1️⃣ Spring Boot 애플리케이션 빌드
Write-Host "`n[Step 1] Building Spring Boot application..." -ForegroundColor Cyan
.\gradlew clean bootJar --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Gradle build failed" -ForegroundColor Red
    exit 1
}

Write-Host "`n[Step 2] Building application Docker image..." -ForegroundColor Cyan
docker build -t myapp:local -f .\Dockerfile .

# PostgreSQL 이미지 pull
docker pull postgres:15-alpine

# Elastic Stack 공식 이미지 pull
Write-Host "`n[Step 3] Pulling Elastic Stack images..." -ForegroundColor Cyan
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.11.0
docker pull docker.elastic.co/kibana/kibana:8.11.0
docker pull docker.elastic.co/logstash/logstash:8.11.0
docker pull docker.elastic.co/beats/filebeat:8.11.0

# ================================================
# 2️⃣ 배포 시작
Write-Host "`n[Step 4] Creating Kubernetes resources..." -ForegroundColor Cyan

# Helper function: Wait for deployment
function Wait-ForDeployment {
    param($name, $timeout = 180)
    
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
    kubectl logs deployment/$name --tail=50
    return $false
}

# Step 4-1: PostgreSQL
Write-Host "`n→ Deploying PostgreSQL..." -ForegroundColor Yellow
kubectl apply -f .\k8s\postgres\deployment.yaml
kubectl apply -f .\k8s\postgres\service.yaml
if (-not (Wait-ForDeployment "postgres" 120)) { exit 1 }

# Step 4-2: Elasticsearch credentials
Write-Host "`n→ Creating Elasticsearch credentials..." -ForegroundColor Yellow
kubectl apply -f .\k8s\elasticsearch\secret.yaml
kubectl apply -f .\k8s\elasticsearch\configmap.yaml

# Step 4-3: Elasticsearch
Write-Host "`n→ Deploying Elasticsearch with SSL..." -ForegroundColor Yellow
kubectl apply -f .\k8s\elasticsearch\deployment.yaml
kubectl apply -f .\k8s\elasticsearch\service.yaml
if (-not (Wait-ForDeployment "elasticsearch" 240)) { exit 1 }

Write-Host "`nWaiting for Elasticsearch to initialize (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Step 4-4: Elasticsearch setup job
Write-Host "`n→ Running Elasticsearch setup job..." -ForegroundColor Yellow
kubectl delete job elasticsearch-setup --ignore-not-found
kubectl apply -f .\k8s\elasticsearch\setup-job.yaml

$jobTimeout = 120
$elapsed = 0
while ($elapsed -lt $jobTimeout) {
    $jobStatus = kubectl get job elasticsearch-setup -o json 2>$null | ConvertFrom-Json
    
    if ($jobStatus.status.succeeded -eq 1) {
        Write-Host "✅ Elasticsearch setup completed" -ForegroundColor Green
        break
    }
    
    if ($jobStatus.status.failed -gt 0) {
        Write-Host "❌ Elasticsearch setup failed" -ForegroundColor Red
        kubectl logs job/elasticsearch-setup
        exit 1
    }
    
    Write-Host "⏳ Waiting for setup job to complete..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
    $elapsed += 5
}

if ($elapsed -ge $jobTimeout) {
    Write-Host "❌ Setup job timeout" -ForegroundColor Red
    kubectl logs job/elasticsearch-setup
    exit 1
}

# Step 4-5: Logstash
Write-Host "`n→ Deploying Logstash..." -ForegroundColor Yellow
kubectl create configmap logstash-pipeline `
    --from-file=.\k8s\logstash\pipeline\logstash.conf `
    --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f .\k8s\logstash\deployment.yaml
kubectl apply -f .\k8s\logstash\service.yaml
if (-not (Wait-ForDeployment "logstash" 180)) { exit 1 }

# Step 4-6: Kibana
Write-Host "`n→ Deploying Kibana..." -ForegroundColor Yellow
kubectl apply -f .\k8s\kibana\deployment.yaml
kubectl apply -f .\k8s\kibana\service.yaml
if (-not (Wait-ForDeployment "kibana" 180)) { exit 1 }

# Step 4-7: Filebeat
Write-Host "`n→ Deploying Filebeat..." -ForegroundColor Yellow

# Create ServiceAccount and RBAC
@"
apiVersion: v1
kind: ServiceAccount
metadata:
  name: filebeat
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: filebeat
rules:
- apiGroups: [""]
  resources:
  - namespaces
  - pods
  - nodes
  verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: filebeat
subjects:
- kind: ServiceAccount
  name: filebeat
  namespace: default
roleRef:
  kind: ClusterRole
  name: filebeat
  apiGroup: rbac.authorization.k8s.io
"@ | kubectl apply -f -

kubectl create configmap filebeat-config `
    --from-file=.\k8s\filebeat\filebeat.yml `
    --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f .\k8s\filebeat\daemonset.yaml

Write-Host "✅ Filebeat DaemonSet deployed" -ForegroundColor Green

# Step 4-8: Application
Write-Host "`n→ Deploying Application..." -ForegroundColor Yellow
kubectl apply -f .\k8s\app\deployment.yaml
kubectl apply -f .\k8s\app\service.yaml
if (-not (Wait-ForDeployment "myapp" 120)) { exit 1 }

# ================================================
# 3️⃣ 서비스 상태 확인
Write-Host "`n[Step 5] Checking services status..." -ForegroundColor Cyan
kubectl get pods -o wide
Write-Host ""
kubectl get svc -o wide

# ================================================
# 4️⃣ 접속 정보 출력
Write-Host "`n=== Deployment Complete! ===" -ForegroundColor Green
Write-Host ""
Write-Host "🔐 Fixed Credentials (Development Only):" -ForegroundColor Yellow
Write-Host "  Elasticsearch Admin:" -ForegroundColor White
Write-Host "    Username: elastic" -ForegroundColor Gray
Write-Host "    Password: deuknet2024" -ForegroundColor Gray
Write-Host ""
Write-Host "  Kibana:" -ForegroundColor White
Write-Host "    Username: kibana_system" -ForegroundColor Gray
Write-Host "    Password: deuknet2024" -ForegroundColor Gray
Write-Host ""
Write-Host "  Logstash (Data Writer):" -ForegroundColor White
Write-Host "    Username: logstash_writer" -ForegroundColor Gray
Write-Host "    Password: deuknet2024" -ForegroundColor Gray
Write-Host ""
Write-Host "  Filebeat:" -ForegroundColor White
Write-Host "    Username: beats_system" -ForegroundColor Gray
Write-Host "    Password: deuknet2024" -ForegroundColor Gray
Write-Host ""

# Minikube service URLs 가져오기
Write-Host "📍 Service URLs:" -ForegroundColor Yellow
$esUrl = minikube service elasticsearch --url 2>$null
$kibanaUrl = minikube service kibana --url 2>$null
$appUrl = minikube service myapp --url 2>$null

if ($esUrl) {
    Write-Host "  Elasticsearch: $esUrl (HTTPS)" -ForegroundColor White
} else {
    Write-Host "  Elasticsearch: Use 'minikube service elasticsearch --url'" -ForegroundColor Gray
}

if ($kibanaUrl) {
    Write-Host "  Kibana: $kibanaUrl" -ForegroundColor White
} else {
    Write-Host "  Kibana: Use 'minikube service kibana --url'" -ForegroundColor Gray
}

if ($appUrl) {
    Write-Host "  Application: $appUrl" -ForegroundColor White
} else {
    Write-Host "  Application: Use 'minikube service myapp --url'" -ForegroundColor Gray
}

Write-Host ""
Write-Host "💡 Useful commands:" -ForegroundColor Cyan
Write-Host "  View logs: kubectl logs -f deployment/<name>" -ForegroundColor Gray
Write-Host "  Get pods: kubectl get pods" -ForegroundColor Gray
Write-Host "  Port forward: kubectl port-forward deployment/<name> <local>:<remote>" -ForegroundColor Gray
Write-Host "  Shell access: kubectl exec -it deployment/<name> -- bash" -ForegroundColor Gray
Write-Host ""
Write-Host "⚠️  Note: SSL verification is disabled for development" -ForegroundColor Yellow
Write-Host ""
