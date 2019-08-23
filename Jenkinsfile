pipeline {

    agent {
        label 'java8'
    }
    stages {
        stage('Preparation') {
            steps {
                sh 'echo "Starting CI/CD Pipeline"'                
            }
        }
        stage('Compile JAVA') {
            steps {
                sh '''
                    mvn -Dmaven.test.failure.ignore=true -s '/home/jenkins/.m2/settings.xml' clean install
                '''                                
              
            }
        }
        // stage('Static Code Analysis') {
        //     steps {
        //         withSonarQubeEnv('Altia SonarQube') {
        //             // requires SonarQube Scanner for Maven 3.2+
        //             sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -P sonar -Dsonar.jenkins.branch=' + env.BRANCH_NAME.replace('/', '_')
        //         }
        //     }
        // }

        
        stage('Install in Nexus') {
            
            steps {
                sh '''
                    mvn -Dmaven.test.skip=true -s '/home/jenkins/.m2/settings.xml' deploy
                '''
            }
        }

        // stage('Build Docker Images') {
        //     steps {
        //         script {
        //             echo 'Dataflow Service'
        //             def app
        //             app = docker.build("k8s-swi001:5000/dataflow-service:1.0", "--build-arg JAR_FILE=target/dataflow-service-1.0-SNAPSHOT.jar --build-arg MS_PORT=8020 .")
        //             app.push()                    
        //         }    
        //     }
        // }
        
        
    }
}