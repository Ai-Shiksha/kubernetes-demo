# Architecture Guide

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      Kubernetes Cluster                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              kubernetes-demo Namespace                    │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │                                                            │   │
│  │  ┌──────────────────────────────────────────────────┐   │   │
│  │  │   Spring Boot Application (2 Replicas)          │   │   │
│  │  │  ┌────────────────────────────────────────────┐ │   │   │
│  │  │  │ Pod 1: kubernetes-demo-app-XXXXX         │ │   │   │
│  │  │  │ - Port: 8080                              │ │   │   │
│  │  │  │ - Health Check: /actuator/health          │ │   │   │
│  │  │  └────────────────────────────────────────────┘ │   │   │
│  │  │  ┌────────────────────────────────────────────┐ │   │   │
│  │  │  │ Pod 2: kubernetes-demo-app-YYYYY         │ │   │   │
│  │  │  │ - Port: 8080                              │ │   │   │
│  │  │  │ - Health Check: /actuator/health          │ │   │   │
│  │  │  └────────────────────────────────────────────┘ │   │   │
│  │  └──────────────────────────────────────────────────┘   │   │
│  │                     │                                    │   │
│  │                     ▼                                    │   │
│  │  ┌──────────────────────────────────────────────────┐   │   │
│  │  │              Service Layer                       │   │   │
│  │  ├──────────────────────────────────────────────────┤   │   │
│  │  │ kubernetes-demo-app (LoadBalancer)              │   │   │
│  │  │ postgres (ClusterIP)                            │   │   │
│  │  │ redis (ClusterIP)                               │   │   │
│  │  │ rabbitmq (ClusterIP)                            │   │   │
│  │  └──────────────────────────────────────────────────┘   │   │
│  │                                                            │   │
│  │  ┌──────────────────────────────────────────────────┐   │   │
│  │  │           Data Layer                            │   │   │
│  │  ├──────────────────────────────────────────────────┤   │   │
│  │  │                                                  │   │   │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │   │   │
│  │  │  │PostgreSQL│  │  Redis   │  │  RabbitMQ    │  │   │   │
│  │  │  │          │  │          │  │              │  │   │   │
│  │  │  │Port 5432 │  │Port 6379 │  │Port 5672     │  │   │   │
│  │  │  │          │  │          │  │Port 15672(UI)│  │   │   │
│  │  │  │emptyDir  │  │emptyDir  │  │emptyDir      │  │   │   │
│  │  │  │Volume    │  │Volume    │  │Volume        │  │   │   │
│  │  │  └──────────┘  └──────────┘  └──────────────┘  │   │   │
│  │  │                                                  │   │   │
│  │  └──────────────────────────────────────────────────┘   │   │
│  │                                                            │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## Component Details

### 1. Spring Boot Application Layer

**Image**: `sagarparikh93/kubernetes-demo:0.0.1-SNAPSHOT`

**Deployment**:
- **Type**: Deployment with 2 replicas
- **Resource**: `kubernetes-demo-app`
- **Port**: 8080

**Capabilities**:
- REST API endpoints
- Health checks via Spring Boot Actuator
- Database connectivity
- Redis caching
- RabbitMQ message publishing/consuming

**Health Checks**:
- **Liveness Probe**: `GET /actuator/health` (30s delay, 10s period)
- **Readiness Probe**: `GET /actuator/health` (20s delay, 5s period)

**Resource Requirements**:
- No CPU/memory limits set (uses node defaults)
- Can be scaled horizontally (replicas)

### 2. PostgreSQL Database

**Image**: `postgres:16-alpine`

**Configuration**:
- **Replicas**: 1
- **Port**: 5432
- **Database**: `kubedb`
- **Username**: `postgres`
- **Password**: `postgres` (stored in Secret)

**Storage**:
- **Type**: `emptyDir` (ephemeral, cleared on pod restart)
- **Mount Path**: `/var/lib/postgresql/data`

**Access Method**:
- **Internal**: `postgres:5432` (DNS-based service discovery)
- **External**: Port-forward then `localhost:5432`

**ConfigMap**:
- `POSTGRES_DB`: Database name
- `POSTGRES_USER`: Username

### 3. Redis Cache

**Image**: `redis:7-alpine`

**Configuration**:
- **Replicas**: 1
- **Port**: 6379

**Storage**:
- **Type**: `emptyDir` (ephemeral)
- **Mount Path**: `/data`

**Access Method**:
- **Internal**: `redis:6379` (DNS-based service discovery)
- **External**: Port-forward then `localhost:6379`

**Use Cases**:
- Application caching
- Session storage
- Rate limiting
- Real-time data

### 4. RabbitMQ Message Broker

**Image**: `rabbitmq:3.12-management-alpine`

**Configuration**:
- **Replicas**: 1
- **AMQP Port**: 5672
- **Management UI Port**: 15672
- **Default User**: `guest`
- **Default Password**: `guest`

**Storage**:
- **Type**: `emptyDir` (ephemeral)
- **Mount Path**: `/var/lib/rabbitmq`

**Access Method**:
- **AMQP**: `rabbitmq:5672` (for messages)
- **Management UI**: `rabbitmq:15672` (for admin)
- **External**: Port-forward then access locally

**Management UI**:
- URL: `http://localhost:15672`
- Username: `guest`
- Password: `guest`

## Service Discovery

### Internal Communication

Services communicate using Kubernetes DNS:

```
<service-name>.<namespace>.svc.cluster.local
```

For this project:
- PostgreSQL: `postgres.kubernetes-demo.svc.cluster.local:5432`
- Redis: `redis.kubernetes-demo.svc.cluster.local:6379`
- RabbitMQ: `rabbitmq.kubernetes-demo.svc.cluster.local:5672`

### Environment Variables

Application uses environment variables for service configuration:

```
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/kubedb
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: postgres
SPRING_REDIS_HOST: redis
SPRING_REDIS_PORT: 6379
SPRING_RABBITMQ_HOST: rabbitmq
SPRING_RABBITMQ_PORT: 5672
```

## Data Flow

### Request Flow

```
1. Client Request
   ↓
2. LoadBalancer Service (kubernetes-demo-app:8080)
   ↓
3. Round-robin to App Pod
   ↓
4. Spring Boot Application
   ├── Read from/Write to PostgreSQL
   ├── Check/Update Redis Cache
   └── Publish/Consume RabbitMQ Messages
   ↓
5. Response to Client
```

### Service Communication

```
App Pod ←→ PostgreSQL (jdbc:postgresql://)
App Pod ←→ Redis (redis://)
App Pod ←→ RabbitMQ (amqp://)
```

## Resource Management

### Namespace Isolation

```yaml
metadata:
  namespace: kubernetes-demo
```

All resources run in isolated namespace for:
- Better organization
- Resource quotas
- Role-based access control
- Easy cleanup

### Storage Strategy

**Current**:
- Uses `emptyDir` volumes (ephemeral storage)
- Data lost on pod restart
- Suitable for development/testing

**For Production**:
- Use `PersistentVolumeClaim` (PVC)
- Use cloud storage (EBS, GCE Persistent Disk)
- Enable data persistence

### Replica Strategy

**Application**: 2 replicas
- Load balancing across pods
- High availability
- Can scale up/down

**Other Services**: 1 replica
- Database, cache, queue need single source
- Can be scaled for HA setup with clustering

## Load Balancing

### Service Type: LoadBalancer

```yaml
type: LoadBalancer
ports:
  - port: 8080
    targetPort: 8080
```

**On Docker Desktop**:
- EXTERNAL-IP: 172.19.0.6 (Docker Desktop IP)
- Use port-forward for local access

**On Cloud Providers**:
- Gets external IP automatically
- Distributes traffic to all replicas

## Health Checks

### Liveness Probe

Restarts pod if unhealthy:
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

### Readiness Probe

Takes pod out of service if not ready:
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 20
  periodSeconds: 5
```

## Deployment Strategy

### Rolling Update (Default)

- Gradually replaces old pods with new ones
- Zero downtime
- Automatic rollback on failure

### Scaling

```bash
# Scale to N replicas
kubectl scale deployment kubernetes-demo-app --replicas=N
```

Only application scales horizontally. Databases typically don't scale this way.

## Network Architecture

### DNS Names

Internal pod-to-pod communication:
- `postgres` → resolves to ClusterIP
- `redis` → resolves to ClusterIP
- `rabbitmq` → resolves to ClusterIP

### Port Mapping

| Service | Internal Port | External Port | Protocol |
|---------|--------------|---------------|----------|
| App | 8080 | 8080 | HTTP |
| PostgreSQL | 5432 | 5432 | JDBC |
| Redis | 6379 | 6379 | Redis Protocol |
| RabbitMQ AMQP | 5672 | 5672 | AMQP |
| RabbitMQ UI | 15672 | 15672 | HTTP |

## Next Steps

- Read [DEPLOYMENT.md](./DEPLOYMENT.md) to learn how to deploy
- Read [API.md](./API.md) to see available endpoints
- Explore Kubernetes manifests in `k8s/` folder
