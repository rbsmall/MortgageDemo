// Sample Jenkinsfile for testing using SSH and a minimal Agent Setup
// Define some basic variables
properties([pipelineTriggers([githubPush()])])
def zAgent       = 'brice-st-node'
def myJenkinsID  = 'briceGithub'             // My Jenkins ID that points to my Git credentials
def myApp        = 'MortgageApplication'
def zAppBuild    = "~/dbb-zappbuild/buildV2.groovy"
def zAppPkg      = "~/dbb-zappbuild/deployV2.groovy"
def pdsHLQ       = "BRICE.JENKINS.PIPELINE"
def buzzTool     = "/u/nlopez/ucd/Tass-agent/bin/buztool.sh"
def ucdComponent = "brice-test-component"
def jobRepo      = "git@github.ibm.com:brice/MortgageV2.git"
def pipelineName = "Brice_MBPipeline"
def gitCredId    = "briceGithub"

//Start of Script
pipeline {
	// point to the USS Agent and skip Git auto-checkout.
	agent  { label zAgent }
	options { skipDefaultCheckout(true) }

	// Some vars for tracing
	environment {
		GIT_TRACE       = 'false'               // Trace git for testing  true/false,1,2
		GIT_TRACE_SETUP = 'true'                // really cool trace tools
		myL4J         = 'ON'                    // DBB log4J with Hook ON/OFF
		JAVA_HOME     = '/usr/lpp/java/J8.0_64/'
		LIB_PATH      = '/var/dbb/lib/'
		_BPX_SHAREAS  = 'NO'
		jobRepo       = "git@github.ibm.com:brice/mortgageV2.git"
}


	// The Declarative pipeline groovy script starts here as steps within a stage
	// For more details see https://www.jenkins.io/doc/book/pipeline/syntax/
	stages {
		stage('init') {
			 when {
				 expression {
					 return env.BRANCH_NAME != "master"
				 }
			 }
			 steps {
				println '** Init Step: Setting up a Git Env'
				sh "env"

				script {   
					def nodes   = env.jobRepo.split("/")
					env.wkDir   = nodes[1].split(".git")[0]  

					nodes = nodes[0].split(":")
					def gitDom  = nodes[1]                
					def gitID  = nodes[0].split("@")[1]   
				}
			}
		}
		
		stage('Cleanup Workspace') {
			when {
				expression {
					return env.BRANCH_NAME != "master"
				}
			}
			steps {
				sh "pwd"  //cleanWs()
				deleteDir()
				println "** Cleaned Up Workspace For Project"
			}
		}
		
		stage('Clone Repository') {
			when {
				expression {
					return env.BRANCH_NAME != "master"
				}
			}
			steps {
				script {
					dir('mortgageV2') {
						sh(script: 'rm -f .git/info/sparse-checkout', returnStdout: true)
						srcGitRepo   = env.jobRepo
						srcGitBranch = env.BRANCH_NAME
						def scmVars  = null
						scmVars = checkout([$class: 'GitSCM', branches: [[name: srcGitBranch]],
											doGenerateSubmoduleConfigurations: false,
											submoduleCfg: [],
											userRemoteConfigs: [[credentialsId: gitCredId, url: env.jobRepo]]])
					}		
				}						
			}
		}

		stage('Impact Build') {
			when {
				expression {
					return env.BRANCH_NAME != "master"
				}
			}
			steps {
				  println  '** Building..'
				  sh "pwd"
				  sh "ls  "
				  sh "${env.DBB_HOME}/bin/groovyz -Dlog4j.configurationFile=file:/var/dbb109/conf/log4j2.properties ${zAppBuild} -w ${env.wkDir} -a ${myApp} -o ${WORKSPACE}/buildlogs/build-${BUILD_NUMBER} -h ${pdsHLQ} -id ADMIN -pw ADMIN --impactBuild -v"
			}
		}

		stage('Packaging Artifacts') {
			when {
				expression {
					return env.BRANCH_NAME != "master"
				}
			}
			steps {
				  println  '** Packaging..'
				  sh "${env.DBB_HOME}/bin/groovyz -Dlog4j.configurationFile=file:/var/dbb109/conf/log4j2.properties ${zAppPkg} -b ${buzzTool} -c ${ucdComponent} -w ${WORKSPACE}/buildlogs/build-${BUILD_NUMBER} -v ${pipelineName}.branch-${env.BRANCH_NAME}.build-${BUILD_NUMBER}"
			}
		}
	}

	post {
		always {
			script {
				try {
					println '** Cleanup...'
					sh "if test -f ~/.git-credentials; then /bin/rm ~/.git-credentials; fi"
				} 
				catch ( Exception ex ) {
					println "!** Error in post step"
				}
			}
		}
	}
}
