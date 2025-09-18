pipeline {
    agent any

    tools {
        jdk 'jdk_17'        // correspond au nom configuré dans Jenkins > Tools
        maven 'Maven_3.9.3'   // idem
    }

    environment {
        HEADLESS = 'true'   // par défaut, on lance en mode headless dans Jenkins
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh "mvn clean test -Dheadless=${HEADLESS}"
            }
            post {
                always {
                    // Récupérer les résultats de tests JUnit
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
    }
}
