# Auto Scaling

## Metrics Server

```bash
cat << EOF > values.yaml
args:
- --kubelet-insecure-tls
- --kubelet-preferred-address-types=InternalDNS,InternalIP,ExternalDNS,ExternalIP,Hostname
EOF

helm upgrade metrics-server --install stable/metrics-server -f values.yaml
```

## Sample mock app

```bash
kubectl apply -f mock.yaml

PUBLIC_IP=$(kubectl get svc nginx-ingress-controller -o jsonpath='{.status.loadBalancer.ingress[*].ip}')
curl -i -H 'Host: mock.cluster.local' $PUBLIC_IP/mock/status/200
```

## HPA

```bash
kubectl apply -f cpu-hpa.yaml
kubectl get hpa,deploy -l name=mock
```

## Test

```bash
hey -n 100 -c 10 http://mock.cluster.local/mock/delay/0.5
```
