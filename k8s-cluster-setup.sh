#!/bin/bash

MASTER_IP=172.16.10.11
 
 # kubeconfigのセットアップ。APIサーバのdefaultは6443ポートが開いている。マルチMasterの場合はLBを指定
kubectl config set-cluster local-k8s --server=https://${MASTER_IP}:6443 --certificate-authority=./local-cluster/shared/k8s-ca.crt
kubectl config set-credentials tester --token=$(cat ./local-cluster/shared/token)
kubectl config set-context local-k8s-tester --cluster=local-k8s --user=tester --namespace default
  
kubectl config use-context local-k8s-tester

which helm
rc=$?
if [[ $rc != 0 ]] ;then
  curl -L https://git.io/get_helm.sh | bash
fi

kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/helm/tiller-rbac.yaml
helm init --service-account tiller
rc=$?
if [[ $rc != 0 ]] ;then
  echo "unable to install helm..."
  exit 1
fi

sleep 30

# MetalLB
# kubectl create namespace metallb-system
# helm install --name metallb stable/metallb --namespace metallb-system
# kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/ingress/metallb-configmap.yaml
# rc=$?
# if [[ $rc != 0 ]] ;then
#   echo "unable to install MetalLB..."
#   exit 1
# fi

# sleep 30

# # Nginx Ingress Controller
# helm upgrade nginx-ingress --install stable/nginx-ingress --set controller.replicaCount=2
# rc=$?
# if [[ $rc != 0 ]] ;then
#   echo "unable to install Ingress Controller..."
#   exit 1
# fi

# OpenEBS
kubectl create ns openebs
helm upgrade openebs --install stable/openebs --namespace openebs
rc=$?
if [[ $rc != 0 ]] ;then
  echo "unable to install OpenEBS..."
  exit 1
fi

sleep 30

kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/storage/cstor-pool-config.yaml
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/storage/storageclass.yaml

echo "Done!!"