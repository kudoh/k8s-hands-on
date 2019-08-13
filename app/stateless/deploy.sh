#!/bin/bash

if [[ $# < 2 ]]; then
  echo "you must specify github-userid github-password as arguments"
  exit 1
fi

# deploy github-service
kubectl create secret generic github-secret --from-literal user=${1} --from-literal password=${2}
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/github-service/service.yaml \
              -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/github-service/deployment.yaml

# deploy Redis
kubectl create ns redis
helm upgrade redis --install stable/redis --namespace redis \
  --set master.persistence.enabled=false \
  --set cluster.enabled=false \
  --set password=frieza-redis-pass

# deploy api-gateway
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/secret.yaml \
              -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/configmap.yaml \
              -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/deployment.yaml \
              -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/service.yaml

# deploy repo-search-ui
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/repo-search-ui/configmap.yaml \
              -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/repo-search-ui/deployment.yaml \
              -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/repo-search-ui/service.yaml

# deploy ingress
openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout tls.key -out tls.crt -subj "/CN=frieza.local/O=Mamezou"
kubectl create secret tls tls-secret --key tls.key --cert tls.crt

kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/ingress/ingress.yaml

PUBLIC_IP=$(kubectl get svc nginx-ingress-controller -o jsonpath={.status.loadBalancer.ingress[0].ip})

echo "${PUBLIC_IP} github.frieza.local" | sudo tee -a /etc/hosts

echo "go to https://github.frieza.local/"
