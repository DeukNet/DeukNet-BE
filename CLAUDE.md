# DeukNet-BE - Development Guide for Claude Code

## Project Overview

DeukNet-BE is a Spring Boot backend service implementing **Hexagonal Architecture** with **CQRS pattern** and **Event Sourcing** using CDC (Change Data Capture).

### Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.3.5
- **Build Tool**: Gradle 8.10.2
- **Write DB**: PostgreSQL 15 (with WAL level=logical for CDC)
- **Read DB**: Elasticsearch 8.11 (with Nori Korean analyzer plugin)
- **File Storage**: MinIO
- **CDC**: Debezium 2.7.3 Embedded Engine
- **Deployment**: Kubernetes + Helm

## Module Structure

This is a multi-module Gradle project with clean dependency flow:

```
deuknet-domain          (Core business logic - no external dependencies)
    ↓
deuknet-application     (Use cases, ports/interfaces)
    ↓
deuknet-infrastructure  (Adapters: DB, Kafka, MinIO, Security, CDC)
    ↓
deuknet-presentation    (REST API, Controllers, DTOs)
```

### Module Responsibilities

- **deuknet-domain**: Aggregates, entities, domain exceptions, pure business rules
- **deuknet-application**: Use case implementations, port interfaces (in/out), application exceptions
- **deuknet-infrastructure**:
  - Persistence adapters (JPA entities, repositories)
  - CDC engine (Debezium Embedded)
  - External service adapters (MinIO, Elasticsearch)
  - Security configuration
- **deuknet-presentation**: REST controllers, request/response DTOs, exception handlers

## Key Architectural Patterns

### 1. CQRS (Command Query Responsibility Segregation)
- **Commands (Write)**: PostgreSQL → Domain models → JPA persistence
- **Queries (Read)**: Elasticsearch → Search optimized models
- **Synchronization**: Debezium CDC captures PostgreSQL changes → Kafka → Elasticsearch indexing

### 2. Hexagonal Architecture (Ports & Adapters)
- **Ports**: Interfaces in `deuknet-application` layer
  - `port.in.*UseCase`: Input ports (application entry points)
  - `port.out.*Port`: Output ports (infrastructure abstractions)
- **Adapters**: Implementations in `deuknet-infrastructure`
  - Persistence adapters implement output ports
  - REST controllers in `deuknet-presentation` call input ports

### 3. Outbox Pattern with CDC
- Domain events stored in `outbox` table
- Debezium captures changes from PostgreSQL WAL
- Events published to Kafka topics
- Consumers update Elasticsearch for read models

### 4. Aggregate Design Principles
- **Small aggregates**: Single entity or entity + value objects
- **ID-based references**: Aggregates reference others by ID, not direct object references
- **Clear transaction boundaries**: One aggregate per transaction
- **Eventual consistency**: Between aggregates via domain events

## Common Commands

### Build & Test
```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :deuknet-application:build

# Run tests
./gradlew test

# Run tests for specific module
./gradlew :deuknet-presentation:test

# Skip tests
./gradlew build -x test

# Clean build
./gradlew clean build
```

### Local Development

#### Start External Services (Docker Compose)
```bash
# Start all external dependencies (PostgreSQL, Elasticsearch, MinIO)
docker compose up -d

# View logs
docker compose logs -f

# Stop services
docker compose down

# Stop and remove volumes (fresh start)
docker compose down -v
```

**Important**: The docker-compose setup is for **external services only**. Run the Spring Boot application in your IDE for debugging.

#### PostgreSQL Configuration for CDC
The docker-compose PostgreSQL is pre-configured with:
- `wal_level=logical` (required for Debezium)
- `max_wal_senders=10`
- `max_replication_slots=10`

#### Elasticsearch with Nori Plugin
Uses custom Dockerfile (`docker/Dockerfile.elasticsearch`) that installs the Korean analyzer plugin:
```dockerfile
FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.0
RUN bin/elasticsearch-plugin install analysis-nori
```

### Kubernetes Deployment (Minikube)

```bash
# Start Minikube
minikube start

# Build image for Minikube
./build-minikube-image.sh

# Deploy with Helm
helm install deuknet ./helm/deuknet

# Upgrade deployment
helm upgrade deuknet ./helm/deuknet

# Port forwarding
kubectl port-forward svc/deuknet-app 8080:8080

# View logs
kubectl logs -f deployment/deuknet-app

# Check CDC status
kubectl exec -it deployment/deuknet-app -- curl localhost:8080/actuator/health
```

## Important Development Notes

### File Upload Security
File validation is implemented at the **infrastructure boundary** (adapter layer):

- **MinioFileStorageAdapter** performs:
  - MIME type whitelist validation
  - File extension validation
  - Path traversal attack prevention (blocks `..`, `/`, `\\`)

- **FileUploadService** (application layer) handles:
  - Business rule validation (file size limits: 10MB max)
  - URL generation

**Security principle**: Technical validation at infrastructure layer, business rules at application layer.

### Database Constraints
- **No unique constraint on reactions**: Decided to allow duplicate reactions for simplicity
  - User quote: "같은 계정의 유저가 동시에 좋아요를 요청하는일이 잘 일어나지는 않으니까"
  - Trade-off: Simplicity over strict uniqueness enforcement

### CDC Pipeline Flow
```
PostgreSQL WAL
  → Debezium Embedded Engine (in-process)
  → Debezium Event Handler(in codebase)
  → ProjectionCommenPOrt
  → Elasticsearch Indices (with Nori tokenization)
```

### Exception Handling
- **Domain exceptions**: Extend `DomainException` in `deuknet-domain`
- **Application exceptions**: Extend `ApplicationException` in `deuknet-application`
- **Global handler**: `GlobalExceptionHandler` in `deuknet-presentation` catches all `DeukNetException`

### Dependency Management
- Kafka version **forced to 3.7.0** in root `build.gradle` for Debezium 2.7.3 compatibility
- Elasticsearch requires Nori plugin installation (not available in base image)

## Troubleshooting

### PostgreSQL Replication Slot Error
If Debezium fails with "replication slot already exists":
```bash
docker compose down -v  # Remove volumes to reset WAL
docker compose up -d
```

### Elasticsearch Nori Plugin Not Found
If you see "Unknown tokenizer type [nori_tokenizer]":
- Ensure `docker/Dockerfile.elasticsearch` is present
- Rebuild the image: `docker compose build elasticsearch`
- Restart: `docker compose up -d elasticsearch`

### Minikube Image Not Found
After building the app, images must be loaded into Minikube:
```bash
./build-minikube-image.sh
```

## Development Workflow

1. **Start external services**: `docker compose up -d`
2. **Run Spring Boot app** in IDE with debug mode
3. **Make changes** following hexagonal architecture layers
4. **Write tests** at appropriate layers (unit tests for domain/application, integration tests for infrastructure)
5. **Build**: `./gradlew build`
6. **Deploy to Minikube** (optional): Build image → Helm upgrade

## Key Configuration Files

- `application.yaml`: Spring Boot configuration (DB, Kafka, MinIO connection strings)
- `docker-compose.yaml`: Local development external services
- `helm/deuknet/values.yaml`: Kubernetes deployment configuration
- `build.gradle`: Dependency versions and module configuration

## Further Reading

- Main README.md: Detailed architecture explanation and aggregate design principles
- helm/deuknet/README.md: CDC pipeline details and Kubernetes deployment guide
