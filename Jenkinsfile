pipeline {
  agent {
    docker {
      args '-v /root/.m2:/root/.m2'
      image 'docker.premsiserv.com/verymc/maven:3.8.4-eclipse-temurin-16'
    }

  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn -B -DskipTests -s /usr/share/maven/ref/settings.xml clean install'
      }
    }

  }
  post {
    always {
      archiveArtifacts(artifacts: 'target/*.jar', fingerprint: true)
    }

  }
}