Khi triển khai thực tế (production), không ai expose thẳng NodePort để client gọi.
Thay vào đó, ta sẽ có một reverse proxy hoặc ingress controller làm điểm truy cập duy nhất, nhiệm vụ của nó là:

Nhận tất cả request từ bên ngoài (public / internal).

Cân bằng tải (load balance) giữa các pod backend (ví dụ: các replica spring-gateway).

Có thể route thêm theo path/domain (ví dụ /api/*, /admin/*…).

⚙️ Kiến trúc chuẩn thực tế
Client (Browser / API Consumer)
          │
          ▼
  ┌─────────────────────┐
  │ Reverse Proxy / Ingress│  ← điểm truy cập duy nhất
  │ (Nginx / Traefik / HAProxy)│
  └─────────────────────┘
          │
          ▼
  ┌──────────────────────────────┐
  │  K8s Service: spring-gateway │  ← load-balance giữa các pod
  │  (ClusterIP)                 │
  └──────────────────────────────┘
          │
          ▼
  ┌───────────────────────┐
  │ Pod 1: spring-gateway │
  │ Pod 2: spring-gateway │
  │ Pod 3: spring-gateway │
  └───────────────────────┘

🧠 Giải thích vai trò
Thành phần	Vai trò
Ingress / Reverse Proxy (bên ngoài cụm)	Điểm truy cập duy nhất. Có thể SSL termination, rewrite URL, auth, cache, routing.
Service (ClusterIP)	Cân bằng tải nội bộ giữa các pod cùng label selector.
Pod (ReplicaSet)	Các instance chạy ứng dụng Spring Gateway.

### Tạo Deployment, Pod, service loại Load Balancer
kubectl apply -f gateway-deployment.yaml
kubectl apply -f gateway-service.yaml

### Kiểm tra pod gateway
kubectl get pods -l app=spring-gateway


### Xem log theo thời gian thực (realtime)
kubectl logs -f -l app=spring-gateway
