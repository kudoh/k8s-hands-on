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

## IngressGateway/EgressGateway

```bash
# SSL Certificate
openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout tls.key -out tls.crt -subj "/CN=frieza.local/O=Mamezou"
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

```bash
kubectl apply -n dev -f timeout/

INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
curl -v -H 'Host:timeout.frieza.local' $INGRESS_HOST/sleep/1s ;echo
```

## Retry

```bash
kubectl apply -n dev -f retry/

INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
curl -v -H 'Host:retry.frieza.local' $INGRESS_HOST/retry ;echo
```
