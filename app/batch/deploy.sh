#!/bin/bash

if [[ $# < 2 ]]; then
  echo "you must specify github-userid github-password as arguments"
  exit 1
fi

# NFS Provisioner
kubectl create ns nfs
helm upgrade nfs-server-provisioner --install stable/nfs-server-provisioner --namespace nfs \
   --set persistence.enabled=true --set persistence.storageClass=openebs-sparse-sc \
   --set persistence.size=10Gi --set service.type=NodePort \
   --set service.nfsNodePort=32000 --set service.mountdNodePort=30050
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/nfs/pvc.yaml

VOL_NAME=$(kubectl get pvc nfs-pvc -o jsonpath='{.spec.volumeName}')

#sudo mkdir -p /private/nfs
#sudo mount -t nfs -o port=32000,mountport=30050 172.16.20.11:/export/$VOL_NAME /private/nfs

# Postgres
kubectl create secret generic github-db-secret --from-literal username=frieza --from-literal password=k8s-frieza-pass
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/postgres/service.yaml \
              -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/postgres/statefulset.yaml

# Batch App(CronJob)
kubectl create secret generic github-api-secret --from-literal github-api-user=${1} --from-literal=github-api-password=${2}
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/batch-app/cronjob.yaml

echo "finish!!"
