def label = "worker-${env.JOB_NAME}-${env.BUILD_NUMBER}"

podTemplate(label: label,
  containers: [
    containerTemplate(name: 'gradle', image: 'gradle:7.4.1-jdk17-alpine', command: 'cat', ttyEnabled: true)
  ],
  volumes: [
     persistentVolumeClaim(claimName: 'gradle-shared', mountPath: '/home/gradle/.gradle')
  ]) {
  node(label) {
    properties([
      pipelineTriggers([
        [$class: 'GenericTrigger',
          genericVariables: [
            [key: 'user_name', value: '$.user_name'], [key: 'checkout_sha', value: '$.checkout_sha'],
            [key: 'ref', value: '$.ref'], [key: 'tag', value: '$.ref', regexpFilter: 'refs/tags/'],
            [key: 'event', value: '$.event_name']
          ], causeString: '$ref-$user_name:$checkout_sha', token: "ccsp20-metering",
          printContributedVariables:false, printPostContent: false, silentResponse: true,
          regexpFilterText: '$ref', regexpFilterExpression: 'refs/heads/' + BRANCH_NAME
        ]
      ])
    ])


    def repo = checkout scm
    def commit = repo.GIT_COMMIT
    def branch = repo.GIT_BRANCH
    def short_commit = "${commit[0..10]}"

    stage("Checking Quality"){
      container('gradle') {
        if(branch == 'release' || branch == 'develop') {
          sh "gradle check"
        }
      }
    }

    stage("Baking Docker"){
      container('gradle') {
        sh "gradle jib -x test"
      }
    }

    stage("Upload Artifact"){
      container('gradle') {
        sh "gradle publish -x test"
      }
    }
  }
}