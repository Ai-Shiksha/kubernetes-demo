# Kubernetes Deployment Guide

## Prerequisites

Before deploying, ensure:
- ✅ Docker Desktop with Kubernetes enabled
- ✅ kubectl installed
- ✅ Docker image built and pushed to Docker Hub
- ✅ Kubernetes cluster running (`kubectl get nodes` returns results)

See [SETUP.md](./SETUP.md) for detailed installation instructions.

## Deployment Steps

### Step 1: Verify Kubernetes Cluster

```powershell
# Check cluster status
kubectl get nodes

# Expected output:
# NAME              STATUS   ROLES           AGE   VERSION
# docker-desktop    Ready    control-plane   XXX   vX.XX.X

# Check available storage
kubectl get storageclass
```

### Step 2: Create Namespace

Create an isolated namespace for this application:

```powershell
# Apply namespace
kubectl apply -f k8s/namespace.yaml

# Verify namespace created
kubectl get namespaces
kubectl describe namespace kubernetes-demo
```

### Step 3: Deploy PostgreSQL

Deploy the database service:

```powershell
# Apply PostgreSQL deployment
kubectl apply -f k8s/postgres-deployment.yaml

# Check deployment status
kubectl get deployments -n kubernetes-demo
kubectl get pods -n kubernetes-demo

# Wait for pod to be Running
kubectl get pods -n kubernetes-demo -w
# Press Ctrl+C when pod shows 1/1 Running

# Verify service
kubectl get svc -n kubernetes-demo -l app=postgres
```

**Verify Database Access**:
```powershell
# Port-forward to database
kubectl port-forward svc/postgres 5432:5432 -n kubernetes-demo

# In another terminal, connect (if you have psql installed)
psql -h localhost -U postgres -d kubedb
# Password: postgres
```

### Step 4: Deploy Redis

Deploy the caching service:

```powershell
# Apply Redis deployment
kubectl apply -f k8s/redis-deployment.yaml

# Check status
kubectl get pods -n kubernetes-demo -l app=redis

# Wait for pod to be Running
kubectl get pods -n kubernetes-demo -w
```

**Verify Redis Access**:
```powershell
# Port-forward to Redis
kubectl port-forward svc/redis 6379:6379 -n kubernetes-demo

# In another terminal (if you have redis-cli installed)
redis-cli -h localhost ping
# Should return: PONG
```

### Step 5: Deploy RabbitMQ

Deploy the message broker service:

```powershell
# Apply RabbitMQ deployment
kubectl apply -f k8s/rabbitmq-deployment.yaml

# Check status
kubectl get pods -n kubernetes-demo -l app=rabbitmq

# Wait for pod to be Running
kubectl get pods -n kubernetes-demo -w
```

**Access RabbitMQ Management UI**:
```powershell
# Port-forward to RabbitMQ management
kubectl port-forward svc/rabbitmq 15672:15672 -n kubernetes-demo

# Open browser: http://localhost:15672
# Username: guest
# Password: guest
```

### Step 6: Deploy Spring Boot Application

Deploy your application:

```powershell
# Apply application deployment
kubectl apply -f k8s/app-deployment.yaml

# Check deployment status
kubectl get deployments -n kubernetes-demo

# Wait for both pods to be Running (2 replicas)
kubectl get pods -n kubernetes-demo -w
# Press Ctrl+C when both pods show 1/1 Running
```

## Verify Complete Deployment

### Check All Resources

```powershell
# View all resources in namespace
kubectl get all -n kubernetes-demo

# Expected output should include:
# - 1 postgres pod (Running)
# - 1 redis pod (Running)
# - 1 rabbitmq pod (Running)
# - 2 kubernetes-demo-app pods (Running)
# - 4 services
```

### Check Services

```powershell
# List all services
kubectl get svc -n kubernetes-demo

# Expected output:
# NAME                    TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)
# kubernetes-demo-app     LoadBalancer   10.96.XXX.XXX   172.19.0.X    8080:XXXXX/TCP
# postgres                ClusterIP      None            <none>        5432/TCP
# redis                   ClusterIP      None            <none>        6379/TCP
# rabbitmq                ClusterIP      None            <none>        5672/TCP,15672/TCP
```

## Access the Application

### Method 1: Port Forwarding

```powershell
# Create port forward
kubectl port-forward svc/kubernetes-demo-app 8080:8080 -n kubernetes-demo

# Open browser
# http://localhost:8080

# Check health
# http://localhost:8080/actuator/health
```

### Method 2: LoadBalancer Service

On cloud providers, the LoadBalancer gets an external IP. For Docker Desktop, use port-forward as above.

## Monitor Application

### View Logs

```powershell
# View all pod logs
kubectl logs -f deployment/kubernetes-demo-app -n kubernetes-demo

# View specific pod logs
kubectl logs -f pod/POD_NAME -n kubernetes-demo

# View last 100 lines
kubectl logs --tail=100 -n kubernetes-demo -l app=kubernetes-demo-app

# View logs with timestamps
kubectl logs -f deployment/kubernetes-demo-app -n kubernetes-demo --timestamps=true
```

### Describe Resources

```powershell
# Get detailed info about deployment
kubectl describe deployment kubernetes-demo-app -n kubernetes-demo

# Get detailed info about specific pod
kubectl describe pod POD_NAME -n kubernetes-demo

# Get detailed info about service
kubectl describe svc kubernetes-demo-app -n kubernetes-demo
```

### Check Resource Usage

```powershell
# View node resource usage
kubectl top nodes

# View pod resource usage
kubectl top pods -n kubernetes-demo
```

## Execute Commands in Pods

### Connect to Application Pod

```powershell
# Get pod name
kubectl get pods -n kubernetes-demo -l app=kubernetes-demo-app -o name

# Connect to shell
kubectl exec -it POD_NAME -n kubernetes-demo -- /bin/sh

# View logs inside pod
kubectl exec -it POD_NAME -n kubernetes-demo -- cat /var/log/app.log

# Exit shell
exit
```

### Connect to Database Pod

```powershell
# Get pod name
kubectl get pods -n kubernetes-demo -l app=postgres -o name

# Connect to PostgreSQL
kubectl exec -it POD_NAME -n kubernetes-demo -- psql -U postgres -d kubedb

# SQL commands
# \dt (list tables)
# SELECT * FROM information_schema.tables;
# \q (exit)
```

### Connect to Redis Pod

```powershell
# Get pod name
kubectl get pods -n kubernetes-demo -l app=redis -o name

# Connect to Redis CLI
kubectl exec -it POD_NAME -n kubernetes-demo -- redis-cli

# Redis commands
# PING
# KEYS *
# exit
```

## Update Deployment

### Update Image

If you push a new image version:

```powershell
# Update image in deployment
kubectl set image deployment/kubernetes-demo-app app=sagarparikh93/kubernetes-demo:NEW_VERSION -n kubernetes-demo

# Monitor rollout
kubectl rollout status deployment/kubernetes-demo-app -n kubernetes-demo
```

### Rollback Deployment

If something goes wrong:

```powershell
# Check rollout history
kubectl rollout history deployment/kubernetes-demo-app -n kubernetes-demo

# Rollback to previous version
kubectl rollout undo deployment/kubernetes-demo-app -n kubernetes-demo

# Monitor rollback
kubectl rollout status deployment/kubernetes-demo-app -n kubernetes-demo
```

### Scale Replicas

```powershell
# Scale to 3 replicas
kubectl scale deployment kubernetes-demo-app --replicas=3 -n kubernetes-demo

# Check status
kubectl get pods -n kubernetes-demo
```

## Clean Up

### Delete Application Only

```powershell
kubectl delete deployment kubernetes-demo-app -n kubernetes-demo
kubectl delete svc kubernetes-demo-app -n kubernetes-demo
```

### Delete All Services in Namespace

```powershell
kubectl delete all -n kubernetes-demo
```

### Delete Entire Namespace

```powershell
kubectl delete namespace kubernetes-demo
```

## Troubleshooting

For common issues and solutions, see [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)

## Next Steps

- Read [ARCHITECTURE.md](./ARCHITECTURE.md) to understand the system design
- Read [API.md](./API.md) to see available endpoints
- Read [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) for problem solving
