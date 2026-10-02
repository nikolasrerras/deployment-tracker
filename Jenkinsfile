
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
                sh '''
                    docker build \
                        -t "deployment-tracker:${IMAGE_TAG}" \
                        .
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    set -eu

                    ENV_FILE="/home/nikolas/Projects/deployment-tracker/.env"

                    test -f "$ENV_FILE"

                    docker compose \
                        --project-name deployment-tracker \
                        --env-file "$ENV_FILE" \
                        up -d \
                        --no-deps \
                        --no-build \
                        --pull never \
                        api
                '''
            }
        }

        stage('Smoke Test') {
            steps {
                retry(12) {
                    sleep(time: 5, unit: 'SECONDS')

                    sh '''
                        set -eu

                        response="$(curl -fsS --max-time 3 \
                            http://127.0.0.1:8080/api/health)"

                        test "$response" = '{"status":"UP"}'

                        curl -fsS --max-time 3 \
                            http://127.0.0.1:8080/api/applications
                    '''
                }
            }
        }

        stage('Docker Image Cleanup') {
            steps {
                sh '''
                    set -eu

                    echo "Cleaning up old CI image tags..."

                    ACTIVE_IMAGE="$(docker inspect \
                        --format '{{.Config.Image}}' \
                        deployment-tracker-api)"

                    docker image ls \
                        --filter 'reference=deployment-tracker:ci-*' \
                        --format '{{.Repository}}:{{.Tag}}' \
                        | grep -E '^deployment-tracker:ci-[0-9]+$' \
                        | sort -t '-' -k3,3nr \
                        | tail -n +4 \
                        | while IFS= read -r IMAGE; do

                            if [ "$IMAGE" = "$ACTIVE_IMAGE" ]; then
                                echo "Keeping active image: $IMAGE"
                                continue
                            fi

                            echo "Removing old tag: $IMAGE"

                            docker image rm "$IMAGE" \
                                || echo "Could not remove: $IMAGE"
                        done

                    echo "Cleanup completed."
                '''
            }
        }
    }
}
