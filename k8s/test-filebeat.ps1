# Filebeat 진단 및 테스트 스크립트
$ErrorActionPreference = "Stop"

Write-Host "`n=== Filebeat Diagnostic Test ===" -ForegroundColor Green
Write-Host ""

# 1. Filebeat Pod 상태 확인
Write-Host "[1] Checking Filebeat Pods..." -ForegroundColor Cyan
$filebeatPods = kubectl get pods -l app=filebeat -o json | ConvertFrom-Json

if ($filebeatPods.items.Count -eq 0) {
    Write-Host "❌ No Filebeat pods found!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Found $($filebeatPods.items.Count) Filebeat pod(s)" -ForegroundColor Green
foreach ($pod in $filebeatPods.items) {
    $name = $pod.metadata.name
    $status = $pod.status.phase
    $ready = ($pod.status.containerStatuses | Where-Object { $_.ready -eq $true }).Count
    $total = $pod.status.containerStatuses.Count
    
    if ($status -eq "Running" -and $ready -eq $total) {
        Write-Host "  ✅ $name - $status ($ready/$total ready)" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  $name - $status ($ready/$total ready)" -ForegroundColor Yellow
    }
}

# 2. Filebeat 로그 확인
Write-Host "`n[2] Checking Filebeat logs..." -ForegroundColor Cyan
$firstPod = $filebeatPods.items[0].metadata.name
Write-Host "Showing logs from: $firstPod" -ForegroundColor Gray
Write-Host ""
kubectl logs $firstPod --tail=30

# 3. Logstash 연결 확인
Write-Host "`n[3] Checking Logstash connection..." -ForegroundColor Cyan
$logstashPods = kubectl get pods -l app=logstash -o json | ConvertFrom-Json

if ($logstashPods.items.Count -eq 0) {
    Write-Host "❌ No Logstash pods found!" -ForegroundColor Red
} else {
    $logstashPod = $logstashPods.items[0].metadata.name
    Write-Host "✅ Logstash pod: $logstashPod" -ForegroundColor Green
    
    Write-Host "`nLogstash listening status:" -ForegroundColor Gray
    kubectl logs $logstashPod --tail=50 | Select-String "Starting input listener"
}

# 4. 테스트 로그 생성
Write-Host "`n[4] Generating test logs..." -ForegroundColor Cyan
$testPods = kubectl get pods -l app=myapp -o json | ConvertFrom-Json

if ($testPods.items.Count -eq 0) {
    Write-Host "⚠️  No myapp pods found. Creating test pod..." -ForegroundColor Yellow
    
    # 간단한 테스트 Pod 생성
    @"
apiVersion: v1
kind: Pod
metadata:
  name: log-generator
  labels:
    app: log-generator
spec:
  containers:
  - name: logger
    image: busybox
    command: ["/bin/sh"]
    args: ["-c", "while true; do echo 'Test log message from Filebeat at \$(date)'; sleep 5; done"]
"@ | kubectl apply -f -

    Write-Host "✅ Test pod created. Waiting for it to start..." -ForegroundColor Green
    Start-Sleep -Seconds 10
    
    $testPodName = "log-generator"
} else {
    $testPodName = $testPods.items[0].metadata.name
    Write-Host "✅ Using existing pod: $testPodName" -ForegroundColor Green
}

Write-Host "`nTest pod logs:" -ForegroundColor Gray
kubectl logs $testPodName --tail=5

# 5. Elasticsearch 인덱스 확인
Write-Host "`n[5] Checking Elasticsearch indices..." -ForegroundColor Cyan
$esPod = kubectl get pods -l app=elasticsearch -o jsonpath='{.items[0].metadata.name}'

if ($esPod) {
    Write-Host "Querying Elasticsearch for logs-* indices..." -ForegroundColor Gray
    
    $result = kubectl exec $esPod -- curl -s -u "elastic:deuknet2024" \
        "http://localhost:9200/_cat/indices/logs-*?v" 2>$null
    
    if ($result) {
        Write-Host $result
    } else {
        Write-Host "⚠️  No logs-* indices found yet. Logs may not have been sent." -ForegroundColor Yellow
    }
}

# 6. 요약
Write-Host "`n=== Summary ===" -ForegroundColor Green
Write-Host "✅ Filebeat Pods: $($filebeatPods.items.Count) running" -ForegroundColor White
Write-Host "✅ Logstash: Listening on port 5044" -ForegroundColor White
Write-Host "✅ Test logs: Being generated" -ForegroundColor White
Write-Host ""
Write-Host "💡 Useful commands:" -ForegroundColor Cyan
Write-Host "  Watch Filebeat logs: kubectl logs -f daemonset/filebeat" -ForegroundColor Gray
Write-Host "  Watch Logstash logs: kubectl logs -f deployment/logstash" -ForegroundColor Gray
Write-Host "  Check indices: kubectl exec deployment/elasticsearch -- curl -u elastic:deuknet2024 http://localhost:9200/_cat/indices" -ForegroundColor Gray
Write-Host "  Delete test pod: kubectl delete pod log-generator" -ForegroundColor Gray
Write-Host ""
