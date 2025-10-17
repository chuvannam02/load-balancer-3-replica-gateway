Khi triển khai thực tế (production), không ai expose thẳng NodePort để client gọi.
Thay vào đó, ta sẽ có một reverse proxy hoặc ingress controller làm điểm truy cập duy nhất, nhiệm vụ của nó là:

Nhận tất cả request từ bên ngoài (public / internal).

Cân bằng tải (load balance) giữa các pod backend (ví dụ: các replica spring-gateway).

Có thể route thêm theo path/domain (ví dụ /api/*, /admin/*…).

⚙️ Kiến trúc chuẩn thực tế
```
Client (Browser / API Consumer)
          │
          ▼
  ┌──────────────────────────────┐
  │ Reverse Proxy / Ingress      │  ← điểm truy cập duy nhất
  │ (Nginx / Traefik / HAProxy)  │
  └──────────────────────────────┘
          │
          ▼
  ┌──────────────────────────────┐
  │ K8s Service: spring-gateway  │  ← Load balance giữa các pod
  │ (ClusterIP)                  │
  └──────────────────────────────┘
          │
          ▼
  ┌──────────────────────────────┐
  │ Pod 1: spring-gateway        │
  │ Pod 2: spring-gateway        │
  │ Pod 3: spring-gateway        │
  └──────────────────────────────┘

  └───────────────────────┘
```
> 💡 Gợi ý:  
Nếu bạn muốn hiển thị trực quan hơn (với mũi tên và box rõ nét) khi render trong Markdown viewer hỗ trợ Mermaid, bạn có thể dùng **Mermaid diagram** như sau:

```markdown
```mermaid
flowchart TD
    A[Client (Browser / API Consumer)]
    B[Reverse Proxy / Ingress<br/>(Nginx / Traefik / HAProxy)]
    C[K8s Service: spring-gateway<br/>(ClusterIP, load-balance giữa các pod)]
    D1[Pod 1: spring-gateway]
    D2[Pod 2: spring-gateway]
    D3[Pod 3: spring-gateway]

    A --> B --> C --> D1
    C --> D2
    C --> D3
```

🔹 **Cách dùng:**  
- Lưu nội dung trên vào file `architecture.md`  
- Mở trên GitHub hoặc VS Code (cài extension *Markdown Preview Mermaid Support*)  
→ Bạn sẽ thấy sơ đồ hiển thị trực quan đẹp mắt, có mũi tên và box rõ ràng.  

Bạn muốn mình thêm **Ingress Controller thực tế (ví dụ nginx-ingress-controller)** vào sơ đồ này luôn không?

⚙️ Giải thích cụ thể:
Thành phần	Vai trò	Thực tế triển khai
Client (Browser / API Consumer)	Gửi request từ bên ngoài (Internet hoặc nội bộ)	Truy cập qua domain hoặc IP public
Ingress Controller (ví dụ: Nginx Ingress Controller)	Reverse Proxy & Load Balancer cấp L7 (HTTP)	Nhận request từ client, định tuyến vào các dịch vụ trong cluster
Ingress Resource	Khai báo rule routing (ví dụ /api → spring-gateway)	Dùng YAML để cấu hình URL path, host, TLS...
Service (spring-gateway)	Load balancing nội bộ giữa các Pod gateway	Thường dùng ClusterIP
Pods (spring-gateway)	Chạy ứng dụng Spring Cloud Gateway thực tế	Được scale tùy nhu cầu (replicas: 3)

```
Client (Browser / API Consumer)
          │
          ▼
┌──────────────────────────────┐
│ Reverse Proxy / Ingress      │   ← điểm truy cập duy nhất
│ (Nginx Ingress Controller)   │
└──────────────────────────────┘
          │
          ▼
┌──────────────────────────────┐
│ Ingress Resource             │
│ /api → spring-gateway        │
└──────────────────────────────┘
          │
          ▼
┌──────────────────────────────┐
│ K8s Service: spring-gateway  │   ← load balance giữa các Pod
│ Type: ClusterIP              │
└──────────────────────────────┘
          │
          ▼
┌──────────────────────────────┐
│ Pod 1: spring-gateway        │
│ Pod 2: spring-gateway        │
│ Pod 3: spring-gateway        │
└──────────────────────────────┘

```

🧠 Giải thích vai trò
Thành phần	Vai trò
Ingress / Reverse Proxy (bên ngoài cụm)	Điểm truy cập duy nhất. Có thể SSL termination, rewrite URL, auth, cache, routing.
Service (ClusterIP)	Cân bằng tải nội bộ giữa các pod cùng label selector.
Pod (ReplicaSet)	Các instance chạy ứng dụng Spring Gateway.

🧩 1️⃣ Chuẩn bị Playground

Truy cập playground
👉 https://kodekloud.com/public-playgrounds/multi-node-k8s-1-34/
Kiểm tra cluster:
```
kubectl get nodes -o wide
```
Bạn sẽ thấy controlplane và node01.

🚀 2️⃣ Cài đặt Nginx Ingress Controller
```
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
```

Chờ Ingress Controller khởi động:
```
kubectl get pods -n ingress-nginx
```

Khi thấy Running:
```
ingress-nginx-controller-xxxxx   1/1     Running
```

⚙️ 3️⃣ Tạo Deployment + Service cho spring-gateway
Tạo file:
```
nano spring-gateway.yaml hoặc vi spring-gateway.yaml
```
Dán nội dung sau:
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-gateway
  labels:
    app: spring-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-gateway
  template:
    metadata:
      labels:
        app: spring-gateway
    spec:
      containers:
        - name: spring-gateway
          image: docker.io/betaplaptrinh/gateway:0.0.1
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 20
            periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: spring-gateway
spec:
  type: ClusterIP
  selector:
    app: spring-gateway
  ports:
    - port: 80
      targetPort: 8080
```
### Triển khai Deployment, Pod, service loại ClusterIP vì không có IP public để sử dụng Load Balancer
kubectl apply -f gateway-deployment.yaml
kubectl apply -f gateway-service.yaml

### Kiểm tra pod gateway
kubectl get pods
kubectl get pods -l app=spring-gateway

### Xem log theo thời gian thực (realtime)
kubectl logs -f -l app=spring-gateway

🌐 4️⃣ Tạo Ingress để định tuyến /api → spring-gateway
Tạo file:
```
nano spring-gateway-ingress.yaml hoặc vi spring-gateway-ingress.yaml
```
Dán nội dung sau:
```
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: spring-gateway-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
spec:
  ingressClassName: nginx
  rules:
    - http:
        paths:
          - path: /api(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: spring-gateway
                port:
                  number: 80
```
Áp dụng:
```
kubectl apply -f spring-gateway-ingress.yaml
```

🧠 5️⃣ Kiểm tra routing
Lấy IP node (vì playground không hỗ trợ LoadBalancer public):
```
kubectl get nodes -o wide
```
→ Giả sử IP của node01 là 172.17.0.2
Thử truy cập:
```
curl http://172.17.0.2/api/whoami
```
  
Bạn sẽ nhận được:
```
✅ Gateway handled by pod: spring-gateway-xxxxx
```
<img width="1542" height="536" alt="image" src="https://github.com/user-attachments/assets/4f698bd9-be8a-4d0a-beb3-de9dcef36d0e" />

