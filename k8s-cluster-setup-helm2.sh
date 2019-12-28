#!/bin/bash

MASTER_IP=172.16.10.11
SHARED_DIR=${1:-./local-cluster/shared}
 
 # kubeconfigのセットアップ。APIサーバのdefaultは6443ポートが開いている。マルチMasterの場合はLBを指定
# kubectl config set-cluster local-k8s --server=https://${MASTER_IP}:6443 --certificate-authority=${SHARED_DIR}/k8s-ca.crt
# kubectl config set-credentials tester --token=$(cat ${SHARED_DIR}/token)
# kubectl config set-context local-k8s-tester --cluster=local-k8s --user=tester --namespace default

kubectl config set-cluster local-k8s --server=https://${MASTER_IP}:6443 \
  --certificate-authority=${SHARED_DIR}/ca.crt --embed-certs
kubectl config set-credentials admin \
  --client-certificate=${SHARED_DIR}/k8s-admin.crt \
  --client-key=${SHARED_DIR}/k8s-admin.key \
  --embed-certs
kubectl config set-context local-k8s-tester \
  --cluster=local-k8s --namespace=default --user=admin

kubectl config use-context local-k8s-tester

which helm
rc=$?
if [[ $rc != 0 ]] ;then
  curl -L https://git.io/get_helm.sh | bash
fi

kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/helm/tiller-rbac.yaml
#helm init --upgrade --service-account tiller
# for k8s greater than v1.16
helm init --service-account tiller --output yaml | sed 's@apiVersion: extensions/v1beta1@apiVersion: apps/v1@' | sed 's@  replicas: 1@  replicas: 1\n  selector: {"matchLabels": {"app": "helm", "name": "tiller"}}@' | kubectl apply -f -
rc=$?
if [[ $rc != 0 ]] ;then
  echo "unable to install helm..."
  exit 1
fi

sleep 60

# MetalLB
kubectl create namespace metallb-system
helm install --name metallb stable/metallb --namespace metallb-system
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/ingress/metallb-configmap.yaml
rc=$?
if [[ $rc != 0 ]] ;then
  echo "unable to install MetalLB..."
  exit 1
fi

sleep 30

# Nginx Ingress Controller
helm upgrade nginx-ingress --install stable/nginx-ingress --set controller.replicaCount=2
rc=$?
if [[ $rc != 0 ]] ;then
  echo "unable to install Ingress Controller..."
  exit 1
fi

# OpenEBS
for n in $(kubectl get node -l 'node-role.kubernetes.io/master!=' -o jsonpath='{.items[*].metadata.name}' | grep -i worker); do 
  kubectl label nodes $n node=openebs
done
kubectl create ns openebs
helm upgrade openebs --install stable/openebs --namespace openebs --version 1.5.0 \
  --set apiserver.sparse.enabled=true \
  --set ndm.sparse.path="/var/openebs/sparse" \
  --set ndm.sparse.count=1 \
  --set ndm.sparse.size=32212254720 \
  --wait

rc=$?
if [[ $rc != 0 ]] ;then
  echo "unable to install OpenEBS..."
  exit 1
fi

# kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/storage/cstor-pool-config.yaml
# kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/storage/storageclass.yaml

echo "Done!!"