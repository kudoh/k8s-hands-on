# ALB Ingress Controller

```bash
CLUSTER_NAME=frieza
# EKS setup
eksctl create cluster \
    --name $CLUSTER_NAME \
    --nodegroup-name standard-workers \
    --node-type m5.large \
    --nodes 3 \
    --auto-kubeconfig \
    --region ap-northeast-1 \
    --zones ap-northeast-1a,ap-northeast-1c,ap-northeast-1d \
    --external-dns-access \
    --alb-ingress-access

eksctl utils write-kubeconfig --name=$CLUSTER_NAME

# install helm
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/helm/tiller-rbac.yaml
helm init --service-account tiller

# install alb ingress controller
helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator
helm install incubator/aws-alb-ingress-controller \
  --name aws-alb-ingress-controller
  --set clusterName=$CLUSTER_NAME \
  --set autoDiscoverAwsRegion=true \
  --set autoDiscoverAwsVpcID=true

# deploy sample apps
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/ingress/nginx/app1.yaml
kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/ingress/nginx/app2.yaml

# apply Ingress Resource
PUBLIC_SUBNETS=$(aws ec2 describe-subnets \
  --filters "Name=tag:Name,Values=*${CLUSTER_NAME}*Public*" \
  --query 'Subnets[].SubnetId' --output text \
  | tr '\t' ',')

curl -sSL https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/ingress/alb/ingress.yaml | \
    sed -e "s#alb.ingress.kubernetes.io/subnets: ''#alb.ingress.kubernetes.io/subnets: $PUBLIC_SUBNETS#g" | \
    kubectl apply -f-

# install external-dns
curl -o external-dns.yaml https://raw.githubusercontent.com/kubernetes-sigs/aws-alb-ingress-controller/v1.1.2/docs/examples/external-dns.yaml
# edit
# kubectl apply -f https://raw.githubusercontent.com/kudoh/k8s-hands-on/master/ingress/alb/external-dns.yaml
kubectl apply -f external-dns.yaml    


```