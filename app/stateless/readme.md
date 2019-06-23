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

## api-gateway

```bash
kubectl apply -f k8s/api-gateway/secret.yaml
kubectl apply -f k8s/api-gateway/configmap.yaml

kubectl apply -f k8s/api-gateway/deployment.yaml

kubectl apply -f k8s/api-gateway/service.yaml
```

## Redis

```bash
helm upgrade redis --install stable/redis --namespace redis \
  --set master.persistence.enabled=false \
  --set cluster.enabled=false \
  --set password=frieza-redis-pass

REDIS_PASSWORD=$(kubectl get secret --namespace redis redis -o jsonpath="{.data.redis-password}" | base64 --decode)

```

# Ingress

```bash
kubectl apply -f k8s/ingress/ingress.yaml

echo "172.16.20.11 github.frieza.local" | sudo tee -a /etc/hosts
```