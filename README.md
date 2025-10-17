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


🔹 **Cách dùng:**  
- Lưu nội dung trên vào file `architecture.md`  
- Mở trên GitHub hoặc VS Code (cài extension *Markdown Preview Mermaid Support*)  
→ Bạn sẽ thấy sơ đồ hiển thị trực quan đẹp mắt, có mũi tên và box rõ ràng.  

Bạn muốn mình thêm **Ingress Controller thực tế (ví dụ nginx-ingress-controller)** vào sơ đồ này luôn không?

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
