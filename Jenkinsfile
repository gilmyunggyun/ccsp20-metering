def label = "worker-${env.JOB_NAME}${env.BUILD_NUMBER}"

podTemplate(label: label,
containers: [
  containerTemplate(name: 'gradle', image: 'gradle:jdk17-alpine', command: 'cat', ttyEnabled: true)
],
volumes: [
   persistentVolumeClaim(claimName: 'gradle-shared', mountPath: '/home/gradle/.gradle')
]) {
  node(label) {
     properties([
       pipelineTriggers([
        [$class: 'GenericTrigger',
        genericVariables: [
          [key: 'user_name', value: '$.user_name'],
          [key: 'checkout_sha', value: '$.checkout_sha'],
          [key: 'web_url', value: '$.project.web_url'],
          [key: 'ref', value: '$.ref'],
          [key: 'tag', value: '$.ref', regexpFilter: 'refs/tags/'],
        ],
         causeString: '$user_name pushed tag $tag to $web_url referencing $checkout_sha',
         token: "ccsp20-metering",
         printContributedVariables: true,
         printPostContent: true,
         silentResponse: false,
         regexpFilterText: '$ref',
         regexpFilterExpression: '^refs/tags/.*'
        ]
       ])
      ])

    def myRepo = checkout scm
    def gitCommit = myRepo.GIT_COMMIT
    def gitBranch = myRepo.GIT_BRANCH
    def shortGitCommit = "${gitCommit[0..10]}"
    def previousGitCommit = sh(script: "git rev-parse ${gitCommit}~", returnStdout: true)

     stage('Test') {
      if(gitBranch == 'develop'){
        try {
          container('gradle') {
            sh """
               pwd
               echo "GIT_BRANCH=${gitBranch}" >> /etc/environment
               echo "GIT_COMMIT=${gitCommit}" >> /etc/environment
               gradle test
               """
          }
        }catch (exc) {
          println "Failed to test - ${currentBuild.fullDisplayName}"
          throw(exc)
        }
      }
    }

    stage('Build') {
     if(gitBranch == 'develop'){
        try {
          container('gradle') {
            sh """
               pwd
               echo "GIT_BRANCH=${gitBranch}" >> /etc/environment
               echo "GIT_COMMIT=${gitCommit}" >> /etc/environment
               gradle test
               """
          }
        }catch (exc) {
          println "Failed to test - ${currentBuild.fullDisplayName}"
          throw(exc)
        }
      }
    }

    stage('Bake') {
      try {
        container('gradle') {
          sh "gradle jib"
        }


      }catch (exc) {
        println "Failed to test - ${currentBuild.fullDisplayName}"
        throw(exc)
      }
    }
  }
}