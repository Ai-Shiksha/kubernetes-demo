pipeline {
    agent any

    environment {
        DOCKER_IMAGE     = "kubernetes-demo"
        DOCKER_TAG       = "${BUILD_NUMBER}"
        KUBE_NAMESPACE   = "kubernetes-demo"
        APP_NAME         = "kubernetes-demo-app"
    }

    stages {

        stage('Checkout') {
            steps {
                echo '========== Stage 1: Checkout Code =========='
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                echo '========== Stage 2: Build Spring Boot JAR =========='
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '========== Stage 3: Build Docker Image =========='
                sh '''
                    docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                    docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                    echo "Docker image built: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                '''
            }
        }

        stage('Deploy Infrastructure to Kubernetes') {
            steps {
                echo '========== Stage 4: Deploy PostgreSQL, Redis, RabbitMQ =========='
                sh '''
                    # Create namespace if not exists
                    kubectl apply -f k8s/namespace.yaml

                    # Deploy infra services
                    kubectl apply -f k8s/postgres-deployment.yaml
                    kubectl apply -f k8s/redis-deployment.yaml
                    kubectl apply -f k8s/rabbitmq-deployment.yaml

                    echo "Waiting for infra services to be ready..."
                    sleep 20

                    kubectl get pods -n ${KUBE_NAMESPACE}
                '''
            }
        }

        stage('Deploy Application to Kubernetes') {
            steps {
                echo '========== Stage 5: Deploy Spring Boot App =========='
                sh '''
                    # Update image tag in deployment
                    sed -i "s|kubernetes-demo:.*|${DOCKER_IMAGE}:${DOCKER_TAG}|g" k8s/app-deployment.yaml

                    # Deploy app
                    kubectl apply -f k8s/app-deployment.yaml

                    echo "Waiting for application to be ready..."
                    kubectl rollout status deployment/${APP_NAME} -n ${KUBE_NAMESPACE} --timeout=120s

                    echo "All resources in namespace:"
                    kubectl get all -n ${KUBE_NAMESPACE}
                '''
            }
        }

        stage('Verify') {
            steps {
                echo '========== Stage 6: Verify Deployment =========='
                sh '''
                    echo "Pods:"
                    kubectl get pods -n ${KUBE_NAMESPACE}

                    echo "Services:"
                    kubectl get svc -n ${KUBE_NAMESPACE}

                    echo "Deployments:"
                    kubectl get deployments -n ${KUBE_NAMESPACE}
                '''
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully! App is deployed to Kubernetes.'
        }
        failure {
            echo '❌ Pipeline failed! Check the logs above.'
        }
        always {
            echo 'Pipeline finished.'
        }
    }
}
