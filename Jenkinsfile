
pipeline {
    agent any
    tools {
        jdk 'JDK 21'
        maven 'Maven 3'
    }
    environment {
        APP_NAME = 'dicestore'
        JAR_NAME = 'dicestore-0.0.1-SNAPSHOT.jar'
        DOCKER_IMAGE = 'victoruk11/dicestore'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }
        stage('Build') {
            steps {
                echo 'Compiling project...'
                sh 'mvn clean compile -Dspring.profiles.active=test'
            }
        }
        stage('Test') {
            steps {
                echo 'No tests yet - skipping...'
                sh 'mvn test -Dspring.profiles.active=test -DskipTests'
            }
        }
        stage('Package') {
            steps {
                echo 'Packaging the application...'
                sh 'mvn package -DskipTests -Dspring.profiles.active=test'
            }
        }
        stage('Build & Push Docker Image') {
            steps {
                echo 'Building and pushing Docker image with Jib...'
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    sh '''
                        mvn compile jib:build \
                            -Djib.to.image=docker.io/victoruk11/dicestore \
                            -Djib.to.tags=$BUILD_NUMBER \
                            -Djib.to.auth.username=$DOCKER_USERNAME \
                            -Djib.to.auth.password=$DOCKER_PASSWORD \
                            -Dspring.profiles.active=test \
                            -DskipTests
                    '''
                }
            }
        }
        stage('Archive JAR') {
            steps {
                echo 'Archiving build artifact...'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        stage('Deploy to EC2') {
            steps {
                echo 'Deploy stage placeholder - will configure after EC2 setup...'
            }
        }
    }
    post {
        success {
            echo "BUILD SUCCESSFUL - ${APP_NAME} pipeline completed!"
        }
        failure {
            echo "BUILD FAILED - Check Jenkins logs."
        }
        always {
            cleanWs()
        }
    }
}









// pipeline {
//     agent any
//     tools {
//         jdk 'JDK 21'
//         maven 'Maven 3'
//     }
//     environment {
//         APP_NAME = 'dicestore'
//         JAR_NAME = 'dicestore-0.0.1-SNAPSHOT.jar'
//         DOCKER_IMAGE = 'victoruk11/dicestore'
//         IMAGE_TAG = "${BUILD_NUMBER}"
//     }
//     stages {
//         stage('Checkout') {
//             steps {
//                 echo 'Checking out source code...'
//                 checkout scm
//             }
//         }
//         stage('Build') {
//             steps {
//                 echo 'Compiling project...'
//                 sh 'mvn clean compile -Dspring.profiles.active=test'
//             }
//         }
//         stage('Test') {
//             steps {
//                 echo 'No tests yet - skipping...'
//                 sh 'mvn test -Dspring.profiles.active=test -DskipTests'
//             }
//         }
//         stage('Package') {
//             steps {
//                 echo 'Packaging the application...'
//                 sh 'mvn package -DskipTests -Dspring.profiles.active=test'
//             }
//         }
//         stage('Build & Push Docker Image') {
//             steps {
//                 echo 'Building and pushing Docker image with Jib...'
//                 withCredentials([usernamePassword(
//                     credentialsId: 'dockerhub-credentials',
//                     usernameVariable: 'DOCKER_USERNAME',
//                     passwordVariable: 'DOCKER_PASSWORD'
//                 )]) {
//                     sh """
//                         mvn compile jib:build \
//                             -Djib.to.image=docker.io/${DOCKER_IMAGE} \
//                             -Djib.to.tags=${IMAGE_TAG} \
//                             -Djib.to.auth.username=${DOCKER_USERNAME} \
//                             -Djib.to.auth.password=${DOCKER_PASSWORD} \
//                             -Dspring.profiles.active=test \
//                             -DskipTests
//                     """
//                 }
//             }
//         }
//         stage('Archive JAR') {
//             steps {
//                 echo 'Archiving build artifact...'
//                 archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
//             }
//         }
//         stage('Deploy to EC2') {
//             steps {
//                 echo 'Deploy stage placeholder - will configure after EC2 setup...'
//             }
//         }
//     }
//     post {
//         success {
//             echo "BUILD SUCCESSFUL - ${APP_NAME} pipeline completed!"
//         }
//         failure {
//             echo "BUILD FAILED - Check Jenkins logs."
//         }
//         always {
//             cleanWs()
//         }
//     }
// }
