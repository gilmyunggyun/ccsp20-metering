def label = "worker-${env.JOB_NAME}-${env.BUILD_NUMBER}"

podTemplate(label: label,
  containers: [
    containerTemplate(name: 'gradle', image: 'gradle:7.5.1-jdk17-alpine', command: 'cat', ttyEnabled: true)
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

    stage("Upload Artifact"){
      container('gradle') {
        sh "gradle publish -x test"
      }
    }

    stage("Baking Docker"){
      container('gradle') {
        sh "gradle jib -x test"
      }
    }

    stage("Publish Helm Chart"){
      container('gradle') {
        sh "gradle publishChart"
      }
    }
  }
}