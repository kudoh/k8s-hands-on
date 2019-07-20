# serverless app

## Istio

https://github.com/knative/docs/blob/master/docs/install/installing-istio.md#installing-istio-without-sidecar-injection

### download istio resource

```bash
export ISTIO_VERSION=1.1.7
curl -L https://git.io/getLatestIstio | sh -
cd istio-${ISTIO_VERSION}
```

### Install

```bash
for i in install/kubernetes/helm/istio-init/files/crd*yaml; do kubectl apply -f $i; done
kubectl create ns istio-system
kubectl label ns istio-system istio-injection=disabled

helm template --namespace=istio-system \
  --set prometheus.enabled=false \
  --set mixer.enabled=false \
  --set mixer.policy.enabled=false \
  --set mixer.telemetry.enabled=false \
  `# Pilot doesn't need a sidecar.` \
  --set pilot.sidecar=false \
  --set pilot.resources.requests.memory=128Mi \
  `# Disable galley (and things requiring galley).` \
  --set galley.enabled=false \
  --set global.useMCP=false \
  `# Disable security / policy.` \
  --set security.enabled=false \
  --set global.disablePolicyChecks=true \
  `# Disable sidecar injection.` \
  --set sidecarInjectorWebhook.enabled=false \
  --set global.proxy.autoInject=disabled \
  --set global.omitSidecarInjectorConfigMap=true \
  `# Set gateway pods to 1 to sidestep eventual consistency / readiness problems.` \
  --set gateways.istio-ingressgateway.autoscaleMin=1 \
  --set gateways.istio-ingressgateway.autoscaleMax=1 \
  `# Set pilot trace sampling to 100%` \
  --set pilot.traceSampling=100 \
  install/kubernetes/helm/istio \
  > ./istio-lean.yaml

kubectl apply -f istio-lean.yaml

```

## Knative

```bash
kubectl apply --selector knative.dev/crd-install=true \
  -f https://github.com/knative/serving/releases/download/v0.7.0/serving.yaml \
  -f https://github.com/knative/build/releases/download/v0.7.0/build.yaml \
  -f https://github.com/knative/eventing/releases/download/v0.7.0/release.yaml \
  -f https://github.com/knative/serving/releases/download/v0.7.0/monitoring.yaml

kubectl apply -f https://github.com/knative/serving/releases/download/v0.7.0/serving.yaml --selector networking.knative.dev/certificate-provider!=cert-manager \
  -f https://github.com/knative/build/releases/download/v0.7.0/build.yaml \
  -f https://github.com/knative/eventing/releases/download/v0.7.0/release.yaml \
  -f https://github.com/knative/serving/releases/download/v0.7.0/monitoring.yaml

kubectl get pods --namespace knative-serving
kubectl get pods --namespace knative-build
kubectl get pods --namespace knative-eventing
kubectl get pods --namespace knative-monitoring
```

## application
