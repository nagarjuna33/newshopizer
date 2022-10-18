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
   stage('build'){
        steps{
}
    
}
