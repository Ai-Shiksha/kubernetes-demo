# Kubernetes Demo - Spring Boot Application

A comprehensive Proof of Concept (POC) demonstrating a Spring Boot microservice deployed on Kubernetes with PostgreSQL, Redis, and RabbitMQ services.

## Overview

This project showcases:
- **Spring Boot 4.0.5** REST API application
- **PostgreSQL** for data persistence
- **Redis** for caching
- **RabbitMQ** for message brokering
- **Kubernetes** deployment and orchestration
- **Docker** containerization
- **Health checks** and monitoring

## Project Structure

```
kubernetes-demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/kubernetesdemo/
│   │   │       ├── KubernetesDemoApplication.java
│   │   │       └── HelloController.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── k8s/
│   ├── namespace.yaml
│   ├── app-deployment.yaml
│   ├── postgres-deployment.yaml
│   ├── redis-deployment.yaml
│   └── rabbitmq-deployment.yaml
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── Jenkinsfile
```

## Quick Start

### Prerequisites
- **Docker Desktop 4.69.0+** (with Kubernetes enabled)
- **kubectl** (usually included with Docker Desktop)
- **Java 17+** (for local development)
- **Maven 3.8+**

### Run with Docker Compose (Local)

```bash
# Navigate to project directory
cd kubernetes-demo

# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f app

# Stop services
docker-compose down
```

Application will be available at: `http://localhost:8080`

### Deploy to Kubernetes

```bash
# 1. Build the Docker image
docker build -t sagarparikh93/kubernetes-demo:0.0.1-SNAPSHOT .

# 2. Push to Docker Hub
docker push sagarparikh93/kubernetes-demo:0.0.1-SNAPSHOT

# 3. Deploy to Kubernetes
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/redis-deployment.yaml
kubectl apply -f k8s/rabbitmq-deployment.yaml
kubectl apply -f k8s/app-deployment.yaml

# 4. Monitor pods
kubectl get pods -n kubernetes-demo -w

# 5. Access the application
kubectl port-forward svc/kubernetes-demo-app 8080:8080 -n kubernetes-demo
```

Application will be available at: `http://localhost:8080`

## Services

### Spring Boot Application
- **Port**: 8080
- **Health Check**: `/actuator/health`
- **API Base**: `/api`

### PostgreSQL
- **Port**: 5432
- **Database**: `kubedb`
- **Username**: `postgres`
- **Password**: `postgres`

### Redis
- **Port**: 6379
- **Type**: Cache

### RabbitMQ
- **AMQP Port**: 5672
- **Management UI**: 15672
- **Username**: `guest`
- **Password**: `guest`
- **Access UI**: `http://localhost:15672`

## Documentation

- [Setup & Prerequisites](./docs/SETUP.md)
- [Deployment Guide](./docs/DEPLOYMENT.md)
- [Architecture](./docs/ARCHITECTURE.md)
- [API Documentation](./docs/API.md)
- [Troubleshooting](./docs/TROUBLESHOOTING.md)

## Key Features

✅ Multi-tier application with database, cache, and message queue  
✅ Kubernetes deployment manifests  
✅ Health checks and readiness probes  
✅ Docker Compose for local development  
✅ Spring Boot Actuator integration  
✅ ConfigMap and Secrets for configuration  
✅ Service discovery within Kubernetes  

## Build & Development

### Build with Maven
```bash
mvn clean package
```

### Build Docker Image
```bash
docker build -t kubernetes-demo:0.0.1-SNAPSHOT .
```

### Run Locally
```bash
java -jar target/kubernetes-demo-0.0.1-SNAPSHOT.jar
```

## Kubernetes Namespace

All resources are deployed in the `kubernetes-demo` namespace. To switch to this namespace:

```bash
kubectl config set-context --current --namespace=kubernetes-demo
```

## Monitoring

### View Logs
```bash
# Application logs
kubectl logs -f deployment/kubernetes-demo-app -n kubernetes-demo

# Specific pod logs
kubectl logs -f pod/POD_NAME -n kubernetes-demo
```

### Get Resource Status
```bash
kubectl get all -n kubernetes-demo
kubectl describe deployment kubernetes-demo-app -n kubernetes-demo
```

### Connect to Pod Shell
```bash
kubectl exec -it POD_NAME -n kubernetes-demo -- /bin/sh
```

## Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 4.0.5 | Web Framework |
| Java | 17 | Runtime |
| PostgreSQL | 16-alpine | Database |
| Redis | 7-alpine | Cache |
| RabbitMQ | 3.12-management | Message Queue |
| Kubernetes | Latest | Orchestration |
| Docker | 4.69.0+ | Containerization |

## Cleanup

### Remove from Kubernetes
```bash
kubectl delete namespace kubernetes-demo
```

### Stop Docker Compose
```bash
docker-compose down -v
```

## License

MIT License

## Author

Sagar Parikh

## Notes

This is a POC project for learning Kubernetes deployment patterns. For production use, consider:
- Using managed Kubernetes services (EKS, GKE, AKS)
- Implementing proper secret management
- Setting up proper logging and monitoring
- Using stateful storage solutions instead of `emptyDir`
- Implementing CI/CD pipelines
