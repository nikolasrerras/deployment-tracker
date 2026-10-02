
pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        POSTGRES_PASSWORD = 'ci-test-placeholder'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh './mvnw -B -ntp clean verify'
            }

            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t deployment-tracker:ci-${BUILD_NUMBER} .'
            }
        }
    }
}
