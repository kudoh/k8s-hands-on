

```bash
helm upgrade redis --install stable/redis --namespace redis \
  --set cluster.enabled=true \
  --set sentinel.enabled=true \
  --set password=frieza-redis-pass \
  --set master.persistence.storageClass=openebs-sparse-sc \
  --set slave.persistence.storageClass=openebs-sparse-sc

kubectl run -it srvlookup --image=tutum/dnsutils --rm --restart=Never -- dig SRV redis-headless.redis.svc.cluster.local

```