### Tạo Deployment, Pod, service loại Load Balancer
kubectl apply -f gateway-deployment.yaml
kubectl apply -f gateway-service.yaml

### Kiểm tra pod gateway
kubectl get pods -l app=spring-gateway


### Xem log theo thời gian thực (realtime)
kubectl logs -f -l app=spring-gateway
