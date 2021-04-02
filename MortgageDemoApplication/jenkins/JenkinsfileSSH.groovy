// Sample Jenkinsfile for testing using SSH and a minimal Agent Setup
// Define some basic variables
properties([pipelineTriggers([githubPush()])])
def zAgent       = 'brice-st-node'
def myJenkinsID  = 'briceGithub'             // My Jenkins ID that points to my Git credentials
def myApp        = 'MortgageApplication'
def zAppBuild    = "~/dbb-zappbuild/buildV2.groovy"
def zAppPkg      = "~/dbb-zappbuild/deployV2.groovy"
def pdsHLQ       = "BSMALL.JENKINS.PIPELINE"
def buzzTool     = "/u/nlopez/ucd/Tass-agent/bin/buztool.sh"
def ucdComponent = "brice-test-component"
def jobRepo      = ""
def gitBranch    = "Devl"

//Start of Script
pipeline {
	// point to the USS Agent and skip Git auto-checkout.
	agent  { label zAgent }
	options { skipDefaultCheckout(true) }

	// Some vars for tracing
	environment {
		GIT_TRACE='false'             // Trace git for testing  true/false,1,2
		GIT_TRACE_SETUP='true'        // really cool trace tools
		myL4J='ON'                    // DBB log4J with Hook ON/OFF
		JAVA_HOME='/usr/lpp/java/J8.0_64/'
		LIB_PATH='/var/dbb/lib/'
		_BPX_SHAREAS='NO'
	  	//PATH='$PATH:/var/dbb100FIX/usr/lpp/IBM/dbb/bin:/var/dbb100FIX/usr/lpp/IBM/dbb/lib:/var/zoautil100FIX/usr/lpp/IBM/zoautil/bin'
	}



	// The Declarative pipeline groovy script starts here as steps within a stage
	// For more details see https://www.jenkins.io/doc/book/pipeline/syntax/
	stages {
		 stage('init') {
			steps {
				println '** Init Step: Setting up a Git Env with SSH Credentials stored in Jenkins'
				sh "env"

				// Get this job's repo, parse it to derive Git ID and DOM (can support many repos)
				// Use a script block to provide more advanced groovy coding
				script {
//					println "***RBS: Getting getUserRemoteConfigs"
//					def jobRepo = scm.getUserRemoteConfigs()[0].getUrl()
					env.jobRepo = "git@github.ibm.com:brice/MortgageV2.git"
					def nodes   = env.jobRepo.split("/")
					env.wkDir   = nodes[1].split(".git")[0]  //'dbb-zappbuild'   //nodes[4].split('.git')[0]

					nodes = nodes[0].split(":")
					def gitDom  = nodes[1]                // get the domain
					def gitID  = nodes[0].split("@")[1]  //'github.ibm.com'  //nodes[3]

					println "***RBS: jobRepo - ${env.jobRepo}"
					println "***RBS: wkDir   - ${wkDir}"
					println "***RBS: gitDom  - ${gitDom}"
					println "***RBS: gitID   - ${gitID}"
					}
				}
			}
		
		stage('Cleanup Workspace') {
			steps {
				sh "pwd"  //cleanWs()
				deleteDir()
				println "** Cleaned Up Workspace For Project"
			}
		}
		
		stage('Clone Repository') {
			steps {
				// Use my Jenkins ID to access SSH
				withCredentials([sshUserPrivateKey(credentialsId: 'briceGithub', keyFileVariable: 'sshKeyFile', passphraseVariable: '',
					usernameVariable: 'myJenkinsID')]) {

					// Use echo to write the Git Pat and ID in USS as a global helper store
					println  "**  Using Git Credentials stored for Jenkins ID $myJenkinsID  for repo ${env.jobRepo}"
					sh "git config --global credential.helper store"
//                        sh "echo git@${gitID}:${myJenkinsID}@${gitDom}  > ~/.git-credentials"

//                        sh "rm -r " + env.wkDir  // " 2>&1"
					sh "if test -d " + env.wkDir + "; then /bin/rm -r " + env.wkDir + "; fi"

					sh "git -c http.sslVerify=false clone -b ${gitBranch} ${env.jobRepo}"
//                        sh "/bin/rm ~/.git-credentials"
				}
			}
		}

		stage('Impact Build') {
			steps {
				  println  '** Building..'
				  sh "ls  "
				  sh "${env.DBB_HOME}/bin/groovyz " + zAppBuild + " -w " + env.wkDir + " -a " + myApp + " -o ${WORKSPACE}/buildlogs/build-${BUILD_NUMBER} -h " + pdsHLQ + " -id ADMIN -pw ADMIN --impactBuild"
//                  sh "${env.DBB_HOME}/bin/groovyz " + zAppBuild + " -w " + env.wkDir + " -a " + myApp + " -o dbb-logs -h " + env.USER + "." + env.diffMode + " -url https://9.160.132.167:9443/dbb/ -id ADMIN -pw ADMIN --impactBuild "
			}
		}

		stage('Packaging Artifacts') {
			steps {
				  println  '** Packaging..'
				  sh "${env.DBB_HOME}/bin/groovyz " + zAppPkg + " -b " + buzzTool + " -c " + ucdComponent + " -w ${WORKSPACE}/buildlogs/build-${BUILD_NUMBER} -v Brice_Pipeline.build-${BUILD_NUMBER}"
			}
		}
	}

	post {
		always {
			script {
				try {
					println '** Cleanup...'
					//println '*** Removed Repo stored in ' + env.wkDir
					//sh "rm -r " + env.wkDir
					sh "if test -f ~/.git-credentials; then /bin/rm ~/.git-credentials; fi"
				} 
				catch ( Exception ex ) {
					println "!** Error in post step"
				}
			}
		}
	}
}
