#!/bin/bash

# NFS Provisioner
kubectl delete -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/nfs/pvc.yaml
helm delete --purge nfs-server-provisioner
kubectl delete ns nfs

# Postgres
kubectl delete secret github-db-secret
kubectl delete -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/postgres/service.yaml \
               -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/postgres/statefulset.yaml

# Batch App(CronJob)
kubectl delete secret github-api-secret
kubectl delete -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/batch-app/cronjob.yaml

echo "undeployed!!"
