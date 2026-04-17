# Setup & Prerequisites

## System Requirements

### Windows PC
- Windows 10/11 (Home, Pro, or Enterprise)
- Minimum 8GB RAM (16GB recommended)
- 50GB free disk space
- Virtualization enabled in BIOS

### Mac/Linux
- macOS 11+ or Linux (Ubuntu 18.04+)
- Minimum 8GB RAM (16GB recommended)
- 50GB free disk space

## Software Installation

### 1. Docker Desktop

**Download**: https://www.docker.com/products/docker-desktop

#### Installation Steps:

1. **Download and install** Docker Desktop for your OS
2. **Launch Docker Desktop**
3. **Enable Kubernetes**:
   - Open Docker Desktop Settings
   - Go to **Kubernetes** tab
   - Check **"Enable Kubernetes"**
   - Click **"Apply & Restart"** (takes 5-10 minutes)
4. **Verify Installation**:
   ```powershell
   docker version
   kubectl version --client
   docker run hello-world
   ```

### 2. Java Development Kit (JDK) 17

**Download**: https://www.oracle.com/java/technologies/downloads/#java17

#### Installation Steps:

1. Download JDK 17 installer
2. Run the installer
3. Set `JAVA_HOME` environment variable:
   
   **Windows**:
   ```powershell
   setx JAVA_HOME "C:\Program Files\Java\jdk-17"
   # Restart PowerShell
   java -version
   ```

   **Mac/Linux**:
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   echo $JAVA_HOME
   java -version
   ```

### 3. Maven 3.8+

**Download**: https://maven.apache.org/download.cgi

#### Installation Steps:

1. Download Maven (binary archive)
2. Extract to a folder (e.g., `C:\apache-maven` on Windows)
3. Set `MAVEN_HOME` environment variable:
   
   **Windows**:
   ```powershell
   setx MAVEN_HOME "C:\apache-maven-3.9.x"
   setx PATH "%PATH%;%MAVEN_HOME%\bin"
   # Restart PowerShell
   mvn --version
   ```

   **Mac/Linux**:
   ```bash
   export MAVEN_HOME=/opt/apache-maven-3.9.x
   export PATH=$PATH:$MAVEN_HOME/bin
   echo $PATH
   mvn --version
   ```

### 4. Git (Optional but Recommended)

**Download**: https://git-scm.com/download

#### Installation Steps:

1. Download Git installer
2. Run installer with default options
3. Verify:
   ```powershell
   git --version
   ```

### 5. Docker Hub Account

1. Go to https://hub.docker.com/signup
2. Create a free account
3. Verify email
4. Login to Docker Desktop with your credentials

## Project Setup

### 1. Clone or Download Project

```powershell
# Option 1: Clone from repository (if using Git)
git clone <repository-url>
cd kubernetes-demo

# Option 2: Download and extract zip file
# Navigate to the extracted folder
cd kubernetes-demo
```

### 2. Initialize Git Repository (if not already done)

```powershell
git init
git add .
git commit -m "Initial commit"
```

### 3. Build the Project

```powershell
# Navigate to project root
cd C:\sagar\AI POC\kubernetes-demo

# Build with Maven
mvn clean package

# Expected output: BUILD SUCCESS
```

### 4. Build Docker Image

```powershell
# Build the image
docker build -t kubernetes-demo:0.0.1-SNAPSHOT .

# Verify image was created
docker images | findstr kubernetes-demo
```

### 5. Tag and Push to Docker Hub

```powershell
# Replace YOUR_USERNAME with your Docker Hub username
$username = "YOUR_USERNAME"

# Tag the image
docker tag kubernetes-demo:0.0.1-SNAPSHOT $username/kubernetes-demo:0.0.1-SNAPSHOT

# Login to Docker Hub
docker login
# Enter your Docker Hub credentials

# Push to Docker Hub
docker push $username/kubernetes-demo:0.0.1-SNAPSHOT
```

## Verify Setup

Run this checklist to verify everything is working:

```powershell
# Check Docker
docker --version
docker ps

# Check Kubernetes
kubectl version
kubectl get nodes

# Check Java
java -version
javac -version

# Check Maven
mvn --version

# Check Git
git --version

# Check Docker Hub connectivity
docker pull redis:7-alpine
docker images | findstr redis
```

## Environment Variables

### Windows PowerShell

Create/update your profile:

```powershell
# Edit profile
notepad $PROFILE

# Add these lines
$env:DOCKER_USERNAME = "your_docker_hub_username"
$env:PROJECT_HOME = "C:\sagar\AI POC\kubernetes-demo"

# Reload profile
. $PROFILE
```

### Mac/Linux Bash

Edit your shell config file (`.bashrc`, `.zshrc`):

```bash
export DOCKER_USERNAME="your_docker_hub_username"
export PROJECT_HOME="/path/to/kubernetes-demo"
```

## Common Issues

### Issue: Kubernetes not enabled in Docker Desktop

**Solution**:
1. Open Docker Desktop
2. Go to Settings → Kubernetes
3. Check "Enable Kubernetes"
4. Click "Apply & Restart"
5. Wait 5-10 minutes for it to initialize

### Issue: Maven command not found

**Solution**:
1. Verify MAVEN_HOME is set: `echo $env:MAVEN_HOME` (Windows) or `echo $MAVEN_HOME` (Mac/Linux)
2. Verify PATH includes Maven bin folder
3. Restart terminal after setting environment variables

### Issue: Docker image build fails

**Solution**:
```powershell
# Check Dockerfile
cat Dockerfile

# Try building with verbose output
docker build -t kubernetes-demo:0.0.1-SNAPSHOT . --progress=plain

# Check Docker daemon
docker ps
```

### Issue: Cannot push to Docker Hub

**Solution**:
```powershell
# Login again
docker logout
docker login

# Verify credentials
docker ps  # This will fail if not authenticated

# Try pushing again
docker push username/kubernetes-demo:0.0.1-SNAPSHOT
```

## Next Steps

Once setup is complete:
1. Read [DEPLOYMENT.md](./DEPLOYMENT.md) to deploy to Kubernetes
2. Read [ARCHITECTURE.md](./ARCHITECTURE.md) to understand the system
3. Read [API.md](./API.md) to understand available endpoints
4. Read [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) for common issues

## Resources

- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Maven Documentation](https://maven.apache.org/guides/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
