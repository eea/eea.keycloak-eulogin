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

        stage('Build Docker Images') {
            steps {
                script {
                    echo 'Keycloak with EULogin Plugin'
                    def app
                    app = docker.build("k8s-swi001:5000/keycloak-eulogin:3.0", "--build-arg project_version=3.0.0-SNAPSHOT ./target")
                    app.push()                    
                }    
            }
        }
        
        
    }
}