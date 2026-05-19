pipeline {
    agent any
    
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['Development', 'Staging/UAT', 'Production'], description: 'Select the environment to run the pipeline')
        string(name: 'EXECUTOR_NAME', defaultValue: '', description: 'Enter Your Name Please', trim: true)
    }
    
    tools {
        maven "MAVEN-3.9"   // keep exact name as configured in Jenkins
    }
    
    environment {
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk-amd64'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        SONAR_ORGANIZATION = "teckexplorers"
        SONAR_PROJECTKEY = "teckexplorers_dev-demo"
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
                sh 'mvn clean test'
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
        
        stage("6. PACKAGE") {
            steps {
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
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