# Ingress

## MetalLB(Network Load Balancer)

```bash
kubectl create namespace metallb-system
helm install --name metallb stable/metallb --namespace metallb-system --set existingConfigMap=config
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/ingress/metallb-configmap.yaml
kubectl logs -l component=speaker -n metallb-system
```

## Nginx Ingress Controller

### Install Nginx Ingress Controller with 2 replicas
```bash
helm upgrade nginx-ingress --install  stable/nginx-ingress --set controller.replicaCount=2

```

### Deploy sample applications
```bash
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/ingress/nginx/deployment-app1.yaml
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/ingress/nginx/deployment-app2.yaml
```

### Create Ingress Resource
```
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/ingress/nginx/ingress.yaml
```