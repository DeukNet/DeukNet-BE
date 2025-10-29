#!/bin/bash
eval $(minikube docker-env)
docker build -t localhost:5000/deuknet-app:latest .
