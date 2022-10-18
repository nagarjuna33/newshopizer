pipeline {
 agent { label 'SHOPIZER'}
stages {
    stage('SCM') {
        steps {
           git url: 'git@github.com:nagarjuna33/newshopizer.git'
        }
    }
    stage('git'){
        steps{
            sh 'git checkout developer'
            sh 'git checkout master'
            sh 'git branch -a'
            sh 'git merge release --no-ff'
            sh 'git push -u origin master'
        }
    }
    stage ('Archive JUnit test') {
        steps{
            junit testResults : '**/surefire-reports/*.xml'
        }
    }
    stage('artifactorydeploy') {
        steps {
            rtMavenDeployer  id : 'MAVEN_DEPLOYER',
                    releaseRepo: 'default-libs-release-local',
                    snapshotRepo: 'default-libs-snapshot-local',
                       serverId : 'Shopizer-Artifact'
        }
    }
        stage('artifactoryrun') {
        steps {
            rtMavenRun        pom : 'pom.xml',
                            goals : 'package',
                        deployerId: "MAVEN_DEPLOYER"
        }
    }
  }
}
