# Twitter Microservice Architecture

## Overview
This project is a comprehensive microservice implementation simulating the core functionality of Twitter, built with Spring Boot and Spring Cloud. The architecture is designed for scalability, maintainability, and resilience, employing modern cloud-native patterns and technologies.

## Table of Contents
- [Technology Stack](#technology-stack)
- [Project Architecture](#project-architecture)
- [Core Services](#core-services)
- [Database Architecture](#database-architecture)
- [Security Architecture](#security-architecture)
- [Event-Driven Architecture](#event-driven-architecture)
- [Caching Strategy](#caching-strategy)
- [Configuration Management](#configuration-management)
- [Prerequisites](#prerequisites)
- [Infrastructure Setup](#infrastructure-setup)
- [Running the Project](#running-the-project)
- [API Documentation](#api-documentation)
- [Monitoring and Observability](#monitoring-and-observability)
- [Deployment](#deployment)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

## Technology Stack
- **Java**: JDK 21
- **Framework**: Spring Boot 3.4.5, Spring Cloud 2024.0.1
- **Service Discovery**: Spring Cloud Netflix Eureka
- **Configuration Management**: Spring Cloud Config with Git backend
- **API Gateway**: Spring Cloud Gateway
- **Circuit Breaking & Resilience**: Resilience4j
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Messaging**: RabbitMQ
- **Security**: OAuth2, JWT, Keycloak
- **Databases**:
  - MySQL (Profile Service)
  - MongoDB (Tweet, Comment, Follow, Notification, Timeline Services)
  - Elasticsearch (Search Service)
  - Redis (Caching for various services)
- **Storage**: Azure Blob Storage (Media Service)
- **Build Tool**: Maven
- **Container**: Docker & Docker Compose
- **Monitoring**: Spring Boot Actuator, Prometheus, Grafana
- **Testing**: JUnit 5, Testcontainers, WireMock

## Project Architecture

### System Overview
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Web Client    │────│   Mobile Client  │────│   Third Party   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────────┐
                    │    API Gateway      │
                    │      (8080)         │
                    └─────────────────────┘
                                 │
                    ┌─────────────────────┐
                    │   Service Registry  │
                    │      (8761)         │
                    └─────────────────────┘
                                 │
    ┌────────────────────────────┼────────────────────────────┐
    │                           │                             │
┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
│ Profile   │  │   Tweet   │  │  Comment  │  │  Follow   │  │ Timeline  │
│ Service   │  │ Service   │  │ Service   │  │ Service   │  │ Service   │
│  (8081)   │  │  (8083)   │  │  (8085)   │  │  (8086)   │  │  (8087)   │
└───────────┘  └───────────┘  └───────────┘  └───────────┘  └───────────┘

┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
│   Media   │  │Notification│  │  Search   │  │  Config   │
│ Service   │  │ Service    │  │ Service   │  │ Service   │
│  (8082)   │  │  (8084)    │  │  (8089)   │  │  (8888)   │
└───────────┘  └───────────┘  └───────────┘  └───────────┘
```

## Core Services

### 1. API Gateway (Port: 8080)
The API Gateway serves as the single entry point for all client requests and provides:
- Centralized routing to microservices
- Load balancing with service discovery integration
- Authentication and authorization via OAuth2/JWT
- Circuit breaker patterns with Resilience4j
- Request rate limiting and throttling
- Request/response transformation
- Swagger UI aggregation for all services
- CORS handling
- Request/response logging
- Monitoring endpoints

**Key Features:**
- Dynamic routing based on service discovery
- JWT token validation and forwarding
- Rate limiting per user/IP
- Circuit breaker with fallback responses
- Request timeout configuration
- Health check aggregation

### 2. Config Service (Port: 8888)
The Config Service provides externalized configuration for all microservices:
- Centralized configuration management
- Environment-specific configurations (dev, prod, test)
- Git integration for version control of configurations
- Encrypted property support for sensitive data
- Dynamic configuration updates with refresh scope
- High availability configuration
- Health monitoring endpoints

**Configuration Structure:**
```
config-repo/
├── application.yaml              # Common base config
├── service-name.yaml            # Service base config
├── service-name-dev.yaml        # Development config
├── service-name-prod.yaml       # Production config
└── service-name-test.yaml       # Testing config
```

### 3. Registry Service (Port: 8761)
The Registry Service implements service discovery using Netflix Eureka:
- Automatic service registration and discovery
- Health monitoring of registered services
- Dashboard for service status visualization
- Self-preservation mode for network partitions
- Load balancing integration
- Resilience to network issues
- Instance metadata management

### 4. Profile Service (Port: 8081)
The Profile Service manages user profiles and accounts:
- User profile CRUD operations
- MySQL database for persistent storage
- Redis caching for frequently accessed profiles
- Keycloak integration for account management
- User preferences management
- Follow relationship status tracking
- Event publishing for profile changes
- Integration with media service for profile images

**Database Schema:**
- Users table with profile information
- User preferences and settings
- Profile statistics (followers, following counts)

### 5. Tweet Service (Port: 8083)
The Tweet Service handles the core content creation functionality:
- Tweet creation, retrieval, update, and deletion
- Support for text content, hashtags, and mentions
- Thread creation and management
- Engagement tracking (likes, retweets, views)
- MongoDB for document storage
- Redis for caching and counters
- Event publishing for timeline updates
- Media attachments via Media Service integration
- Content moderation capabilities

**Tweet Document Structure:**
```json
{
  "id": "ObjectId",
  "userId": "string",
  "content": "string",
  "hashtags": ["string"],
  "mentions": ["string"],
  "mediaIds": ["string"],
  "stats": {
    "likes": 0,
    "retweets": 0,
    "replies": 0,
    "views": 0
  },
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### 6. Comment Service (Port: 8085)
The Comment Service manages replies and conversations:
- Comment creation and threading
- Nested replies support
- Engagement tracking for comments
- MongoDB for document storage
- Event publishing for notifications
- Integration with tweets and user profiles
- Content moderation capabilities
- Pagination and sorting for comment threads
- Real-time updates for active conversations

### 7. Follow Service (Port: 8086)
The Follow Service manages social connections between users:
- Follow/unfollow functionality
- Follower and following lists
- Blocking and muting capabilities
- Suggested users to follow
- Event publishing for timeline updates
- MongoDB for relationship storage
- Social graph analysis for recommendations
- Follow request handling for private accounts
- Analytics for follow patterns

### 8. Timeline Service (Port: 8087)
The Timeline Service generates personalized user feeds:
- Home timeline construction from followed accounts
- Algorithmic "For You" timeline recommendations
- Chronological "Latest" timeline
- Topic-based timelines
- Event-driven updates from Tweet/Comment/Follow services
- MongoDB for timeline storage and caching
- Redis caching for improved performance
- Personalization based on user interests and engagement
- Real-time updates for active users
- Pagination and infinite scrolling support

### 9. Media Service (Port: 8082)
The Media Service handles all media content:
- Image, video, and GIF uploads and storage
- Azure Blob Storage integration
- Media processing and optimization
- Thumbnail generation
- Content-type validation
- Secure access controls
- MIME type detection with Apache Tika
- Support for various media formats
- Integration with tweets and user profiles

**Supported Formats:**
- Images: JPEG, PNG, GIF, WebP
- Videos: MP4, MOV, AVI (up to 10MB)
- Maximum file size: 10MB per file
- Automatic thumbnail generation

### 10. Notification Service (Port: 8084)
The Notification Service manages user alerts and notifications:
- Real-time notification delivery
- Multiple notification types (mentions, likes, follows, etc.)
- Firebase Cloud Messaging integration for push notifications
- Email notifications via SendGrid
- Notification preferences management
- MongoDB for notification storage
- Delivery status tracking
- Read/unread state management
- Notification batching and throttling

**Notification Types:**
- New follower
- Tweet liked/retweeted
- Mention in tweet
- Reply to tweet
- New comment
- Direct messages

### 11. Search Service (Port: 8089)
The Search Service provides robust search capabilities:
- Full-text search across tweets, users, and hashtags
- Elasticsearch for high-performance indexing and querying
- Real-time indexing via event processing
- Redis caching for search results
- Relevance ranking and scoring
- Filtering and faceted search
- Autocomplete and search suggestions
- Trending topics identification
- Type-ahead search functionality
- Personalized search results

**Search Features:**
- Global search across all content
- User-specific searches
- Hashtag trending analysis
- Search result caching
- Advanced filtering options

### 12. Common Library
The Common module provides shared components across services:
- Data transfer objects (DTOs)
- Exception handling
- Common utilities
- Event definitions
- Security configurations
- Response models
- Validation utilities
- Messaging configurations

## Database Architecture
The project uses a polyglot persistence approach:

- **MySQL**: Relational data for user profiles
  - Tables: users, user_preferences, user_stats
  - ACID compliance for critical user data
  - Master-slave replication for read scaling

- **MongoDB**: Document storage for tweets, comments, follows, notifications, and timelines
  - Collections: tweets, comments, follows, notifications, timelines
  - Horizontal scaling capability
  - Indexing for query optimization

- **Elasticsearch**: Search indexing for tweets, users, and hashtags
  - Real-time indexing
  - Full-text search capabilities
  - Aggregations for analytics

- **Redis**: Caching, session management, rate limiting, and counters
  - Profile caching (5 minutes TTL)
  - Tweet statistics caching (1 minute TTL)
  - Timeline caching (3 minutes TTL)
  - Search results caching (5 minutes TTL)
  - Rate limiting counters

- **Azure Blob Storage**: Media files (images, videos, GIFs)
  - CDN integration for global distribution
  - Automatic backup and redundancy
  - Cost-effective storage for large files

Each service owns its data store, following the database-per-service pattern to ensure loose coupling and independent scaling.

## Security Architecture
Security is implemented through multiple layers:

- **Authentication**: Keycloak as the identity provider
  - OAuth2/OpenID Connect
  - JWT token-based authentication
  - Multi-factor authentication support

- **Authorization**: OAuth2 with JWT tokens
  - Role-based access control (RBAC)
  - Scope-based permissions
  - Token validation at API Gateway

- **API Gateway**: Token validation and authorization policies
  - JWT signature verification
  - Token expiration checking
  - Route-based authorization

- **Microservices**: Resource server configuration
  - Method-level security
  - User context propagation
  - Service-to-service authentication

- **Transport Security**: HTTPS recommended for all communications
- **Data Security**: Encrypted sensitive data in configuration
- **Rate Limiting**: Protection against abuse and DoS attacks
- **Content Security**: Input validation and content moderation

## Event-Driven Architecture
The services communicate through both synchronous (REST) and asynchronous (messaging) patterns:

- **RabbitMQ**: Message broker for event publishing and consumption
- **Event Types**:
  - User events (registration, profile updates)
  - Content events (tweet creation, comments)
  - Engagement events (likes, retweets)
  - Social events (follows, mentions)
  - Notification events

**Event Flow Example:**
```
Tweet Created → Tweet Service → RabbitMQ → Timeline Service → User Timelines Updated
                              ↓
                            Search Service → Elasticsearch Index Updated
                              ↓
                          Notification Service → Push Notifications Sent
```

This allows for loose coupling between services and enables real-time updates across the system.

## Caching Strategy

### Services with Redis Caching:
1. **Profile Service**: User profiles, follower counts (5-10 minutes TTL)
2. **Tweet Service**: Tweet content, statistics (1-30 minutes TTL)
3. **Timeline Service**: User timelines, home feeds (2-3 minutes TTL)
4. **Search Service**: Search results, suggestions (5 minutes - 1 hour TTL)

### Cache Configuration:
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 300000  # 5 minutes default
      cache-null-values: false
```

### Cache Eviction Strategy:
- Time-based expiration (TTL)
- Event-driven invalidation
- Manual cache refresh endpoints
- Memory-based eviction for Redis

## Configuration Management

### Environment-Specific Configurations:
- **Development**: Local services, debug logging, relaxed security
- **Production**: External services, minimal logging, strict security
- **Testing**: In-memory databases, mock services

### Sensitive Data Management:
- Environment variables for secrets
- Encrypted properties in Git repository
- Keycloak client secrets
- Database passwords
- API keys (SendGrid, Azure Storage)

### Configuration Refresh:
```bash
# Refresh configuration without restart
curl -X POST http://service-url/actuator/refresh
```

## Prerequisites
- **Java**: JDK 21+
- **Maven**: 3.8+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Git**: For configuration repository

### Required Infrastructure:
- **MySQL**: 8.0+
- **MongoDB**: 5.0+
- **Elasticsearch**: 8.0+
- **RabbitMQ**: 3.9+
- **Redis**: 6.2+
- **Keycloak**: 20.0+

## Infrastructure Setup

### Using Docker Compose:
```bash
# Start all infrastructure services
docker-compose -f docker-compose.infrastructure.yml up -d

# Verify services are running
docker-compose ps
```

### Infrastructure Services:
```yaml
# docker-compose.infrastructure.yml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: profile_service
    volumes:
      - mysql_data:/var/lib/mysql

  mongodb:
    image: mongo:5.0
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: 1234
    volumes:
      - mongodb_data:/data/db

  elasticsearch:
    image: elasticsearch:8.5.0
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

  rabbitmq:
    image: rabbitmq:3.9-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin

  redis:
    image: redis:6.2
    ports:
      - "6379:6379"

  keycloak:
    image: quay.io/keycloak/keycloak:20.0
    ports:
      - "9000:8080"
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    command: start-dev

volumes:
  mysql_data:
  mongodb_data:
  elasticsearch_data:
```

### Manual Installation:
```bash
# MySQL
sudo apt install mysql-server-8.0

# MongoDB
sudo apt install mongodb

# Elasticsearch
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.5.0-linux-x86_64.tar.gz

# RabbitMQ
sudo apt install rabbitmq-server

# Redis
sudo apt install redis-server

# Keycloak
wget https://github.com/keycloak/keycloak/releases/download/20.0.0/keycloak-20.0.0.tar.gz
```

## Running the Project

### Build All Services:
```bash
# Clean and build all services
mvn clean package -DskipTests

# Build specific service
cd profile-service
mvn clean package
```

### Starting Services (Recommended Order):
```bash
# 1. Start Registry Service
cd registry-service
mvn spring-boot:run

# 2. Start Config Service
cd config-service
export GITHUB_TOKEN=your_github_token
mvn spring-boot:run

# 3. Start other services (can be parallel)
cd profile-service
mvn spring-boot:run -Dspring.profiles.active=dev

cd tweet-service
mvn spring-boot:run -Dspring.profiles.active=dev

cd comment-service
mvn spring-boot:run -Dspring.profiles.active=dev

cd follow-service
mvn spring-boot:run -Dspring.profiles.active=dev

cd timeline-service
mvn spring-boot:run -Dspring.profiles.active=dev

cd media-service
mvn spring-boot:run -Dspring.profiles.active=dev

cd notification-service
mvn spring-boot:run -Dspring.profiles.active=dev

cd search-service
mvn spring-boot:run -Dspring.profiles.active=dev

# 4. Start API Gateway (last)
cd api-gateway
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Using Docker:
```bash
# Build Docker images
docker build -t twitter/registry-service ./registry-service
docker build -t twitter/config-service ./config-service
# ... repeat for all services

# Run with Docker Compose
docker-compose up -d
```

### Environment Variables:
```bash
# Development
export SPRING_PROFILES_ACTIVE=dev
export GITHUB_TOKEN=ghp_your_github_token
export SENDGRID_API_KEY=SG.your_sendgrid_key

# Production
export SPRING_PROFILES_ACTIVE=prod
export MONGO_URI=mongodb://prod-user:password@prod-cluster
export DATABASE_URL=jdbc:mysql://prod-mysql:3306/profile_service
export REDIS_HOST=prod-redis-cluster
export ELASTICSEARCH_URIS=https://prod-elasticsearch:9200
```

## API Documentation

### Swagger UI Access:
- **API Gateway Aggregated**: http://localhost:8080/swagger-ui.html
- **Individual Services**:
  - Profile Service: http://localhost:8081/swagger-ui.html
  - Tweet Service: http://localhost:8083/swagger-ui.html
  - Comment Service: http://localhost:8085/swagger-ui.html
  - Follow Service: http://localhost:8086/swagger-ui.html
  - Timeline Service: http://localhost:8087/swagger-ui.html
  - Media Service: http://localhost:8082/swagger-ui.html
  - Notification Service: http://localhost:8084/swagger-ui.html
  - Search Service: http://localhost:8089/swagger-ui.html

### API Examples:

#### Authentication:
```bash
# Get access token from Keycloak
curl -X POST http://localhost:9000/realms/twitter/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=social_app_backend&username=user&password=password"
```

#### Create Tweet:
```bash
curl -X POST http://localhost:8080/api/tweets \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"content": "Hello Twitter Clone!", "hashtags": ["springboot", "microservices"]}'
```

#### Get User Timeline:
```bash
curl -X GET http://localhost:8080/api/timeline/user/123 \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

#### Search Tweets:
```bash
curl -X GET "http://localhost:8080/api/search/tweets?query=microservices" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

## Monitoring and Observability

### Health Checks:
```bash
# Check all services health
curl http://localhost:8080/actuator/health

# Individual service health
curl http://localhost:8081/actuator/health
```

### Metrics Endpoints:
- **Prometheus Metrics**: `/actuator/prometheus`
- **Application Metrics**: `/actuator/metrics`
- **JVM Metrics**: `/actuator/metrics/jvm.*`
- **Custom Metrics**: `/actuator/metrics/custom.*`

### Logging Configuration:
```yaml
logging:
  level:
    com.david: DEBUG
    org.springframework.web: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
```

### Distributed Tracing:
- Request correlation IDs
- Service call tracing
- Performance monitoring
- Error tracking

## Deployment

### Production Deployment Checklist:
- [ ] Environment variables configured
- [ ] Database migrations applied
- [ ] SSL certificates installed
- [ ] Monitoring and alerting configured
- [ ] Backup strategies implemented
- [ ] Load balancers configured
- [ ] Auto-scaling policies set

### Docker Production:
```dockerfile
# Multi-stage build example
FROM openjdk:21-jdk-slim as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM openjdk:21-jre-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes Deployment:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: profile-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: profile-service
  template:
    metadata:
      labels:
        app: profile-service
    spec:
      containers:
      - name: profile-service
        image: twitter/profile-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
```

## Testing

### Unit Tests:
```bash
# Run all unit tests
mvn test

# Run tests for specific service
cd profile-service
mvn test
```

### Integration Tests:
```bash
# Run integration tests with Testcontainers
mvn verify -P integration-tests
```

### API Testing:
```bash
# Using Newman (Postman CLI)
newman run twitter-api-tests.postman_collection.json \
  -e dev-environment.postman_environment.json
```

### Performance Testing:
```bash
# Using Apache Bench
ab -n 1000 -c 10 http://localhost:8080/api/tweets

# Using JMeter
jmeter -n -t twitter-load-test.jmx -l results.jtl
```

### Test Coverage:
```bash
# Generate coverage report
mvn jacoco:report

# View coverage in target/site/jacoco/index.html
```

## Troubleshooting

### Common Issues:

#### Service Registration Issues:
```bash
# Check Eureka dashboard
http://localhost:8761

# Verify service configuration
curl http://localhost:8081/actuator/info
```

#### Database Connection Issues:
```bash
# Check database connectivity
curl http://localhost:8081/actuator/health

# Verify database configuration
docker exec -it mysql_container mysql -u root -p
```

#### RabbitMQ Message Issues:
```bash
# Check RabbitMQ management
http://localhost:15672 (admin/admin)

# Verify message queues
curl http://localhost:8081/actuator/health
```

#### Memory Issues:
```bash
# Increase JVM heap size
export JAVA_OPTS="-Xmx2g -Xms1g"

# Monitor memory usage
curl http://localhost:8081/actuator/metrics/jvm.memory.used
```

### Debugging:
```bash
# Enable debug logging
export LOGGING_LEVEL_COM_DAVID=DEBUG

# Remote debugging
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -jar app.jar
```

### Log Analysis:
```bash
# Search for errors
grep -r "ERROR" logs/

# Monitor real-time logs
tail -f logs/application.log

# Analyze specific service logs
docker logs -f container_name
```

## Contributing

### Development Workflow:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

### Code Standards:
- Java 21 features encouraged
- Spring Boot best practices
- Comprehensive unit tests (>80% coverage)
- Integration tests for critical paths
- API documentation with Swagger
- Proper error handling and logging

### Branch Strategy:
- `main`: Production-ready code
- `develop`: Integration branch
- `feature/`: New features
- `bugfix/`: Bug fixes
- `hotfix/`: Critical production fixes

### Pull Request Requirements:
- [ ] Code compiles without warnings
- [ ] All tests pass
- [ ] Code coverage maintained/improved
- [ ] Documentation updated
- [ ] Changelog updated
- [ ] API documentation updated

This comprehensive README provides a complete guide to understanding, setting up, running, and maintaining the Twitter microservice architecture. It covers all aspects from development to production deployment, making it easy for both new contributors and operations teams to work with the system.