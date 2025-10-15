#!/usr/bin/env pwsh

Write-Host "=== DeukNet ELK Stack Deployment ===" -ForegroundColor Green
Write-Host ""

# Check if kubectl is available
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "Error: kubectl is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

Write-Host "Step 1: Creating Elasticsearch credentials..." -ForegroundColor Cyan
kubectl apply -f elasticsearch/secret.yaml

Write-Host ""
Write-Host "Step 2: Creating Elasticsearch configuration..." -ForegroundColor Cyan
kubectl apply -f elasticsearch/configmap.yaml

Write-Host ""
Write-Host "Step 3: Deploying Elasticsearch..." -ForegroundColor Cyan
kubectl apply -f elasticsearch/deployment.yaml
kubectl apply -f elasticsearch/service.yaml

Write-Host ""
Write-Host "Step 4: Waiting for Elasticsearch to be ready..." -ForegroundColor Cyan
kubectl wait --for=condition=ready pod -l app=elasticsearch --timeout=300s

Write-Host ""
Write-Host "Step 5: Running Elasticsearch setup job..." -ForegroundColor Cyan
kubectl delete job elasticsearch-setup --ignore-not-found
kubectl apply -f elasticsearch/setup-job.yaml
kubectl wait --for=condition=complete job/elasticsearch-setup --timeout=120s

Write-Host ""
Write-Host "Step 6: Creating Logstash pipeline ConfigMap..." -ForegroundColor Cyan
kubectl create configmap logstash-pipeline --from-file=logstash/pipeline/logstash.conf --dry-run=client -o yaml | kubectl apply -f -

Write-Host ""
Write-Host "Step 7: Deploying Logstash..." -ForegroundColor Cyan
kubectl apply -f logstash/deployment.yaml
kubectl apply -f logstash/service.yaml

Write-Host ""
Write-Host "Step 8: Deploying Kibana..." -ForegroundColor Cyan
kubectl apply -f kibana/deployment.yaml
kubectl apply -f kibana/service.yaml

Write-Host ""
Write-Host "Step 9: Creating Filebeat ConfigMap..." -ForegroundColor Cyan
kubectl create configmap filebeat-config --from-file=filebeat/filebeat.yml --dry-run=client -o yaml | kubectl apply -f -

Write-Host ""
Write-Host "Step 10: Creating Filebeat ServiceAccount..." -ForegroundColor Cyan
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

Write-Host ""
Write-Host "Step 11: Deploying Filebeat..." -ForegroundColor Cyan
kubectl apply -f filebeat/daemonset.yaml

Write-Host ""
Write-Host "Step 12: Deploying PostgreSQL..." -ForegroundColor Cyan
kubectl apply -f postgres/deployment.yaml
kubectl apply -f postgres/service.yaml

Write-Host ""
Write-Host "=== Deployment Complete! ===" -ForegroundColor Green
Write-Host ""
Write-Host "Fixed Credentials (Development Only):" -ForegroundColor Yellow
Write-Host "  Username: elastic / kibana_system / logstash_system" -ForegroundColor White
Write-Host "  Password: deuknet2024" -ForegroundColor White
Write-Host ""
Write-Host "Access URLs:" -ForegroundColor Yellow
Write-Host "  Elasticsearch: https://localhost:30920" -ForegroundColor White
Write-Host "  Kibana: http://localhost:30562" -ForegroundColor White
Write-Host ""
Write-Host "Check pod status:" -ForegroundColor Cyan
kubectl get pods
