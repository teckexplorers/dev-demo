pipeline {
    agent { label 'jenkins-slave' }
    
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['Development', 'Staging/UAT', 'Production'], description: 'Select the environment to run the pipeline')
        string(name: 'EXECUTOR_NAME', defaultValue: '', description: 'Enter Your Name Please', trim: true)
        booleanParam(
            name: 'DEPLOY_TO_DEV',
            defaultValue: false,
            description: 'Deploy to Dev environment?'
        )
    }
    
    tools {
        maven "MAVEN-3.9" 
    }
    
    environment {
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk-amd64'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        SONAR_ORGANIZATION = "teckexplorers"
        SONAR_PROJECTKEY = "teckexplorers_dev-demo"
        IMAGE_TAG = "${BUILD_NUMBER}"
        IMAGE_NAME = "calc-demo"
        DOCKER_IMAGE = "monsternex007/calc-demo"
        KUBECONFIG = '/home/ubuntu/.kube/config'
    }

    stages {
        stage("1. CLEAN WORKSPACE") {
            steps {
                cleanWs()
            }
        }
        
        stage("2. CHECKOUT FROM SCM") {
            steps {
                git branch: 'main',
                    credentialsId: 'GITHUB',
                    url: 'https://github.com/teckexplorers/dev-demo.git'
                sh 'ls -ltr'
            }
        }
        
        stage("3. VERIFY TOOLS") {
            steps {
                sh 'java -version'
                sh 'mvn -version'
            }
        }
        
        stage("4. BUILD & TEST") {
            steps {
                sh 'mvn clean verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage("5. SONARCLOUD ANALYSIS") {
            steps {
                withCredentials([string(credentialsId: 'JENKINS-SONAR-TOKEN', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        mvn sonar:sonar \
                          -Dsonar.host.url=https://sonarcloud.io \
                          -Dsonar.organization=${SONAR_ORGANIZATION} \
                          -Dsonar.projectKey=${SONAR_PROJECTKEY} \
                          -Dsonar.token=${SONAR_TOKEN}
                    '''
                }
            }
        }
        
        stage('7. BUILD IMAGE AND PUSH TO REGISTRY') {
            steps{
                withCredentials([usernamePassword(credentialsId: 'DOCKERHUB', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_TOKEN')]){
                    sh '''
                        echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USER" --password-stdin
                        
                        docker buildx build --push \
                          -t $DOCKERHUB_USER/$IMAGE_NAME:$IMAGE_TAG \
                          -t $DOCKERHUB_USER/$IMAGE_NAME:latest \
                          .
                    '''
                }
            }
        }
        
        stage('8. BUILD CLEAN UP') {
            steps {
                sh 'echo === Cleaning Up Docker Resources for further builds ==='
                sh 'docker system prune -af'
            }
        }

        stage('9. Approval for Dev Deployment') {
            when {
                expression { return params.DEPLOY_TO_DEV }
            }
            steps {
                input message: 'Approve deployment to Dev?', ok: 'Deploy'
            }
        }

        stage('Deploy to Dev') {
            when {
                expression { return params.DEPLOY_TO_DEV }
            }
            steps {
                sh '''
                    echo "Setting up kubectl context for Dev environment..."
                    kubectl config current-context
                    kubectl get nodes
                    
                    echo "Applying Kubernetes manifests..."
                    kubectl apply -f deploy/dev/namespace.yaml
                    kubectl apply -f deploy/dev/deployment.yaml
                    kubectl apply -f deploy/dev/service.yaml

                    echo "Updating deployment image..."
                    kubectl set image deployment/calc-demo \
                      calc-demo=${DOCKER_IMAGE}:${IMAGE_TAG} \
                      -n dev

                    kubectl rollout status deployment/calc-demo -n dev --timeout=120s
                    kubectl get pods -n dev
                    kubectl get svc -n dev
                '''
            }
        }
    }
    
    post {
        success {
            echo "Pipeline completed successfully for ${params.ENVIRONMENT} Env and executed by ${params.EXECUTOR_NAME}"
            
        }
        failure {
            echo "Pipeline failed"
        }
    }
}