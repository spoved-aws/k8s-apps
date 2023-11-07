//ANSIBLE : 172.31.27.231
//Jenkins : 172.31.27.163


node{
    stage("Git checkout"){
        git branch: 'main', url: 'https://github.com/spoved-aws/devops-webapp-1.git'
        
    }
    
    stage("Sending docker file to ansible using ssh"){
        sshagent(['ansible-demo']) {
            
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.27.231'
            sh 'scp -r /var/lib/jenkins/workspace/app-01-pipeline ubuntu@172.31.27.231:/home/ubuntu/'
    
        }
    }
    
    stage("Docker image building"){
        sshagent(['ansible-demo']) {
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.27.231 cd /home/ubuntu/app-01-pipeline/'
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.27.231 docker image build -t $JOB_NAME:v1.$BUILD_ID .'
        }
        
    }
    
    stage("Docker image tagging"){
        sshagent(['ansible-demo']) {
            //sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.27.231 cd /home/ubuntu/'
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.27.231 docker image tag $JOB_NAME:v1.$BUILD_ID kanukhosla10/$JOB_NAME:v1.$BUILD_ID'
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.27.231 docker image tag $JOB_NAME:v1.$BUILD_ID kanukhosla10/$JOB_NAME:latest'
        
        }
        
    }
    
    stage("Push docker images Dockerhub"){
        sshagent(['ansible-demo']) {
            withCredentials([string(credentialsId: '470dc790-c145-4d5e-887b-a678edbb6f2e', variable: 'dockerhub_pass')]) {
    // some block
            sh "ssh -o StrictHostKeyChecking=no ubuntu@172.31.27.231 docker login -u kanukhosla10 -p ${dockerhub_pass}"
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.27.231 docker image push kanukhosla10/$JOB_NAME:v1.$BUILD_ID'
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.27.231 docker image push kanukhosla10/$JOB_NAME:latest'
            
            }
        }
        
    }


    stage("Run Ansible to configure the deployment and service in minikube"){
        sshagent(['ansible-demo']) {
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.27.231 cd /home/ubuntu/app-01-pipeline/'
            sh 'ssh -o StrictHostKeyChecking=no ubuntu@172.31.27.231 ansible-playbook playbook_app01.yml -i inventory.yml'
        }
        
    }



}