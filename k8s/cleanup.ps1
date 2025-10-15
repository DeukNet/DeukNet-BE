#!/usr/bin/env pwsh

Write-Host "=== DeukNet ELK Stack Cleanup ===" -ForegroundColor Yellow
Write-Host ""

Write-Host "Deleting all resources..." -ForegroundColor Cyan

kubectl delete -f filebeat/daemonset.yaml --ignore-not-found
kubectl delete -f kibana/ --ignore-not-found
kubectl delete -f logstash/ --ignore-not-found
kubectl delete job elasticsearch-setup --ignore-not-found
kubectl delete -f elasticsearch/ --ignore-not-found
kubectl delete -f postgres/ --ignore-not-found

kubectl delete configmap logstash-pipeline --ignore-not-found
kubectl delete configmap filebeat-config --ignore-not-found
kubectl delete serviceaccount filebeat --ignore-not-found
kubectl delete clusterrole filebeat --ignore-not-found
kubectl delete clusterrolebinding filebeat --ignore-not-found

Write-Host ""
Write-Host "=== Cleanup Complete! ===" -ForegroundColor Green
