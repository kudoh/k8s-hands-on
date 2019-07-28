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
docker rmi github-service:dev.1 -f
docker rmi api-gateway:dev.1 -f
docker rmi repo-search-ui:dev.1 -f
```

## Environment: prod

deploy to Azure AKS.

```bash
az login

RG=k8sResourceGroup
ACR=k8sHandsOn
REDIS=friezaRedis
AKS=aks-cluster

# Azure Resoruce Group
az group create --name k8sResourceGroup --location japaneast


# ACR
az acr create --resource-group ${RG} --name ${ACR} --sku Basic
az acr login --name ${ACR}

# github-service
docker tag github-service:dev.1 k8shandson.azurecr.io/github-service:prod.1
docker push k8shandson.azurecr.io/github-service:prod.1

# api-gateway
docker tag api-gateway:dev.1 k8shandson.azurecr.io/api-gateway:prod.1
docker push k8shandson.azurecr.io/api-gateway:prod.1

# ui-repo-search
cd $WS/../stateless/repo-search-ui && docker build -t repo-search-ui:prod.1 -f Dockerfile.multi-env --build-arg TARGET=prod .
docker tag repo-search-ui:prod.1 k8shandson.azurecr.io/repo-search-ui:prod.1
docker push k8shandson.azurecr.io/repo-search-ui:prod.1

az acr repository list --name ${ACR} --output table

# AKS
az aks create \
     --resource-group ${RG} \
     --name ${AKS} \
     --node-count 3 \
     --enable-addons monitoring \
     --generate-ssh-keys
az aks get-credentials --resource-group ${RG} --name ${AKS}

CLIENT_ID=$(az aks show --resource-group $RG --name $AKS --query "servicePrincipalProfile.clientId" --output tsv)
ACR_ID=$(az acr show --name $ACR --resource-group $RG --query "id" --output tsv)
az role assignment create --assignee $CLIENT_ID --role acrpull --scope $ACR_ID

# Redis
az redis create --location japaneast \
                --name ${REDIS} \
                --resource-group ${RG} \
                --sku basic \
                --vm-size c0 \
                --enable-non-ssl-port
az redis list --resource-group ${RG}
az redis list-keys --resource-group ${RG} --name ${REDIS}

#az redis delete --name ${REDIS} --resource-group ${RG} -y

# GlobalIP 52.246.183.66
NODE_RG=$(az aks list --resource-group $RG --query '[0].nodeResourceGroup' -o tsv)
az network public-ip create --name frieza-ip --resource-group $NODE_RG --allocation-method Static --sku Basic

# Helm/Ingress Controller
# install nginx controller by helm
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/helm/tiller-rbac.yaml
helm init --service-account tiller
helm upgrade nginx-ingress --install stable/nginx-ingress \
   --set controller.service.loadBalancerIP="52.246.183.66" \
   --set nodeSelector."beta.kubernetes.io/os"=linux
kubectl get svc -l app=nginx-ingress

# cert-manager
kubectl apply -f https://github.com/jetstack/cert-manager/releases/download/v0.8.0/cert-manager.yaml --validate=false

# credential
echo -n "<your-github-userid>" > overlays/prod/.env/github-user
echo -n "<your-github-password>" > overlays/prod/.env/github-pass
REDIS_PASS=$(az redis list-keys --resource-group ${RG} --name ${REDIS} --query 'primaryKey' -o tsv)
echo -n $REDIS_PASS > overlays/prod/.env/redis-pass

az group delete --name ${RG}
```
