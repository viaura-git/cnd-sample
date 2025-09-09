# scripts/deploy-cnd-dev-full.ps1
# IntelliJ 클릭 한 번으로 cnd-dev 전체 배포 + Gateway API CRD 설치 + Port-Forward

# 1️⃣ Gateway API CRD 확인/설치
Write-Host "==============================="
Write-Host "🚀 Step 1: Gateway API CRD 확인/설치"
Write-Host "==============================="
#if (-not (kubectl get crd gateways.gateway.networking.k8s.io 2>$null)) {
    # CRD가 없으면 설치
    kubectl kustomize "github.com/kubernetes-sigs/gateway-api/config/crd?ref=v1.3.0" | kubectl apply -f -
#}

# 1️⃣ istio-system 설치
Write-Host "==============================="
Write-Host "🚀 Step 2: Istio System install"
Write-Host "==============================="
istioctl install -f k8s/01.demo-profile-no-gateways.yaml -y

# 2️⃣ Namespace 적용
Write-Host "==============================="
Write-Host "🚀 Step 3: Namespace 적용"
Write-Host "==============================="
kubectl apply -f k8s/02.namespace.yaml

# 3️⃣ Deployments + Services 적용
Write-Host "==============================="
Write-Host "🚀 Step 4: Deployments + Services 적용"
Write-Host "==============================="
kubectl apply -f k8s/03.deployments-member.yaml
kubectl apply -f k8s/03.deployments-lea-api-common.yaml

kubectl get gateway -n cnd-dev

# 3️⃣ Deployments + Services 적용
Write-Host "==============================="
Write-Host "🚀 Step 5: Deployments + Services 적용"
Write-Host "==============================="
kubectl apply -f k8s/03-0.jwt-secret.yaml
kubectl apply -f k8s/03-1.jwt-config.yaml


# 3️⃣ Deployments + Services 적용
Write-Host "==============================="
Write-Host "🚀 Step 6: Deployments + Services 적용"
Write-Host "==============================="
kubectl apply -f k8s/04.cnd-ingress-gateway.yaml

# 6️⃣ Port-Forward
Write-Host "==============================="
Write-Host " Step 7: gateway annotate (because docker has no loadbalancer)"
Write-Host "==============================="
kubectl annotate gateway cnd-ingress-gateway networking.istio.io/service-type=ClusterIP --namespace=cnd-dev

# 3️⃣ Deployments + Services 적용
Write-Host "==============================="
Write-Host "🚀 Step 8: Deployments + Services 적용"
Write-Host "==============================="
kubectl apply -f k8s/05.cnd-gateway-authentication.yaml
kubectl apply -f k8s/06.cnd-gateway-authorization-policy.yaml

kubectl rollout restart deployment -n cnd-dev

# 6️⃣ Port-Forward
Write-Host "==============================="
Write-Host " Step 14: Port-Forward cnd-com-gateway:8080 -> localhost:80"
Write-Host " kubectl port-forward svc/cnd-ingress-gateway-istio 8080:80 -n cnd-dev "
Write-Host " test :  http://localhost:8080/api/members/v1/23 "
Write-Host " do in Powershell  "
Write-Host "==============================="
#Start-Process powershell -ArgumentList "-NoExit", "-Command", "kubectl port-forward svc/cnd-ingress-gateway-istio 8080:80 -n cnd-dev"
#Write-Host "Port-Forward 실행 중 (새 PowerShell 창)"
kubectl get pods -n cnd-dev
