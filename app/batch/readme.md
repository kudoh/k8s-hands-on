# batch app

## NFS Server

```bash
kubectl create ns nfs
helm upgrade nfs-server-provisioner --install stable/nfs-server-provisioner --namespace nfs \
   --set persistence.enabled=true --set persistence.storageClass=openebs-sparse-sc \
   --set persistence.size=10Gi --set service.type=NodePort \
   --set service.nfsNodePort=32000 --set service.mountdNodePort=30050
kubectl apply -f k8s/nfs/pvc.yaml

VOL_NAME=$(kubectl get pvc nfs-pvc -o jsonpath='{.spec.volumeName}')

sudo mkdir -p /private/nfs
sudo mount -t nfs -o port=32000,mountport=30050 172.16.20.11:/export/$VOL_NAME /private/nfs
#sudo umount /private/nfs

# バッチの入力ファイル設定
cp ./github-events.csv /private/nfs
```

## Postgres

```bash
kubectl create secret generic github-db-secret --from-literal username=frieza --from-literal password=k8s-frieza-pass
kubectl apply -f k8s/postgres/service.yaml
kubectl apply -f k8s/postgres/statefulset.yaml
```

## application(Srping Batch)

```bash
docker build -t kudohn/batch-app:v1 ./batch-app && docker push kudohn/batch-app:v1
```

## Job

```bash
INPUT_FILE_NAME=github-events.csv
cat k8s/batch-app/job.yaml | \
    sed -e "s/file.name=''/file.name=$INPUT_FILE_NAME/g" | \
    kubectl apply -f-

kubectl logs job/batch-app

DB_POD=$(kubectl get pod -l app=github-db -o jsonpath='{.items[0].metadata.name}')
kubectl exec -it $DB_POD bash
su - postgres
psql -d frieza -U frieza -c "select count(*) from github_events"

psql -d frieza -U frieza -c "select * from github_events limit 10"
exit

kubectl delete job batch-app
```

## CronJob

```bash
docker build -t kudohn/cron-batch-app:v1 -f ./batch-app/Dockerfile.cron ./batch-app && docker push kudohn/cron-batch-app:v1

kubectl create secret generic github-api-secret --from-literal github-api-user=<your-user-id> --from-literal=github-api-password=<your-password>
kubectl apply -f k8s/batch-app/cronjob.yaml
```
