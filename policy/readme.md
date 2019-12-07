# OPA

## setup Gatekeeper

```sh
# before you apply, make sure you have cluster-admin role

kubectl apply -f https://raw.githubusercontent.com/open-policy-agent/gatekeeper/master/deploy/gatekeeper.yaml
```

## setup clusterrolebindings

```sh
# for fireza
kubectl create clusterrolebinding mamezou-sre --clusterrole=cluster-admin --group mamezou-sre
# for dodoria, zarbon
kubectl create clusterrolebinding mamezou-dev --clusterrole=edit --group mamezou-dev
```

## create OPA policies

```sh
kubectl apply -f labelconstraint/label-constraint-template.yaml
kubectl apply -f labelconstraint/required-label-pod-constraint.yaml

kubectl apply -f containerconstraint/container-constraint-template.yaml
kubectl apply -f containerconstraint/container-constraint.yaml
```
