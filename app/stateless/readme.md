## github-service

```bash
GITHUB_USER=$(echo -n "github-userid" | base64)
GITHUB_PASSWORD=$(echo -n "github-password" | base64)
cat k8s/github-service/secret.yaml | \
   sed -e "s/user: ''/user: $GITHUB_USER/g" | \
   sed -e "s/password: ''/password: $GITHUB_PASSWORD/g" | \
   kubectl apply -f-
   
kubectl apply -f k8s/github-service/github-api.yaml
kubectl apply -f k8s/github-service/service.yaml
kubectl apply -f k8s/github-service/deployment.yaml
```

## Redis

```bash
helm upgrade redis --install stable/redis --namespace redis \
  --set master.persistence.enabled=false \
  --set cluster.enabled=false \
  --set password=frieza-redis-pass
```

## api-gateway

```bash
kubectl apply -f k8s/api-gateway/secret.yaml
kubectl apply -f k8s/api-gateway/configmap.yaml

kubectl apply -f k8s/api-gateway/deployment.yaml

kubectl apply -f k8s/api-gateway/service.yaml
```

## repo-search-ui

```bash
kubectl apply -f k8s/repo-search-ui/configmap.yaml
kubectl apply -f k8s/repo-search-ui/deployment.yaml
kubectl apply -f k8s/repo-search-ui/service.yaml

```

# Ingress

```bash
openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout tls.key -out tls.crt -subj "/CN=frieza.local/O=Mamezou"
kubectl create secret tls tls-secret --key tls.key --cert tls.crt

kubectl apply -f k8s/ingress/ingress.yaml

echo "172.16.20.11 github.frieza.local" | sudo tee -a /etc/hosts
```