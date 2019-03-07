/*
 * Copyright (c) Oscaro 2019 All rights reserved
 * This file and the information it contains are property of Oscaro and confidential.
 * They shall not be reproduced nor disclosed to any person except to those having
 * a need to know them without prior written consent of Oscaro.
 */

node {

    try {

        gitlabCommitStatus {

            def gitEnv = checkout(scm)

            def version = leinVersion()
            def buildNum = env.BUILD_NUMBER
            def branchName = gitEnv.GIT_BRANCH ?: branchName()

            echo """\
Git vars are $gitEnv
Version is '$version'
Build number is '$buildNum'
Branch name is '$branchName'"""
            sh 'printenv'

            lein 'sub clean'

            stage('Core') {
                dir('core') {
                    lein 'test'
                    lein 'jar'
                    lein 'deploy'
                }
            }

            stage('Prometheus') {
                dir('prometheus') {
                    lein '-U deps'
                    lein 'test'
                    lein 'jar'
                    lein 'deploy'
                }
            }
        }
    } finally {
        cleanWs()
    }
}
