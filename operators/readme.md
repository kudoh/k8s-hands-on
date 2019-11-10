# Kubernetes Operators

```sh
export GOPATH=$(pwd)
mkdir $GOPATH/src
cd $GOPATH/src
operator-sdk new github-search-operator
cd github-search-operator
go mod tidy

operator-sdk add api --api-version=mamezou.com/v1 --kind=GithubSearch
https://github.com/operator-framework/operator-sdk/blob/master/doc/operator-scope.md

operator-sdk generate k8s
operator-sdk generate openapi
operator-sdk add controller --api-version=mamezou.com/v1 --kind=GithubSearch

operator-sdk build kudohn/github-search-operator:v1.0.0
docker push kudohn/github-search-operator:v1.0.0
sed -i "" 's|REPLACE_IMAGE|kudohn/github-search-operator:v1.0.0|g' deploy/operator.yaml

kubectl create ns dev
kubectl create ns operator

GIHUB_USER=<your-github-user>
GITHUB_PASSWORD=<your-github-password>
kubectl create -n dev secret generic github-secret \
  --from-literal user=${GITHUB_USER} --from-literal password=${GITHUB_PASSWORD}


# ClusterScoped
kubectl apply \
  -f deploy/role.yaml \
  -f deploy/role_binding.yaml \
  -f deploy/crds/mamezou.com_githubsearches_crd.yaml
# NamespaceScoped
kubectl apply -n operator \
  -f deploy/service_account.yaml \
  -f deploy/operator.yaml

kubectl apply -f githubsearch_v1.yaml
kubectl apply -f githubsearch_v2.yaml
```
