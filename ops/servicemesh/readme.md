# Service Mesh

## Istio

```bash
helm repo add istio.io https://storage.googleapis.com/istio-release/releases/1.2.5/charts/charts

kubectl create ns istio-system

ISTIO_VERSION=1.2.5
curl -L https://git.io/getLatestIstio | ISTIO_VERSION=$ISTIO_VERSION sh -
cd istio-$ISTIO_VERSION
helm upgrade istio-init --install install/kubernetes/helm/istio-init --namespace istio-system

# add istioctl to PATH variable
export PATH=$PWD/bin:$PATH

helm upgrade istio --install install/kubernetes/helm/istio --namespace istio-system \
  --set kiali.enabled=true

kubectl get svc,pod -n istio-system
```

## Auto Injection

```bash
# for app
kubectl create ns dev
kubectl label namespace dev istio-injection=enabled
# for redis
kubectl create ns redis
kubectl label namespace redis istio-injection=enabled
```

## App

```bash
GIHUB_USER=<your-github-user>
GITHUB_PASSWORD=<your-github-password>
# deploy github-service
kubectl create -n dev secret generic github-secret --from-literal user=${GITHUB_USER} --from-literal password=${GITHUB_PASSWORD}
kubectl apply -n dev \
  -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/github-service/service.yaml \
  -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/github-service/deployment.yaml

# deploy Redis by helm
kubectl create ns redis
helm upgrade redis --install stable/redis --namespace redis \
  --set master.persistence.enabled=false \
  --set cluster.enabled=false \
  --set password=frieza-redis-pass

# deploy api-gateway
kubectl apply -n dev \
  -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/secret.yaml \
  -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/configmap.yaml \
  -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/deployment.yaml \
  -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/service.yaml

# deploy repo-search-ui
kubectl apply -n dev \
  -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/repo-search-ui/configmap.yaml \
  -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/repo-search-ui/deployment.yaml \
  -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/repo-search-ui/service.yaml
```

## IngressGateway

```bash
# SSL Certificate
#openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout tls.key -out tls.crt -subj "/CN=frieza.local/O=Mamezou"

# chrome require SAN section, so use openssl1.1 but libressl
brew install openssl@1.1
export PATH="/usr/local/Cellar/openssl@1.1/1.1.1d/bin:$PATH"
openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout tls.key -out tls.crt -subj "/CN=frieza.local/O=Mamezou" \
  -addext 'subjectAltName = DNS:*.frieza.local'

kubectl create secret tls istio-ingressgateway-certs --key tls.key --cert tls.crt -n istio-system

export INGRESS_POD=$(kubectl get pod -n istio-system -l app=istio-ingressgateway -o jsonpath='{.items[0].metadata.name}')
kubectl exec -it -n istio-system $INGRESS_POD -- ls -al /etc/istio/ingressgateway-certs

# istio gateway/vs
kubectl apply -n dev -f gateway.yaml
istioctl proxy-config listener $INGRESS_POD  -n istio-system
istioctl proxy-config route $INGRESS_POD -o json -n istio-system

kubectl apply -n dev -f api-gateway-vs.yaml

# static entry
export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
echo "$INGRESS_HOST github.frieza.local" | sudo tee -a /etc/hosts

export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
export INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].port}')
export SECURE_INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="https")].port}')
```

## Canary Release

```bash
kubectl apply -n dev -f canary-release/apps.yaml
kubectl apply -n dev -f canary-release/gateway.yaml
kubectl apply -n dev -f canary-release/destinationrule.yaml
kubectl apply -n dev -f canary-release/gateway-vs-canary0.yaml

kubectl apply -n dev -f canary-release/gateway-vs-canary1.yaml

kubectl apply -n dev -f canary-release/gateway-vs-canary2.yaml

# check request routing
INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
curl -v -H 'Host:test.frieza.local' $INGRESS_HOST ;echo
```

## Fault Tolerance

### Timeout

```bash
kubectl apply -n dev -f timeout/

INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
curl -v -H 'Host:timeout.frieza.local' $INGRESS_HOST/sleep/1s ;echo
```

### Retry

```bash
kubectl apply -n dev -f retry/

INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
curl -v -H 'Host:retry.frieza.local' $INGRESS_HOST/retry ;echo
```

### Circuit Breaker

```bash
kubectl apply -n dev -f circuit-breaker/

POD=$(kubectl get pod -n dev -l app=circuit-breaking -o jsonpath='{.items[0].metadata.name}')
# dump envoy config
kubectl exec -it $POD -c istio-proxy -n dev  -- sh -c 'curl localhost:15000/config_dump'
# show envoy stats
# kubectl exec $POD -n dev -c istio-proxy -- pilot-agent request GET stats | grep circuit-breaking | grep pending
# using istioctl
# istioctl proxy-config endpoint $POD -n dev --cluster "outbound|8000||circuit-breaker.dev.svc.cluster.local"

INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
while true; do curl -s -H 'Host:circuit-breaking.frieza.local' $INGRESS_HOST; echo; sleep 1; done

hey -c 10 -z 20s --host circuit-breaking.frieza.local http://$INGRESS_HOST
```

### Flow Control

```bash
kubectl -n dev apply -f flow-controling/

INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# INGRESS_GW=$(kubectl -n istio-system get pod -l app=istio-ingressgateway -o jsonpath='{.items[0].metadata.name}')
# kubectl annotate pod $INGRESS_GW -n istio-system sidecar.istio.io/statsInclusionPrefixes='cluster.outbound,cluster_manager,listener_manager,http_mixer_filter,tcp_mixer_filter,server,cluster.xds-grpc'
# kubectl exec $INGRESS_GW -n istio-system -c istio-proxy -- pilot-agent request GET stats | grep flow-control | grep pending

hey -q 10 -n 100 --host flow-control.frieza.local http://$INGRESS_HOST/sleep/1s
```

## Security

### Authentication

```bash
# disable livenessProbe/readinessProbe
kubectl patch deploy -n dev github-service --type='json' \
  -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/readinessProbe"}]'
kubectl patch deploy -n dev github-service --type='json' \
  -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/livenessProbe"}]'
kubectl patch deploy -n dev api-gateway --type='json' \
  -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/readinessProbe"}]'
kubectl patch deploy -n dev api-gateway --type='json' \
  -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/livenessProbe"}]'
kubectl patch deploy -n dev repo-search-ui --type='json' \
  -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/readinessProbe"}]'
kubectl patch deploy -n dev repo-search-ui --type='json' \
  -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/livenessProbe"}]'

kubectl apply -f authentication/meshpolicy.yaml
kubectl apply -f authentication/mtls-destinationrule.yaml

kubectl run hacker --restart=Never -it --rm --generator run-pod/v1 --image tutum/curl \
  -- curl -v http://api-gateway.dev.svc.cluster.local/api/v1/repos?query=test
```

### Authorization

```bash
kubectl apply -f authorization/rbacconfig.yaml

kubectl create sa api-gateway -n dev
kubectl patch -n dev deploy api-gateway --patch '{"spec": {"template": {"spec": {"serviceAccountName": "api-gateway"}}}}'
kubectl create sa github-service -n dev
kubectl patch -n dev deploy github-service --patch '{"spec": {"template": {"spec": {"serviceAccountName": "github-service"}}}}'

kubectl apply -f authorization/servicerole.yaml -f authorization/servicerolebinding.yaml

# Test
kubectl run hacker -n dev --restart=Never --generator run-pod/v1 --image tutum/curl -- sleep 3600
kubectl exec hacker -n dev -c hacker -- curl -s -i api-gateway.dev.svc.cluster.local/api/v1/repos?query=hacking

API_GW=$(kubectl get pod -n dev -l app=api-gateway -o jsonpath='{.items[0].metadata.name}')
kubectl exec -it -n dev ${API_GW} -c api-gateway -- curl -i github-service/github/admin
kubectl exec -it -n dev ${API_GW} -c api-gateway -- curl -X POST -i github-service/github/repos?query=hacking
```
