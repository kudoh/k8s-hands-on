# stateful app

## redis

```bash
kubectl create namespace redis
kubectl create secret generic redis-secret --from-literal redis-secret=frieza-redis-pass -n redis
kubectl apply -f k8s/redis/headless-service.yaml
kubectl apply -f k8s/redis/configmap.yaml
kubectl apply -f k8s/redis/master-statefulset.yaml
kubectl apply -f k8s/redis/slave-statefulset.yaml
kubectl apply -f k8s/redis/service-master.yaml
kubectl apply -f k8s/redis/service-slave.yaml
```

## api-gateway

```bash
docker build -t kudohn/api-gateway:v2 ./api-gateway && docker push kudohn/api-gateway:v2
```

## application

```bash
# github-service
GITHUB_USER=$(echo -n "<your-github-userid>" | base64)
GITHUB_PASSWORD=$(echo -n "<your-github-password>" | base64)
cat ../stateless/k8s/github-service/secret.yaml | \
    sed -e "s/user: ''/user: $GITHUB_USER/g" | \
    sed -e "s/password: ''/password: $GITHUB_PASSWORD/g" | \
    kubectl apply -f-
kubectl apply -f ../stateless/k8s/github-service/deployment.yaml
kubectl apply -f ../stateless/k8s/github-service/service.yaml
# api-gateway. ここだけ変更
kubectl apply -f k8s/api-gateway/configmap.yaml
kubectl apply -f k8s/api-gateway/secret.yaml
kubectl apply -f k8s/api-gateway/deployment.yaml
kubectl apply -f k8s/api-gateway/service.yaml
# repo-search-ui
kubectl apply -f ../stateless/k8s/repo-search-ui/configmap.yaml
kubectl apply -f ../stateless/k8s/repo-search-ui/deployment.yaml
kubectl apply -f ../stateless/k8s/repo-search-ui/service.yaml
# Ingress
openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout tls.key -out tls.crt -subj "/CN=frieza.local/O=Mamezou"
kubectl create secret tls tls-secret --key tls.key --cert tls.crt
```