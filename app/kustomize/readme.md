# Kustomize

## Environment: dev

deploy to Docker Desktop(enable kuberentes)

```bash
WS=$(pwd)
# backend
cd $WS/../stateless/github-service && docker build -t github-service:dev.1 .
# api-gateway
cd $WS/../stateless/api-gateway && docker build -t api-gateway:dev.1 .
# ui(dev mode)
cd $WS/../stateless/repo-search-ui && docker build -t repo-search-ui:dev.1 -f Dockerfile.multi-env --build-arg TARGET=dev .
# github mock(wiremock)
cd $WS/mock && docker build -t github-stub:latest .

cd $WS

# switch k8s context to docker desktop
kubectl config use-context docker-for-desktop --namespace default

# install redis,nginx controller by helm
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/helm/tiller-rbac.yaml
helm init --service-account tiller
#helm init
helm upgrade nginx-ingress --install stable/nginx-ingress
helm upgrade redis --install stable/redis --namespace redis \
  --set master.persistence.enabled=false \
  --set cluster.enabled=false \
  --set password=frieza-redis-pass

# create secret files
mkdir overlays/dev/.env
echo -n "<your-github-userid>" > overlays/dev/.env/github-user
echo -n "<your-github-password>" > overlays/dev/.env/github-pass
echo -n "frieza-redis-pass" > overlays/dev/.env/redis-pass

# apply kustomize resources
kubectl apply -k overlays/dev

# add static DNS entry
echo "127.0.0.1 dev.github.frieza.local" | sudo tee -a /etc/hosts

# delete kustomize resource
kubectl delete -k overlays/dev
docker rmi github-service:latest -f
docker rmi api-gateway:latest -f
docker rmi repo-search-ui:latest -f
```

## Environment: prod

deploy to Azure AKS.

```bash

```
