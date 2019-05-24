# Helm(Package Manager)

## install helm client tool
```bash
# for mac
brew install kubernetes-helm
helm version
```

## using Tiller as server side component
### Set up
```bash
# RBAC Settings
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/helm/tiller-rbac.yaml

# Install Tiller
helm init --service-account tiller
kubectl get pod -n kube-system -l app=helm -o wide
```

### install kubernnates-dashboard chart
```bash
helm upgrade --install kubernetes-dashboard stable/kubernetes-dashboard --namespace kube-system \
  --set rbac.clusterAdminRole=true --wait
```

## using Tiller as client side commonent

### Set up
```bash
# Initialize Helm Client
helm init --client-only
```

### install jenkins chart
```bash
kubectl create namespace ops
helm fetch stable/jenkins --untar

helm template jenkins/ --name jenkins --namespace ops \
  --set persistence.enabled=false --set master.ingress.enabled=false \
  --set master.adminPassword=admin-pass --set master.nodePort=30080 \
  --set master.serviceType=NodePort \
  | kubectl apply -n ops -f -
```