# Troubleshooting Guide

## Common Issues and Solutions

---

## 1. Pods Not Starting

### Problem: Pods stuck in Pending/ImagePullBackOff

**Symptoms**:
```
NAME                                  READY   STATUS             RESTARTS   AGE
kubernetes-demo-app-xxxxx             0/1     ImagePullBackOff   0          2m
```

**Causes & Solutions**:

#### Solution 1: Check Pod Description
```powershell
kubectl describe pod POD_NAME -n kubernetes-demo
```

Look at the "Events" section for error messages.

#### Solution 2: Image Not Available Locally

```powershell
# Check if image exists
docker images | findstr kubernetes-demo

# If not found, build it
docker build -t kubernetes-demo:0.0.1-SNAPSHOT .

# Verify
docker images | findstr kubernetes-demo
```

#### Solution 3: Change ImagePullPolicy

Edit `k8s/app-deployment.yaml`:

```yaml
imagePullPolicy: IfNotPresent  # Try this first
# or
imagePullPolicy: Never         # Use only local image
```

Then redeploy:
```powershell
kubectl apply -f k8s/app-deployment.yaml
```

#### Solution 4: Image Not Pushed to Docker Hub

```powershell
# Tag the image
docker tag kubernetes-demo:0.0.1-SNAPSHOT USERNAME/kubernetes-demo:0.0.1-SNAPSHOT

# Login
docker login

# Push
docker push USERNAME/kubernetes-demo:0.0.1-SNAPSHOT

# Update app-deployment.yaml with correct image name
# Re-apply
kubectl apply -f k8s/app-deployment.yaml
```

---

## 2. CrashLoopBackOff Error

### Problem: Pod keeps crashing and restarting

**Symptoms**:
```
NAME                                  READY   STATUS             RESTARTS   AGE
kubernetes-demo-app-xxxxx             0/1     CrashLoopBackOff   5          3m
```

**Solutions**:

#### Check Pod Logs
```powershell
kubectl logs -f pod/POD_NAME -n kubernetes-demo

# Or previous crash logs
kubectl logs --previous pod/POD_NAME -n kubernetes-demo
```

#### Common Causes

**1. Database Connection Failed**:
```
Error connecting to postgres://postgres:5432/kubedb
```

Solution:
```powershell
# Check if postgres pod is running
kubectl get pods -n kubernetes-demo | findstr postgres

# Check postgres logs
kubectl logs -f pod/POSTGRES_POD -n kubernetes-demo

# Verify database credentials
kubectl describe deployment postgres -n kubernetes-demo
```

**2. Redis Connection Failed**:
```
Cannot connect to redis:6379
```

Solution:
```powershell
# Check Redis pod
kubectl get pods -n kubernetes-demo | findstr redis

# Check Redis logs
kubectl logs -f pod/REDIS_POD -n kubernetes-demo

# Test Redis connectivity from app pod
kubectl exec -it APP_POD -n kubernetes-demo -- sh
# Then: nc -zv redis 6379
```

**3. RabbitMQ Connection Failed**:
```
Cannot connect to rabbitmq:5672
```

Solution:
```powershell
# Check RabbitMQ pod
kubectl get pods -n kubernetes-demo | findstr rabbitmq

# Check RabbitMQ logs
kubectl logs -f pod/RABBITMQ_POD -n kubernetes-demo

# Test RabbitMQ connectivity
kubectl exec -it APP_POD -n kubernetes-demo -- sh
# Then: nc -zv rabbitmq 5672
```

#### Memory/Resource Issues

```powershell
# Check node resources
kubectl top nodes

# Check pod resources
kubectl top pods -n kubernetes-demo

# If pods exceed resource limits, increase them in YAML
```

---

## 3. ErrImageNeverPull

### Problem: Image policy set to Never but image not found locally

**Symptoms**:
```
Container image "kubernetes-demo:0.0.1-SNAPSHOT" is not present with pull policy of Never
```

**Solution**:

```powershell
# Build the image locally
docker build -t kubernetes-demo:0.0.1-SNAPSHOT .

# Change imagePullPolicy to IfNotPresent
# Edit k8s/app-deployment.yaml

# Re-apply
kubectl apply -f k8s/app-deployment.yaml

# Monitor
kubectl get pods -n kubernetes-demo -w
```

---

## 4. ImagePullBackOff with Docker Hub

### Problem: Cannot pull image from Docker Hub

**Symptoms**:
```
Failed to pull image "sagarparikh93/kubernetes-demo:0.0.1-SNAPSHOT":
failed to resolve reference: pull access denied, repository does not exist
```

**Solutions**:

#### Solution 1: Image Not Pushed Yet

```powershell
# Verify image exists locally
docker images | findstr kubernetes-demo

# If exists, push it
docker tag kubernetes-demo:0.0.1-SNAPSHOT USERNAME/kubernetes-demo:0.0.1-SNAPSHOT
docker login
docker push USERNAME/kubernetes-demo:0.0.1-SNAPSHOT

# Wait for push to complete
# Re-apply deployment
kubectl apply -f k8s/app-deployment.yaml
```

#### Solution 2: Wrong Image Name in YAML

```powershell
# Check image name in app-deployment.yaml
cat k8s/app-deployment.yaml | findstr image:

# Should match Docker Hub: USERNAME/kubernetes-demo:VERSION
# Update if needed and re-apply
```

#### Solution 3: Network Issues

```powershell
# Test Docker Hub connectivity
docker logout
docker login  # Re-authenticate

# Try pulling a test image
docker pull redis:7-alpine

# If this fails, check your internet connection
```

---

## 5. Service Not Accessible

### Problem: Cannot connect to application via port-forward

**Symptoms**:
```
Connection refused or timeout
```

**Solutions**:

#### Step 1: Verify Service Exists

```powershell
kubectl get svc -n kubernetes-demo

# Should show kubernetes-demo-app service
```

#### Step 2: Check Service Endpoints

```powershell
kubectl get endpoints -n kubernetes-demo

# Should show IP addresses of running pods
```

#### Step 3: Check Pod Status

```powershell
kubectl get pods -n kubernetes-demo

# All should show "1/1 Running"
```

#### Step 4: Port-Forward

```powershell
# Kill any existing port-forwards (Ctrl+C)

# Start new port-forward
kubectl port-forward svc/kubernetes-demo-app 8080:8080 -n kubernetes-demo

# Should show: Forwarding from 127.0.0.1:8080 -> 8080
```

#### Step 5: Test Connection

```powershell
# In another terminal
Invoke-WebRequest http://localhost:8080/actuator/health

# If fails, check app logs
kubectl logs -f deployment/kubernetes-demo-app -n kubernetes-demo
```

---

## 6. Readiness/Liveness Probe Failing

### Problem: Pods are crashing due to failed health checks

**Symptoms**:
```
Liveness probe failed: HTTP probe failed with statuscode: 503
Readiness probe failed: HTTP probe failed with statuscode: 503
```

**Solutions**:

#### Check Health Endpoint

```powershell
# Port-forward and test
kubectl port-forward svc/kubernetes-demo-app 8080:8080 -n kubernetes-demo

# In another terminal
Invoke-WebRequest http://localhost:8080/actuator/health -UseBasicParsing | ConvertFrom-Json
```

#### Check Application Logs

```powershell
kubectl logs -f deployment/kubernetes-demo-app -n kubernetes-demo

# Look for dependency errors (database, redis, rabbitmq)
```

#### Increase Probe Timeouts

Edit `k8s/app-deployment.yaml`:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 60    # Increase from 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 40    # Increase from 20
  periodSeconds: 5
```

Re-apply:
```powershell
kubectl apply -f k8s/app-deployment.yaml
```

#### Verify Dependencies

```powershell
# Check all pods
kubectl get pods -n kubernetes-demo

# All services must be Running before app starts
```

---

## 7. Database Connection Issues

### Problem: Application cannot connect to PostgreSQL

**Symptoms**:
```
FATAL: password authentication failed for user "postgres"
or
Connection refused: postgres:5432
```

**Solutions**:

#### Check Database Pod

```powershell
kubectl get pods -n kubernetes-demo -l app=postgres

# Should show "1/1 Running"

# View logs
kubectl logs -f pod/POSTGRES_POD -n kubernetes-demo
```

#### Check Credentials

Verify in `k8s/postgres-deployment.yaml`:

```yaml
env:
- name: POSTGRES_DB
  value: "kubedb"
- name: POSTGRES_USER
  value: "postgres"
- name: POSTGRES_PASSWORD
  value: "postgres"  # Check this
```

And in `k8s/app-deployment.yaml`:

```yaml
- name: SPRING_DATASOURCE_URL
  value: "jdbc:postgresql://postgres:5432/kubedb"
- name: SPRING_DATASOURCE_USERNAME
  value: "postgres"
- name: SPRING_DATASOURCE_PASSWORD
  value: "postgres"
```

#### Test Connectivity

```powershell
# Connect to app pod
kubectl exec -it APP_POD -n kubernetes-demo -- sh

# Inside pod:
apt-get update && apt-get install -y postgresql-client

# Try connecting
psql -h postgres -U postgres -d kubedb
# Enter password: postgres
```

---

## 8. Namespace Issues

### Problem: Resources not found in namespace

**Symptoms**:
```
Error from server (NotFound): pods "xyz" not found
```

**Solutions**:

#### Check Current Namespace

```powershell
kubectl config view | findstr namespace

# Should show: namespace: kubernetes-demo
```

#### Set Default Namespace

```powershell
kubectl config set-context --current --namespace=kubernetes-demo

# Now all commands default to this namespace
# You won't need -n kubernetes-demo flag
```

#### Create Namespace if Missing

```powershell
kubectl apply -f k8s/namespace.yaml

# Verify
kubectl get namespaces
```

---

## 9. Deployment Issues

### Problem: Deployment stuck in "Waiting for Rollout"

**Symptoms**:
```
Waiting for deployment spec update to be observed...
```

**Solutions**:

#### Check Deployment Status

```powershell
kubectl rollout status deployment/kubernetes-demo-app -n kubernetes-demo

# Shows: Waiting for deployment spec update to be observed
```

#### Check Events

```powershell
kubectl describe deployment kubernetes-demo-app -n kubernetes-demo

# Look at "Events" section
```

#### Force Restart

```powershell
kubectl rollout restart deployment/kubernetes-demo-app -n kubernetes-demo

# Wait for pods to restart
kubectl get pods -n kubernetes-demo -w
```

#### Rollback if Needed

```powershell
# Check history
kubectl rollout history deployment/kubernetes-demo-app -n kubernetes-demo

# Rollback to previous version
kubectl rollout undo deployment/kubernetes-demo-app -n kubernetes-demo

# Check status
kubectl rollout status deployment/kubernetes-demo-app -n kubernetes-demo
```

---

## 10. Storage Issues

### Problem: Data lost after pod restart

**Note**: This is expected behavior with `emptyDir` volumes.

**For Development**: This is fine, data is temporary.

**For Production**: Use PersistentVolumeClaim:

```yaml
volumes:
- name: postgres-storage
  persistentVolumeClaim:
    claimName: postgres-pvc
```

---

## Diagnostic Commands

### Quick Health Check

```powershell
# All resources status
kubectl get all -n kubernetes-demo

# Pod status
kubectl get pods -n kubernetes-demo

# Service status
kubectl get svc -n kubernetes-demo

# Deployment status
kubectl get deployments -n kubernetes-demo
```

### Deep Dive

```powershell
# Deployment details
kubectl describe deployment kubernetes-demo-app -n kubernetes-demo

# Pod details
kubectl describe pod POD_NAME -n kubernetes-demo

# Service details
kubectl describe svc kubernetes-demo-app -n kubernetes-demo

# Events
kubectl get events -n kubernetes-demo --sort-by='.lastTimestamp'
```

### Log Analysis

```powershell
# Current logs
kubectl logs -f deployment/kubernetes-demo-app -n kubernetes-demo

# Previous crash logs
kubectl logs --previous pod/POD_NAME -n kubernetes-demo

# All pods logs
kubectl logs -f -l app=kubernetes-demo-app -n kubernetes-demo

# Timestamps
kubectl logs -f deployment/kubernetes-demo-app -n kubernetes-demo --timestamps=true
```

### Resource Usage

```powershell
# Node resources
kubectl top nodes

# Pod resources
kubectl top pods -n kubernetes-demo

# Detailed metrics
kubectl get pods -n kubernetes-demo -o=custom-columns=NAME:.metadata.name,CPU:.status.container.metrics.cpu,MEMORY:.status.container.metrics.memory
```

---

## Useful Links

- [Kubernetes Troubleshooting](https://kubernetes.io/docs/tasks/debug-application-cluster/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
- [Spring Boot Actuator Docs](https://spring.io/guides/gs/actuator-service/)
- [Docker Troubleshooting](https://docs.docker.com/config/containers/resource_constraints/)

---

## Still Stuck?

1. Collect all diagnostics:
   ```powershell
   kubectl get all -n kubernetes-demo > status.txt
   kubectl describe all -n kubernetes-demo >> status.txt
   kubectl logs -f deployment/kubernetes-demo-app -n kubernetes-demo >> logs.txt
   ```

2. Check resources provided in links above

3. Review your changes against the template YAML files

4. Recreate the deployment from scratch if needed
