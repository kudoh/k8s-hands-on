#!/bin/bash

# undeploy github-service
kubectl delete secret github-secret
kubectl delete -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/github-service/service.yaml \
               -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/github-service/deployment.yaml

# undeploy Redis
helm delete --purge redis
kubectl delete ns redis

# undeploy api-gateway
kubectl delete -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/secret.yaml \
               -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/configmap.yaml \
               -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/deployment.yaml \
               -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/api-gateway/service.yaml

# undeploy repo-search-ui
kubectl delete -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/repo-search-ui/configmap.yaml \
               -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/repo-search-ui/deployment.yaml \
               -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/repo-search-ui/service.yaml

# undeploy ingress
kubectl delete secret tls-secret
rm -rf tls.*

kubectl delete -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/k8s/ingress/ingress.yaml

sudo sed -i.bak '/github.frieza.local/d' /etc/hosts

echo "undeployed!"
