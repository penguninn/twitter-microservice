# Twitter Microservices Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.1-brightgreen.svg)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> A production-ready, scalable Twitter clone built with Spring Boot microservices architecture, featuring OAuth2 authentication, real-time notifications, and comprehensive media handling.

## 🚀 Features

- **User Management**: Complete profile lifecycle with Keycloak OAuth2 integration
- **Social Media Core**: Tweet creation, engagement (likes), commenting, and following
- **Real-time Search**: Elasticsearch-powered full-text search across tweets and users
- **Media Handling**: Azure Blob Storage integration for images and videos
- **Push Notifications**: Firebase Cloud Messaging for real-time alerts
- **Timeline Generation**: Event-driven personalized feed aggregation
- **Microservices Architecture**: 12 independently deployable services
- **Circuit Breakers**: Resilience4j for fault tolerance
- **Event-Driven**: RabbitMQ messaging for loose coupling
- **Multi-Database**: Polyglot persistence with MySQL, MongoDB, and Elasticsearch

## 📋 Table of Contents

- [🏗️ Architecture](#️-architecture)
- [🛠️ Technology Stack](#️-technology-stack)
- [📦 Services Overview](#-services-overview)
- [🗄️ Data Architecture](#️-data-architecture)
- [🔐 Security](#-security)
- [⚡ Event System](#-event-system)
- [🚀 Quick Start](#-quick-start)
- [📖 API Documentation](#-api-documentation)
- [🐳 Deployment](#-deployment)
- [📊 Monitoring](#-monitoring)
- [🤝 Contributing](#-contributing)

## 🛠️ Technology Stack

### Core Framework
- **Java 21** - Modern JDK with latest features
- **Spring Boot 3.4.5** - Main application framework
- **Spring Cloud 2024.0.1** - Microservices toolkit
- **Maven** - Dependency management and build tool

### Infrastructure
- **Service Discovery**: Spring Cloud Netflix Eureka
- **API Gateway**: Spring Cloud Gateway with load balancing
- **Configuration**: Spring Cloud Config Server with Git backend
- **Circuit Breakers**: Resilience4j for fault tolerance
- **Message Broker**: RabbitMQ for event-driven communication

### Security
- **Authentication**: Keycloak OAuth2/OpenID Connect
- **Authorization**: JWT tokens with role-based access control
- **Resource Security**: Spring Security OAuth2 Resource Server

### Data Storage
- **MySQL 8.0+** - User profiles and relational data
- **MongoDB 5.0+** - Document storage for tweets, comments, follows
- **Elasticsearch 9.0+** - Full-text search and analytics
- **Redis 6.2+** - Caching and session storage
- **Azure Blob Storage** - Media files storage

### Monitoring & DevOps
- **Spring Boot Actuator** - Application monitoring endpoints
- **Docker & Docker Compose** - Containerization
- **Swagger/OpenAPI 3** - API documentation
- **Custom Keycloak Theme** - Tailwind CSS styling

## 🏗️ Architecture

### System Overview

```mermaid
graph TB
    Client["🌐 Clients<br/>(Web/Mobile/API)"]
    
    subgraph "Infrastructure Layer"
        Gateway["🚪 API Gateway<br/>:8080"]
        Registry["📋 Service Registry<br/>:8761"]
        Config["⚙️ Config Server<br/>:8888"]
        Keycloak["🔐 Keycloak<br/>:9000"]
    end
    
    subgraph "Business Services"
        Profile["👤 Profile Service<br/>:8081"]
        Tweet["🐦 Tweet Service<br/>:8083"]
        Comment["💬 Comment Service<br/>:8085"]
        Follow["👥 Follow Service<br/>:8086"]
        Timeline["📰 Timeline Service<br/>:8087"]
        Media["📸 Media Service<br/>:8082"]
        Notification["🔔 Notification Service<br/>:8084"]
        Search["🔍 Search Service<br/>:8089"]
    end
    
    subgraph "Data Layer"
        MySQL[("MySQL<br/>Profiles")]
        MongoDB[("MongoDB<br/>Tweets/Comments/Follows")]
        Elasticsearch[("Elasticsearch<br/>Search Index")]
        Redis[("Redis<br/>Cache")]
        Azure[("Azure Blob<br/>Media Storage")]
    end
    
    subgraph "Messaging"
        RabbitMQ["🐰 RabbitMQ<br/>Event Bus"]
    end
    
    Client --> Gateway
    Gateway --> Registry
    Gateway --> Keycloak
    
    Gateway --> Profile
    Gateway --> Tweet
    Gateway --> Comment
    Gateway --> Follow
    Gateway --> Timeline
    Gateway --> Media
    Gateway --> Notification
    Gateway --> Search
    
    Profile --> MySQL
    Tweet --> MongoDB
    Comment --> MongoDB
    Follow --> MongoDB
    Timeline --> MongoDB
    Notification --> MongoDB
    Search --> Elasticsearch
    
    Profile --> Redis
    Tweet --> Redis
    Timeline --> Redis
    Search --> Redis
    
    Media --> Azure
    
    Profile --> RabbitMQ
    Tweet --> RabbitMQ
    Follow --> RabbitMQ
    Comment --> RabbitMQ
    Notification --> RabbitMQ
    Search --> RabbitMQ
    Timeline --> RabbitMQ
```

### Key Architecture Principles

- **Database per Service**: Each microservice owns its data
- **Event-Driven Communication**: Asynchronous messaging via RabbitMQ
- **Circuit Breaker Pattern**: Fault tolerance with Resilience4j
- **Centralized Configuration**: Git-based configuration management
- **Service Discovery**: Automatic service registration and discovery
- **API Gateway Pattern**: Single entry point with authentication
- **Polyglot Persistence**: Right database for the right job

## 📦 Services Overview

### 🚪 API Gateway
**Port**: 8080 | **Database**: None | **Tech**: Spring Cloud Gateway + OAuth2

The unified entry point for all client requests, providing:
- **Authentication**: OAuth2/JWT token validation via Keycloak
- **Routing**: Dynamic load-balanced routing to microservices
- **Circuit Breakers**: Resilience4j fault tolerance patterns
- **Swagger Aggregation**: Centralized API documentation
- **CORS**: Cross-origin request handling

**Routes**:
```yaml
/api/profiles/** → Profile Service
/api/tweets/** → Tweet Service
/api/comments/** → Comment Service
/api/follows/** → Follow Service
/api/timeline/** → Timeline Service
/api/media/** → Media Service
/api/notifications/** → Notification Service
/api/search/** → Search Service
```

### ⚙️ Config Service
**Port**: 8888 | **Database**: Git Repository | **Tech**: Spring Cloud Config Server

Centralized configuration management with Git backend:
- **Configuration Source**: [https://github.com/penguninn/social-app-config.git](https://github.com/penguninn/social-app-config.git)
- **Environment Profiles**: dev, prod, test configurations
- **Security**: Encrypted sensitive properties
- **Refresh**: Dynamic configuration updates via `/actuator/refresh`

**Configuration Pattern**:
```
{service-name}-{profile}.yaml
example: profile-service-dev.yaml
```

### 📋 Service Registry
**Port**: 8761 | **Database**: In-Memory | **Tech**: Netflix Eureka Server

Service discovery and registration hub:
- **Auto-Registration**: Services register automatically on startup
- **Health Monitoring**: Continuous health checks with heartbeats
- **Dashboard**: Web UI at `http://localhost:8761`
- **Load Balancing**: Client-side load balancing support
- **Self-Preservation**: Handles network partition scenarios

### 👤 Profile Service
**Port**: 8081 | **Database**: MySQL + Redis Cache | **Tech**: Spring Boot + JPA + Keycloak Admin Client

Complete user profile and account management:

**Features**:
- **User Management**: Direct Keycloak integration for user CRUD operations
- **Profile Data**: Complete profile information with image upload support
- **Caching**: Redis caching for profile data by ID and username
- **Events**: RabbitMQ events for profile updates

**Database Schema (MySQL)**:
```sql
TABLE profiles {
  id BIGINT PRIMARY KEY AUTO_INCREMENT
  userId VARCHAR(255) UNIQUE NOT NULL  # Keycloak user ID
  username VARCHAR(255) UNIQUE
  email VARCHAR(255)
  displayName VARCHAR(255)
  bio TEXT
  location VARCHAR(255)
  websiteUrl VARCHAR(2048)
  profileImageUrl VARCHAR(2048)
  bannerImageUrl VARCHAR(2048)
  dateOfBirth DATE
  joinDate DATE
  gender BOOLEAN
}
```

**Key Endpoints**:
- `GET /api/v1/profiles/i/{userId}` - Get profile by user ID
- `GET /api/v1/profiles/u/{username}` - Get profile by username
- `PATCH /api/v1/profiles/{userId}` - Update profile with media upload
- `PATCH /api/v1/profiles/{userId}/username` - Update username
- `DELETE /api/v1/profiles/{userId}` - Delete profile and Keycloak user

### 🐦 Tweet Service
**Port**: 8083 | **Database**: MongoDB | **Tech**: Spring Boot + MongoDB + Media Upload

Core content creation and engagement system:

**Features**:
- **Tweet CRUD**: Create, read, update, delete tweets with media support
- **Engagement**: Like/unlike functionality with real-time stats
- **Media Support**: Multipart file upload integration
- **Hashtags**: Automatic hashtag extraction from content
- **Visibility**: Public, private, followers-only tweet visibility
- **Events**: RabbitMQ events for timeline and search indexing

**Document Schema (MongoDB)**:
```javascript
{
  _id: ObjectId,
  userId: String,
  content: String,
  mediaItems: [{
    mediaId: String,
    mediaType: String,
    mediaUrl: String
  }],
  hashtags: [String],
  stats: { likesCount: Number },
  likedBy: [String],
  visibility: "PUBLIC" | "PRIVATE" | "FOLLOWERS_ONLY",
  createdAt: Long,
  updatedAt: Long
}
```

**Key Endpoints**:
- `POST /api/v1/tweets` - Create tweet with media upload
- `GET /api/v1/tweets/{id}` - Get tweet by ID
- `GET /api/v1/tweets/me` - Get current user's tweets
- `POST /api/v1/tweets/{id}/likes` - Like tweet
- `DELETE /api/v1/tweets/{id}/likes` - Unlike tweet
- `GET /api/v1/tweets/batch?ids=[]` - Batch get tweets

### 💬 Comment Service
**Port**: 8085 | **Database**: MongoDB | **Tech**: Spring Boot + MongoDB

Hierarchical commenting system with nested replies:

**Features**:
- **Comments & Replies**: Two-level comment hierarchy (comments → replies)
- **Thread Management**: Organized comment threads per tweet
- **Event Publishing**: RabbitMQ events for notifications

**Document Schema (MongoDB)**:
```javascript
{
  _id: ObjectId,
  tweetId: String,
  userId: String,
  content: String,
  parentId: String,  // For replies
  type: "COMMENT" | "REPLY",
  createdAt: Long,
  updatedAt: Long
}
```

**Key Endpoints**:
- `POST /api/v1/tweets/{tweetId}/comments` - Create comment
- `POST /api/v1/comments/{parentId}/replies` - Reply to comment
- `GET /api/v1/tweets/{tweetId}/comments` - Get tweet comments
- `GET /api/v1/comments/{commentId}/replies` - Get comment replies

### 👥 Follow Service
**Port**: 8086 | **Database**: MongoDB | **Tech**: Spring Boot + MongoDB

Social relationship management system:

**Features**:
- **Follow/Unfollow**: Simple follow relationship management
- **Relationship Queries**: Check follow status, get followers/following lists
- **Event-Driven**: Publishes follow events for timeline updates

**Document Schema (MongoDB)**:
```javascript
{
  _id: ObjectId,
  followerId: String,    // User who follows
  followedId: String,    // User being followed
  createdAt: Long
}
```

**Key Endpoints**:
- `POST /api/v1/follows/{followedId}` - Follow user
- `DELETE /api/v1/follows/{followedId}` - Unfollow user
- `GET /api/v1/follows/{followerId}` - Check if following
- `GET /api/v1/follows/{followedId}/followers` - Get followers
- `GET /api/v1/follows/{followerId}/following` - Get following list

### 📰 Timeline Service
**Port**: 8087 | **Database**: MongoDB | **Tech**: Spring Boot + MongoDB + Feign Clients

Personalized feed generation and aggregation:

**Features**:
- **Home Timeline**: Aggregated tweets from followed users
- **Event-Driven Updates**: Real-time timeline updates via RabbitMQ
- **Data Enrichment**: Feign clients to fetch tweet and profile data
- **Timeline Storage**: Pre-computed timeline entries for performance

**Document Schema (MongoDB)**:
```javascript
{
  _id: ObjectId,
  userId: String,        // Timeline owner
  tweetId: String,       // Referenced tweet
  tweetOwnerId: String,  // Tweet author
  tweetAt: Long,         // Tweet creation timestamp
  createdAt: Long        // Timeline entry creation
}
```

**Key Endpoints**:
- `GET /api/v1/timeline` - Get personalized timeline

**Event Handlers**:
- Tweet creation → Add to followers' timelines
- Follow event → Backfill timeline with followee's tweets

### 📸 Media Service
**Port**: 8082 | **Database**: Azure Blob Storage | **Tech**: Spring Boot + Azure SDK

Media file upload and storage management:

**Features**:
- **Azure Blob Integration**: Direct upload to Azure Blob Storage
- **File Type Support**: Images and other media file groups
- **Content Validation**: File type and size validation
- **Secure Upload**: OAuth2 protected endpoints

**Key Endpoints**:
- `POST /api/v1/media` - Upload media files (multipart)

**Supported File Types**:
- **Images**: JPEG, PNG, GIF, WebP
- **Videos**: MP4, MOV, AVI
- **Documents**: PDF, DOC, DOCX (configurable)

**Azure Configuration**:
```yaml
azure:
  storage:
    connection-string: "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;..."
    container-name: "twitter"
```

### 🔔 Notification Service
**Port**: 8084 | **Database**: MongoDB | **Tech**: Spring Boot + MongoDB + FCM

Real-time notification system with push notification support:

**Features**:
- **Notification Management**: Create, read, delete notifications
- **Push Notifications**: Firebase Cloud Messaging integration
- **Event-Driven**: RabbitMQ listeners for automatic notification creation
- **Bulk Operations**: Mark all as read, bulk delete

**Document Schemas (MongoDB)**:
```javascript
// Notifications
{
  _id: ObjectId,
  userId: String,
  senderId: String,
  type: "LIKE" | "COMMENT" | "FOLLOW" | "TWEET",
  message: String,
  read: Boolean,
  createdAt: String
}

// FCM Tokens
{
  _id: ObjectId,
  userId: String,
  token: String,
  device: String
}
```

**Key Endpoints**:
- `GET /api/v1/notifications` - Get user notifications
- `GET /api/v1/notifications/unread/count` - Get unread count
- `PATCH /api/v1/notifications/{id}/read` - Mark as read
- `PATCH /api/v1/notifications/read-all` - Mark all as read
- `DELETE /api/v1/notifications` - Bulk delete

**Event Handlers**:
- Tweet liked → Notification to tweet author
- Comment created → Notification to tweet author
- User followed → Notification to followed user

### 🔍 Search Service
**Port**: 8089 | **Database**: Elasticsearch | **Tech**: Spring Boot + Elasticsearch + Redis Cache

Full-text search across tweets and users with real-time indexing:

**Features**:
- **Real-time Indexing**: RabbitMQ event listeners for automatic ES indexing
- **Full-text Search**: Advanced search capabilities across tweets and users
- **User Context**: Search respects user permissions and visibility
- **Caching**: Redis caching for search results

**Elasticsearch Indices**:
```javascript
// tweets index
{
  id: String,
  tweetId: String,
  userId: String,
  content: String,
  mediaItems: [...],
  hashtags: [String],
  visibility: Enum,
  createdAt: Long
}

// users index
{
  id: String,
  userId: String,
  username: String,
  displayName: String,
  bio: String,
  location: String
}
```

**Key Endpoints**:
- `GET /api/v1/search/tweets?q={query}` - Search tweets
- `GET /api/v1/search/users?q={query}` - Search users

**Event Handlers**:
- Tweet created/updated → Index in Elasticsearch
- Profile updated → Update user index
- Tweet deleted → Remove from index

### 📚 Common Library
Shared components and utilities across all services:

**Components**:
- **DTOs**: Standardized data transfer objects
- **Exceptions**: Common exception handling patterns
- **Events**: RabbitMQ event definitions
- **Security**: OAuth2 security configurations
- **Utilities**: Helper classes and validation utilities
- **Messaging**: RabbitMQ configuration and listeners

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

