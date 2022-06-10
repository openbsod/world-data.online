def createNamespace () {
    echo "Creating namespace ${ISTIO_NS} if needed ( --dry-run )"
    sh "[ ! -z \"\$(kubectl get ns ${ISTIO_NS} -o name 2>/dev/null)\" ] || kubectl create ns '${ISTIO_NS}'"
}

pipeline {

    options {
        timeout(time: 10, unit: 'MINUTES')
    }

    environment {
        DEPLOY_PROD = true
    }

    parameters {
        string       (name: 'GIT_BRANCH',     defaultValue: 'adminstream',   description: 'Git branch to build')
        string       (name: 'ISTIO_NS',       defaultValue: 'istio-system',  description: 'Namespace for Istio components')
        string       (name: 'ISTIO_VERSION',  defaultValue: '1.13.3',        description: 'Istio version')
        string       (name: 'ISTIO_PATH',     defaultValue: '/var/jenkins_home/workspace/Istio/istio-1.11.2/bin',                          description: 'Istio path to istioctl')
        booleanParam (name: 'DEPLOY_TO_PROD', defaultValue: true,            description: 'If build and tests are good, proceed and deploy to production without manual approval')
    }

    agent { node { label 'built-in' } }

    stages {

        ////////// Step 1 //////////
        stage('Git clone') {
            steps {
                echo "Check out"
                git branch: "adminstream",
                        // credentialsId: 'bitbucket',
                        url: "ssh://git@bitssh.adminstream.cloud/ad/istio-${ISTIO_VERSION}.git"

                // Validate kubectl
                sh "kubectl cluster-info"

                // Validate istioctl
                sh "'${ISTIO_PATH}'/istioctl version"

                echo "ISTIO_VERSION is ${ISTIO_VERSION}"

                script {
                    branch = GIT_BRANCH.replaceAll('/', '-').replaceAll('\\*', '-')
                    ID = "${ISTIO_VERSION}-${branch}"

                    echo "Global ID set to ${ID}"
                }
            }
        }

        ////////// Step 2 //////////
        // Waif for user manual approval or proceed automatically if DEPLOY_TO_PROD is true
        stage('Go for Production') {
            when {
                allOf {
                    environment name: 'GIT_BRANCH', value: 'adminstream'
                    environment name: 'DEPLOY_TO_PROD', value: 'true'
                }
            }

            steps {
                // Prevent any older builds from deploying to production
                milestone(1)
                input 'Proceed and deploy to Production'
                milestone(2)

                script {
                    DEPLOY_PROD = true
                }
            }
        }

        stage('Deploy to Production') {
            when {
                anyOf {
                    expression { DEPLOY_PROD == true }
                    environment name: 'DEPLOY_TO_PROD', value: 'true'
                }
            }

            steps {
                script {
                    DEPLOY_PROD = true
                    namespace = '${ISTIO_NS}'

                    echo "Creating ${ISTIO_NS} namespace"
                    createNamespace ()

                    // Deploy
                    echo "Check whether ${ISTIO_NS} namespace exists"
                    sh "kubectl get ns"

                    // Deploy
                    echo "Switch context to ${ISTIO_NS} namespace"
                    sh "kubectl config set-context --current --namespace='${ISTIO_NS}'"

                    echo "Istio base chart which contains cluster-wide resources used by the Istio control plane"
                    sh "/usr/local/bin/helm install istio-base ./manifests/charts/base -n '${ISTIO_NS}'"

                    echo "Idle for 5 seconds"
                    sh "sleep 5"

                    echo "Istio discovery chart which deploys the istiod service"
                    sh "/usr/local/bin/helm install istiod ./manifests/charts/istio-control/istio-discovery --set values.global.jwtPolicy=first-party-jwt -n '${ISTIO_NS}'"

                    echo "Idle for 5 seconds"
                    sh "sleep 5"

                    echo "Istio ingress gateway chart which contains the ingress gateway components"
                    sh "/usr/local/bin/helm install istio-ingress ./manifests/charts/gateways/istio-ingress -n '${ISTIO_NS}'"

                    echo "Idle for 5 seconds"
                    sh "sleep 5"

                    echo "Istio egress gateway chart which contains the egress gateway components"
                    sh "/usr/local/bin/helm install istio-egress ./manifests/charts/gateways/istio-egress -n '${ISTIO_NS}'"

                    echo "Idle for 5 seconds"
                    sh "sleep 5"

                    echo "Prometheus addon"
                    sh "kubectl -n '${ISTIO_NS}' apply -f samples/addons/prometheus.yaml"

                    echo "Kiali addon"
                    sh "kubectl -n '${ISTIO_NS}' apply -f samples/addons/kiali.yaml"

                    echo "Grafana addon"
                    sh "kubectl -n '${ISTIO_NS}' apply -f samples/addons/grafana.yaml"

                    echo "Idle for 30 seconds"
                    sh "sleep 30"

                    echo "Get HELM charts"
                    sh "/usr/local/bin/helm ls -n istio-system"

                    echo "Get ISTIO pods"
                    sh "kubectl -n '${ISTIO_NS}' get pods -o wide"

                }
            }
        }
   }
}