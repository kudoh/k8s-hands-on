# Monitoring

## Install Prometheus Operator

```bash
helm repo update
kubectl create ns prom
helm upgrade prometheus-operator --install stable/prometheus-operator \
  --version 6.4.3 --namespace prom \
  --set prometheus.service.type=LoadBalancer
```

## Web UI

```bash
kubectl get svc -n prom -l app=prometheus-operator-prometheus -o custom-columns=IP:status.loadBalancer.ingress[0].ip,PORT:spec.ports[0].nodePort
```

## run web app

```bash
GITHUB_USER=<your-github-userid>
GITHUB_PASS=<your-github-password>

../../app/stateless/deploy.sh $GITHUB_USER $GITHUB_PASS

# undeploy
../../app/stateless/undeploy.sh
```

## ServiceMonitor

```bash
kubectl apply -f servicemonitor.yaml
```

/etc/prometheus/config_out/prometheus.env.yaml

## PushGateway

```bash
cat << EOF > push-gateway-values.yaml
serviceAccount:
  create: false
  name: prometheus-operator-prometheus
serviceMonitor:
  enabled: true
  namespace: prom
  selector:
    release: prometheus-operator
EOF

# 現時点の最新版1.0.0, 1.0.1は動作しない
# https://github.com/prometheus/pushgateway/issues/278
helm upgrade prometheus-pushgateway --install stable/prometheus-pushgateway \
  --namespace prom --version 0.4.1 -f push-gateway-values.yaml
```

## Run Batch app

```bash
../../app/batch/deploy.sh $GITHUB_USER $GITHUB_PASS
# enable prometheus
kubectl patch cj cron-batch-app -p "$(cat prom-cronjob_patch.yaml)"

# undeploy
../../app/batch/undeploy.sh
```

## Alerting

```bash
# apply alerting rule
kubectl apply -f app-alert-rule.yaml

# specify slack receiver
helm upgrade prometheus-operator --install stable/prometheus-operator \
  --version 6.4.3 --namespace prom \
  --set prometheus.service.type=LoadBalancer \
  -f slack-alert-config.yaml --reset-values
```

## Grafana

```bash
helm upgrade prometheus-operator stable/prometheus-operator --set grafana.service.type=LoadBalancer --reuse-values
```
