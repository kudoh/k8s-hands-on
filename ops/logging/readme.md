# logging

## Elasticsearch

```bash
kubectl create ns logging

cat << EOF > es-values.yaml
replicas: 2
esJavaOpts: "-Xmx768m -Xms768m"
resources:
  requests:
    cpu: 100m
    memory: 1024Mi
  limits:
    cpu: 1000m
    memory: 1536Mi
volumeClaimTemplate:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
  storageClassName: openebs-hostpath
EOF

helm repo add elastic https://helm.elastic.co
helm upgrade elasticsearch --install elastic/elasticsearch \
  --namespace logging -f es-values.yaml

kubectl get sts,pod,svc -n logging

kubectl run test -it --rm --generator run-pod/v1 --image tutum/curl bash
# https://www.elastic.co/guide/en/elasticsearch/reference/current/docs.html
curl -X PUT http://elasticsearch-master:9200/test/_doc/1?pretty \
     -H 'Content-Type:application/json' -d '{"name": "frieza"}'
curl -X GET http://elasticsearch-master:9200/test/_doc/1?pretty
curl -X DELETE http://elasticsearch-master:9200/test/_doc/1?pretty
```

## Fluentd

```bash
helm repo add kiwigrid https://kiwigrid.github.io
helm upgrade fluentd-elasticsearch --install kiwigrid/fluentd-elasticsearch \
  --namespace logging --version 4.8.4 \
  --set elasticsearch.host=elasticsearch-master.logging.svc.cluster.local \
  --set elasticsearch.port=9200 \
  -f fluentd-output.yaml

#  --set hostLogDir.dockerContainers=/var/lib/containerd/io.containerd.content.v1.content/blobs/sha256

kubectl get ds,pod -l app.kubernetes.io/instance=fluentd-elasticsearch -n logging
```

## Kibana

```bash
cat << EOF > kibana-values.yaml
elasticsearchHosts: http://elasticsearch-master:9200
resources:
  requests:
    cpu: 100m
    memory: 256Mi
  limits:
    cpu: 1000m
    memory: 1Gi
service:
  type: LoadBalancer
EOF

helm upgrade kibana --install elastic/kibana \
  --namespace logging -f kibana-values.yaml

kubectl get deploy,pod,svc -l app=kibana -n logging
```

## Application

```bash
GITHUB_USER=<your-github-userid>
GITHUB_PASS=<your-github-password>
bash <(curl -s https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/deploy.sh) $GITHUB_USER $GITHUB_PASS


kubectl apply -f logback-configmap.yaml
kubectl patch deploy github-service --patch "$(cat github-service_patch.yaml)"
```
