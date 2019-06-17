## github-service

```bash
# Secret
GITHUB_USER=$(echo -n "github-userid" | base64)
GITHUB_PASSWORD=$(echo -n "github-password" | base64)
cat k8s/github-service/secret.yaml | \
   sed -e "s/user: ''/user: $GITHUB_USER/g" | \
   sed -e "s/password: ''/password: $GITHUB_PASSWORD/g" | \
   kubectl apply -f-

# Service
kubectl apply -f k8s/github-service-service.yaml

# Deployment
kubectl apply -f k8s/github-service-deployment.yaml
```