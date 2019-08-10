# Scheduling

## Taints/Tolerations

```bash
kubectl taint node k8s-worker3 gpu=:NoSchedule

kubectl apply -f tolerations.yaml

kubectl get pod -o custom-columns=Pod:metadata.name,Node:spec.nodeName,HostIP:status.hostIP,Status:status.phase -l 'name in (heavy,normal)'
```

## NodeAffinity

## PodAffinity

## PodAntiAffinity
