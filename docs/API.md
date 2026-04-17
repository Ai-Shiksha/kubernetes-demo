# API Documentation

## Base URL

```
http://localhost:8080
```

## Health Check Endpoint

### GET /actuator/health

Check application health status.

**Request**:
```http
GET /actuator/health HTTP/1.1
Host: localhost:8080
```

**Response** (200 OK):
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "redis": "6379"
      }
    },
    "rabbitmq": {
      "status": "UP",
      "details": {
        "rabbitmq": "5672"
      }
    }
  }
}
```

**Status Codes**:
- `200 OK`: Application is healthy
- `503 Service Unavailable`: One or more components are down

---

## Sample Endpoints

### GET /

Welcome endpoint (if configured).

**Request**:
```http
GET / HTTP/1.1
Host: localhost:8080
```

**Response** (200 OK):
```json
{
  "message": "Welcome to Kubernetes Demo Application"
}
```

---

## Actuator Endpoints

Spring Boot Actuator provides additional endpoints:

### GET /actuator

List all available actuator endpoints.

**Request**:
```http
GET /actuator HTTP/1.1
Host: localhost:8080
```

**Response** (200 OK):
```json
{
  "_links": {
    "self": {
      "href": "http://localhost:8080/actuator",
      "templated": false
    },
    "health": {
      "href": "http://localhost:8080/actuator/health",
      "templated": false
    },
    "metrics": {
      "href": "http://localhost:8080/actuator/metrics",
      "templated": false
    }
  }
}
```

### GET /actuator/health/liveness

Kubernetes liveness probe endpoint.

**Request**:
```http
GET /actuator/health/liveness HTTP/1.1
Host: localhost:8080
```

**Response** (200 OK):
```json
{
  "status": "UP"
}
```

### GET /actuator/health/readiness

Kubernetes readiness probe endpoint.

**Request**:
```http
GET /actuator/health/readiness HTTP/1.1
Host: localhost:8080
```

**Response** (200 OK):
```json
{
  "status": "UP"
}
```

### GET /actuator/metrics

Application metrics.

**Request**:
```http
GET /actuator/metrics HTTP/1.1
Host: localhost:8080
```

**Response** (200 OK):
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "http.server.requests",
    "process.uptime",
    ...
  ]
}
```

### GET /actuator/info

Application information.

**Request**:
```http
GET /actuator/info HTTP/1.1
Host: localhost:8080
```

**Response** (200 OK):
```json
{
  "app": {
    "name": "kubernetes-demo",
    "version": "0.0.1-SNAPSHOT",
    "encoding": "UTF-8"
  }
}
```

---

## Common Response Codes

| Code | Meaning | Example |
|------|---------|---------|
| 200 | OK | Successful request |
| 400 | Bad Request | Invalid parameters |
| 404 | Not Found | Endpoint doesn't exist |
| 500 | Internal Server Error | Server error |
| 503 | Service Unavailable | Dependencies down |

---

## Testing Endpoints

### Using curl

```bash
# Health check
curl http://localhost:8080/actuator/health

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness

# Metrics
curl http://localhost:8080/actuator/metrics

# Info
curl http://localhost:8080/actuator/info
```

### Using PowerShell

```powershell
# Health check
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method Get

# With detailed output
$response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health"
$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

### Using Postman

1. Open Postman
2. Create new request
3. Set method to `GET`
4. Enter URL: `http://localhost:8080/actuator/health`
5. Click `Send`

---

## Database Access (via kubectl)

### Query Database

```bash
# Get into database pod
kubectl exec -it <postgres-pod-name> -n kubernetes-demo -- psql -U postgres -d kubedb

# Common SQL commands
# \dt              - List tables
# \d+ <table>      - Describe table
# SELECT * FROM information_schema.tables;
# \q               - Exit
```

---

## Cache Access (via kubectl)

### Access Redis

```bash
# Connect to Redis
kubectl exec -it <redis-pod-name> -n kubernetes-demo -- redis-cli

# Common Redis commands
# PING
# KEYS *
# GET <key>
# SET <key> <value>
# TTL <key>
# exit
```

---

## Message Queue Access (via Web UI)

### RabbitMQ Management UI

1. Port-forward:
   ```bash
   kubectl port-forward svc/rabbitmq 15672:15672 -n kubernetes-demo
   ```

2. Open browser: `http://localhost:15672`

3. Login:
   - Username: `guest`
   - Password: `guest`

4. Explore:
   - **Overview**: Cluster status
   - **Connections**: Active connections
   - **Channels**: Active channels
   - **Queues**: Message queues
   - **Exchanges**: Message exchanges

---

## Monitoring via kubectl

### View Application Logs

```bash
# Real-time logs
kubectl logs -f deployment/kubernetes-demo-app -n kubernetes-demo

# Last 100 lines
kubectl logs --tail=100 deployment/kubernetes-demo-app -n kubernetes-demo

# Specific pod
kubectl logs -f pod/<pod-name> -n kubernetes-demo
```

### Check Service Status

```bash
# All services
kubectl get svc -n kubernetes-demo

# Specific service
kubectl get svc kubernetes-demo-app -n kubernetes-demo

# Service endpoints
kubectl get endpoints kubernetes-demo-app -n kubernetes-demo
```

### Check Deployments

```bash
# Deployment status
kubectl get deployments -n kubernetes-demo

# Detailed deployment info
kubectl describe deployment kubernetes-demo-app -n kubernetes-demo

# Rollout history
kubectl rollout history deployment/kubernetes-demo-app -n kubernetes-demo
```

---

## Example Workflow

### 1. Start Port-Forward

```powershell
kubectl port-forward svc/kubernetes-demo-app 8080:8080 -n kubernetes-demo
```

### 2. Check Health

```powershell
Invoke-WebRequest http://localhost:8080/actuator/health -Method Get | Select-Object -ExpandProperty Content | ConvertFrom-Json
```

### 3. View Logs

```powershell
kubectl logs -f deployment/kubernetes-demo-app -n kubernetes-demo
```

### 4. Check Services

```powershell
kubectl get all -n kubernetes-demo
```

### 5. Access RabbitMQ UI

```powershell
# In another terminal
kubectl port-forward svc/rabbitmq 15672:15672 -n kubernetes-demo
# Open http://localhost:15672
```

### 6. Query Database

```powershell
kubectl exec -it <postgres-pod> -n kubernetes-demo -- psql -U postgres -d kubedb
```

---

## Troubleshooting API Issues

### Application Not Responding

1. Check pod status:
   ```bash
   kubectl get pods -n kubernetes-demo
   ```

2. Check logs:
   ```bash
   kubectl logs -f deployment/kubernetes-demo-app -n kubernetes-demo
   ```

3. Check service:
   ```bash
   kubectl get svc kubernetes-demo-app -n kubernetes-demo
   kubectl get endpoints kubernetes-demo-app -n kubernetes-demo
   ```

### Health Check Failing

1. Check dependencies:
   ```bash
   kubectl get pods -n kubernetes-demo
   # Verify all pods are Running
   ```

2. Check connectivity:
   ```bash
   kubectl exec -it <app-pod> -n kubernetes-demo -- sh
   # Try connecting to postgres, redis, rabbitmq
   ```

3. Check logs for errors:
   ```bash
   kubectl logs -f <app-pod> -n kubernetes-demo
   ```

---

## Next Steps

- Read [DEPLOYMENT.md](./DEPLOYMENT.md) for deployment instructions
- Read [ARCHITECTURE.md](./ARCHITECTURE.md) for system design
- Read [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) for common issues
