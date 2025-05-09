# Twitter Microservice Architecture

## Tổng quan
Đây là một dự án microservice mô phỏng lại các chức năng cơ bản của Twitter, được xây dựng bằng Spring Boot và Spring Cloud. Dự án sử dụng kiến trúc microservice để đảm bảo khả năng mở rộng và bảo trì.

## Công nghệ sử dụng
- Java 21
- Spring Boot 3.4.5
- Spring Cloud 2024.0.1
- Spring Cloud Gateway
- Spring Cloud Config
- Spring Cloud Netflix Eureka
- Spring Security + OAuth2
- MySQL
- Keycloak
- Docker & Docker Compose
- Maven

## Cấu trúc project
```
twitter-microservice/
├── api-gateway/           # API Gateway service
├── config-service/        # Configuration service
├── registry-service/      # Service registry (Eureka)
├── profile-service/       # Profile management service
├── mysql-init-scripts/    # Database initialization scripts
└── docker-compose.service.yaml  # Docker compose configuration
```

## Các service chính

### 1. API Gateway (Port: 8080)
- Điểm vào duy nhất cho tất cả các request
- Xử lý routing và load balancing
- Xác thực và phân quyền thông qua OAuth2
- Circuit breaker pattern với Resilience4j
- Cấu hình chi tiết:
  - Route mapping cho các service
  - Circuit breaker cho xử lý lỗi
  - Security filter cho xác thực
  - Rate limiting và throttling
  - Request/Response transformation
  - Caching layer

### 2. Config Service (Port: 8888)
- Quản lý cấu hình tập trung cho tất cả các service
- Hỗ trợ nhiều môi trường (dev, prod, etc.)
- Tích hợp với Git để version control cấu hình
- Tính năng chi tiết:
  - Native profile cho lưu trữ cấu hình local
  - Git backend cho version control
  - Encryption/Decryption cho sensitive data
  - Refresh scope cho cập nhật cấu hình động
  - Health check và monitoring
  - Cấu hình riêng cho từng service

### 3. Registry Service (Port: 8761)
- Service discovery sử dụng Eureka
- Quản lý đăng ký và phát hiện service
- Health check và monitoring
- Tính năng chi tiết:
  - Service registration và deregistration
  - Service health monitoring
  - Load balancing integration
  - High availability configuration
  - Self-preservation mode
  - Dashboard cho monitoring
  - Metrics collection (Prometheus)

### 4. Profile Service (Port: 8081)
- Quản lý thông tin người dùng
- Xử lý các thao tác CRUD cho profile
- Tích hợp với MySQL database
- Tính năng chi tiết:
  - Entity: Profile (id, userId, username, email, firstName, lastName)
  - RESTful API endpoints
  - JPA/Hibernate cho persistence
  - Validation cho input data
  - Error handling
  - Service discovery integration
  - Health check endpoints
  - Metrics và logging

## Cơ sở dữ liệu
- MySQL được sử dụng làm database chính
- Mỗi service có thể có database riêng
- Script khởi tạo database được lưu trong thư mục `mysql-init-scripts`

## Bảo mật
- Xác thực thông qua Keycloak
- OAuth2 + JWT cho API Gateway
- Spring Security cho các service
- HTTPS được khuyến nghị cho môi trường production

## Cách chạy project

### Yêu cầu
- Java 21
- Maven
- Docker & Docker Compose
- MySQL

### Các bước chạy
1. Clone repository
2. Chạy các service cơ sở:
```bash
docker-compose -f docker-compose.service.yaml up -d
```
3. Build và chạy các service:
```bash
mvn clean install
mvn spring-boot:run
```

### Thứ tự khởi động
1. Registry Service
2. Config Service
3. Profile Service
4. API Gateway

## Monitoring và Logging
- Spring Boot Actuator cho health check
- Prometheus metrics
- Logging được cấu hình cho từng service

## Môi trường phát triển
- IDE: IntelliJ IDEA được khuyến nghị
- Lombok cho giảm boilerplate code
- Spring Boot DevTools cho hot reload

## Contributing
1. Fork repository
2. Tạo branch mới
3. Commit changes
4. Push lên branch
5. Tạo Pull Request

## License
[MIT License](LICENSE)
