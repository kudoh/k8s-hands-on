#!/bin/bash

# Batch App(CronJob)
kubectl delete -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/batch-app/cronjob.yaml
kubectl delete secret github-api-secret

# NFS Provisioner
helm delete --purge nfs-server-provisioner
kubectl delete -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/nfs/pvc.yaml
kubectl delete ns nfs

# Postgres
kubectl delete secret github-db-secret
kubectl delete -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/postgres/service.yaml \
               -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/batch/k8s/postgres/statefulset.yaml
echo "undeployed!!"
