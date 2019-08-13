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

## run app

```bash
GITHUB_USER=<your-github-userid>
GITHUB_PASS=<your-github-password>
bash <(curl -s https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/app/stateless/deploy.sh) $GITHUB_USER $GITHUB_PASS

../../app/stateless/deploy.sh $GITHUB_USER $GITHUB_PASS
../../app/batch/deploy.sh $GITHUB_USER $GITHUB_PASS

# undeploy
../../app/stateless/undeploy.sh
../../app/batch/undeploy.sh
```

## ServiceMonitor

```bash
kubectl apply -f servicemonitor.yaml
```

/etc/prometheus/config_out/prometheus.env.yaml
