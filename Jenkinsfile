
pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
        skipDefaultCheckout(true)
    }

    environment {
        IMAGE_TAG = "ci-${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            environment {
                POSTGRES_PASSWORD = 'ci-test-placeholder'
            }

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
                sh 'docker build -t "deployment-tracker:${IMAGE_TAG}" .'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    test -f /home/nikolas/Projects/deployment-tracker/.env

                    docker compose \
                        --project-name deployment-tracker \
                        --env-file /home/nikolas/Projects/deployment-tracker/.env \
                        up -d --no-deps --no-build --pull never api
                '''
            }
        }

        stage('Smoke Test') {
            steps {
                retry(12) {
                    sleep(time: 5, unit: 'SECONDS')

                    sh '''
                        response="$(curl -fsS --max-time 3 \
                            http://127.0.0.1:8080/api/health)"

                        test "$response" = '{"status":"UP"}'

                        curl -fsS --max-time 3 \
                            http://127.0.0.1:8080/api/applications
                    '''
                }
            }
        }
    }
}
