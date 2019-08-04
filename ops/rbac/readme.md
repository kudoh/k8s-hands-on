# RBAC

## Create CSR(Local)

```bash
git clone https://github.com/kudoh/k8s-hands-on.git
cd k8s-hands-on/ops/rbac
WS=$(pwd)
mkdir -p $WS/.env
# CSR作成
# user=dodoria, group=mamezou-dev
openssl req -new -newkey rsa:2048 -nodes \
  -keyout $WS/.env/dodoria.key \
  -out $WS/.env/dodoria.csr \
  -subj "/CN=dodoria/O=mamezou-dev"
# user=zarbon, group=mamezou-dev
openssl req -new -newkey rsa:2048 -nodes \
  -keyout $WS/.env/zarbon.key \
  -out $WS/.env/zarbon.csr \
  -subj "/CN=zarbon/O=mamezou-dev"
# user=frieza, group=mamezou-sre
openssl req -new -newkey rsa:2048 -nodes \
  -keyout $WS/.env/frieza.key \
  -out $WS/.env/frieza.csr \
  -subj "/CN=frieza/O=mamezou-sre"

# 秘密鍵のPermissionは最小限にする  
chmod 600 $WS/.env/dodoria.key
chmod 600 $WS/.env/zarbon.key
chmod 600 $WS/.env/frieza.key
```

## Sign CSR

```bash
# CSRをLocalクラスタのVagrantの共有ディレクトリにコピー
cp $WS/.env/dodoria.csr $WS/../../local-cluster/shared
cp $WS/.env/zarbon.csr $WS/../../local-cluster/shared
cp $WS/.env/frieza.csr $WS/../../local-cluster/shared

# Login to k8s-maseter1(vagrant-virtualbox)
cd $WS/../../local-cluster
vagrant ssh k8s-master1

# 以下はMasterノード上での作業
su -
SHARED=/home/vagrant/shared
# kubeadmの場合はデフォルトでここに各種証明書が格納される
MASTER_PKI=/etc/kubernetes/pki

# CSRを署名
# dodoria
openssl x509 -req -days 60 \
  -in $SHARED/dodoria.csr \
  -CA $MASTER_PKI/ca.crt \
  -CAkey $MASTER_PKI/ca.key \
  -CAcreateserial \
  -out $SHARED/dodoria.crt -sha256
# zarbon
openssl x509 -req -days 60 \
  -in $SHARED/zarbon.csr \
  -CA $MASTER_PKI/ca.crt \
  -CAkey $MASTER_PKI/ca.key \
  -CAcreateserial \
  -out $SHARED/zarbon.crt -sha256
# frieza
openssl x509 -req -days 60 \
  -in $SHARED/frieza.csr \
  -CA $MASTER_PKI/ca.crt \
  -CAkey $MASTER_PKI/ca.key \
  -CAcreateserial \
  -out $SHARED/frieza.crt -sha256

logout # root
logout # vagrant

# Localでの作業
# 署名済みのクライアント証明書を作業ディレクトリにコピー
cp -p shared/dodoria.crt $WS/.env
cp -p shared/zarbon.crt $WS/.env
cp -p shared/frieza.crt $WS/.env
cd $WS

ls -ltr $WS/.env
-rw-------  1 my-name  staff  1704  8  4 15:00 dodoria.key
-rw-r--r--  1 my-name  staff   920  8  4 15:00 dodoria.csr
-rw-------  1 my-name  staff  1704  8  4 15:00 zarbon.key
-rw-r--r--  1 my-name  staff   915  8  4 15:00 zarbon.csr
-rw-------  1 my-name  staff  1708  8  4 15:00 frieza.key
-rw-r--r--  1 my-name  staff   915  8  4 15:00 frieza.csr
-rw-r--r--  1 my-name  staff  1005  8  4 15:06 dodoria.crt
-rw-r--r--  1 my-name  staff  1005  8  4 15:06 zarbon.crt
-rw-r--r--  1 my-name  staff  1005  8  4 15:06 frieza.crt
```

## Role/RoleBiding

```bash
kubectl create ns dev1
kubectl create ns dev2

kubectl apply -f developer-role.yaml
kubectl apply -f developer-rolebinding.yaml

kubectl apply -f sre-role.yaml
kubectl apply -f sre-rolebinding.yaml
```

## kubeconfig設定

```bash
# dodoria context
kubectl config set-credentials dodoria \
  --client-certificate=$WS/.env/dodoria.crt \
  --client-key=$WS/.env/dodoria.key
kubectl config set-context dodoria \
  --cluster=local-k8s --namespace=dev1 --user=dodoria
# zarbon context
kubectl config set-credentials zarbon \
  --client-certificate=$WS/.env/zarbon.crt \
  --client-key=$WS/.env/zarbon.key
kubectl config set-context zarbon \
  --cluster=local-k8s --namespace=dev2 --user=zarbon
# frieza context
kubectl config set-credentials frieza \
  --client-certificate=$WS/.env/frieza.crt \
  --client-key=$WS/.env/frieza.key
kubectl config set-context frieza \
  --cluster=local-k8s --namespace=default --user=frieza
```

## Test

```bash
kubectl config use-context dodoria

kubectl run test-dev1 --generator "run-pod/v1" --image busybox -- sh -c "while true; do echo 'わああーーー！！　フリーザ様ーーーーっ！！';sleep 5; done"
kubectl logs test-dev1

kubectl config use-context zarbon
kubectl run test-dev2 --generator "run-pod/v1" --image busybox -- sh -c "while true; do echo 'そ　そうだ…！ふ…ふたりで手を…組もう…そ…そうすればフ…フリーザに勝てるぞ…！';sleep 5; done"
kubectl logs test-dev2
kubectl run attack-dodoria -n dev1 --generator "run-pod/v1" --image busybox -- sleep 10000

kubectl config use-context frieza
kubectl run frieza -n dev1 --generator "run-pod/v1" --image busybox -- sh -c "while true; do echo '追うんですよ、ドドリアさん！つかまえなさい！';sleep 5; done"
```
