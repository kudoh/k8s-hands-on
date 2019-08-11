# Scheduling

## Taints/Tolerations

```bash
kubectl taint node k8s-worker3 gpu=:NoSchedule

kubectl apply -f tolerations.yaml

kubectl get pod -o custom-columns=Pod:metadata.name,Node:spec.nodeName,HostIP:status.hostIP,Status:status.phase -l 'name in (heavy,normal)'
```

## NodeAffinity

```bash
kubectl label node k8s-worker3 gpu=true

kubectl get node -L gpu

kubectl delete deploy heavy normal
kubectl apply -f nodeaffinity.yaml

kubectl get pod -o custom-columns=Pod:metadata.name,Node:spec.nodeName,HostIP:status.hostIP,Status:status.phase -l 'name in (heavy,normal)'
```

## PodAffinity

```bash
# 前につけたtaint/labelは削除しておく
 kubectl taint node k8s-worker3 gpu-
 kubectl label node k8s-worker3 gpu-
 # dara-center Labelを付与
 kubectl label node k8s-worker1 data-center=tokyo
 kubectl label node k8s-worker2 data-center=tokyo
 kubectl label node k8s-worker3 data-center=osaka

kubectl apply -f podaffinity.yaml

kubectl get pod -o custom-columns=Pod:metadata.name,Node:spec.nodeName,HostIP:status.hostIP,Status:status.phase -l 'name in (frieza,dodoria,zarbon)'
```

## PodAntiAffinity

```bash
kubectl apply -f podantiaffinity.yaml

kubectl get pod -o custom-columns=Pod:metadata.name,Node:spec.nodeName,HostIP:status.hostIP,Status:status.phase -l 'name in (frieza,goku)'
```
