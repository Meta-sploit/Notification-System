# System Architecture

## Overview

The Task Management & Notification System is built using a clean, layered architecture with event-driven components for scalability and maintainability.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                            │
│  (Web Browser, Mobile App, API Clients, Postman, etc.)         │
└─────────────────────────────────────────────────────────────────┘
                              ↓ HTTP/REST
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway / Load Balancer                │
│                         (Future Enhancement)                     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                      │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │              Controller Layer (REST API)                  │ │
│  │  - UserController                                         │ │
│  │  - TaskController                                         │ │
│  │  - FileController                                         │ │
│  │  - AuditLogController                                     │ │
│  └───────────────────────────────────────────────────────────┘ │
│                              ↓                                  │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                   Service Layer                           │ │
│  │  - UserService                                            │ │
│  │  - TaskService                                            │ │
│  │  - FileStorageService                                     │ │
│  │  - NotificationService                                    │ │
│  │  - AuditLogService                                        │ │
│  │  - CsvImportService                                       │ │
│  │  - ScheduledNotificationService                           │ │
│  └───────────────────────────────────────────────────────────┘ │
│                              ↓                                  │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                 Repository Layer                          │ │
│  │  - UserRepository (JPA)                                   │ │
│  │  - TaskRepository (JPA + Specifications)                  │ │
│  │  - FileAttachmentRepository (JPA)                         │ │
│  │  - AuditLogRepository (JPA)                               │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      Data & Integration Layer                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │  PostgreSQL  │  │ Apache Kafka │  │   AWS S3     │         │
│  │   Database   │  │   Messaging  │  │ File Storage │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└─────────────────────────────────────────────────────────────────┘
```

## Component Architecture

### 1. Controller Layer
**Responsibility**: Handle HTTP requests and responses

- **UserController**: User CRUD operations
- **TaskController**: Task management and filtering
- **FileController**: File upload/download operations
- **AuditLogController**: Audit log retrieval

**Key Features**:
- Request validation using Jakarta Validation
- Exception handling via @RestControllerAdvice
- Swagger/OpenAPI documentation
- RESTful design principles

### 2. Service Layer
**Responsibility**: Business logic and orchestration

#### UserService
- User creation with password encryption
- User management (CRUD)
- Duplicate validation
- Audit logging integration

#### TaskService
- Task lifecycle management
- Advanced filtering using JPA Specifications
- Event publishing for notifications
- Status change tracking

#### FileStorageService
- Multi-storage support (S3/Local)
- Pre-signed URL generation
- File metadata management
- Secure file operations

#### NotificationService
- Multi-channel notification delivery
- Email sending via JavaMailSender
- Kafka message publishing
- Asynchronous processing

#### AuditLogService
- Comprehensive audit trail
- Asynchronous logging
- Query capabilities by entity/user

#### CsvImportService
- Bulk task import from CSV
- Batch processing
- Error handling and validation
- Progress tracking

#### ScheduledNotificationService
- Cron-based task reminders
- Configurable reminder timing
- Automatic reminder tracking

### 3. Repository Layer
**Responsibility**: Data access and persistence

- **JPA Repositories**: Standard CRUD operations
- **Custom Queries**: Complex filtering and search
- **Specifications**: Dynamic query building
- **Indexing**: Optimized database queries

### 4. Event-Driven Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Event Flow                               │
└─────────────────────────────────────────────────────────────┘

Task Created/Updated
        ↓
TaskService publishes TaskEvent
        ↓
ApplicationEventPublisher
        ↓
TaskEventListener (Async)
        ↓
NotificationService
        ↓
    ┌───────────────┬───────────────┐
    ↓               ↓               ↓
Email Sender   Kafka Producer   Database Log
```

**Event Types**:
- CREATED
- UPDATED
- STATUS_CHANGED
- ASSIGNED
- DELETED
- REMINDER

### 5. Notification System

#### Real-time Notifications
```
Trigger: Task Assignment or Status Change
    ↓
Spring Event Published
    ↓
TaskEventListener
    ↓
NotificationMessage Created
    ↓
    ┌─────────────────┬─────────────────┐
    ↓                 ↓                 ↓
Email (SMTP)    Kafka Topic      Audit Log
```

#### Scheduled Notifications
```
Cron Job (Every Hour)
    ↓
ScheduledNotificationService
    ↓
Query Tasks Due Soon
    ↓
For Each Task:
    ↓
Publish REMINDER Event
    ↓
TaskEventListener
    ↓
Send Notification
    ↓
Mark Reminder Sent
```

## Data Model

### Entity Relationships

```
User (1) ──────── (N) Task [as assignee]
User (1) ──────── (N) Task [as creator]
Task (1) ──────── (N) FileAttachment
Task (1) ──────── (N) AuditLog [indirect]
User (1) ──────── (N) AuditLog [indirect]
```

### Core Entities

#### User
- id (PK)
- username (unique)
- email (unique)
- password (encrypted)
- firstName, lastName
- phoneNumber
- role (ADMIN, MANAGER, USER)
- active (boolean)
- timestamps

#### Task
- id (PK)
- title
- description
- status (TODO, IN_PROGRESS, IN_REVIEW, COMPLETED, CANCELLED)
- priority (LOW, MEDIUM, HIGH, CRITICAL)
- dueDate
- assignee_id (FK → User)
- createdBy_id (FK → User)
- reminderSent (boolean)
- estimatedHours, actualHours
- tags
- timestamps

#### FileAttachment
- id (PK)
- fileName
- fileKey (S3 key or local path)
- contentType
- fileSize
- task_id (FK → Task)
- uploadedBy_id (FK → User)
- uploadedAt

#### AuditLog
- id (PK)
- entityType (TASK, USER, FILE)
- entityId
- action (CREATE, UPDATE, DELETE, STATUS_CHANGE, etc.)
- performedBy
- oldValue, newValue
- details
- ipAddress
- timestamp

## Security Architecture

### Authentication & Authorization
- Spring Security integration
- BCrypt password hashing
- HTTP Basic Authentication (current)
- JWT support (future enhancement)

### Data Security
- SQL injection prevention via JPA
- Input validation
- XSS protection
- CSRF protection (configurable)

### File Security
- Pre-signed URLs with expiration
- Access control via task ownership
- Secure file storage (S3 encryption)

## Scalability Considerations

### Horizontal Scaling
- Stateless application design
- Database connection pooling
- Kafka for distributed messaging
- Load balancer ready

### Performance Optimization
- Database indexing on frequently queried fields
- Lazy loading for entity relationships
- Asynchronous processing (@Async)
- Batch processing for bulk operations

### Caching Strategy (Future)
- Redis for session management
- Cache frequently accessed data
- Cache invalidation on updates

## Deployment Architecture

### Docker Compose Setup
```
┌─────────────────────────────────────────────────────────┐
│                    Docker Network                       │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  PostgreSQL  │  │  Zookeeper   │  │ Spring Boot  │ │
│  │  Container   │  │  Container   │  │  Container   │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │    Kafka     │  │   pgAdmin    │  │  Kafka UI    │ │
│  │  Container   │  │  Container   │  │  Container   │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Production Deployment (Recommended)
```
┌─────────────────────────────────────────────────────────┐
│                    Load Balancer                        │
│                   (AWS ALB / NGINX)                     │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│              Application Cluster                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │ App Pod 1│  │ App Pod 2│  │ App Pod 3│             │
│  └──────────┘  └──────────┘  └──────────┘             │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│                  Managed Services                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   RDS        │  │  MSK/Kafka   │  │     S3       │ │
│  │ (PostgreSQL) │  │  (Managed)   │  │  (Storage)   │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## Monitoring & Observability

### Metrics
- Spring Boot Actuator endpoints
- JVM metrics
- Database connection pool metrics
- Custom business metrics

### Logging
- Structured logging (SLF4J + Logback)
- Log levels: DEBUG, INFO, WARN, ERROR
- File-based logging with rotation
- Centralized logging (future: ELK stack)

### Health Checks
- Application health endpoint
- Database connectivity check
- Kafka connectivity check
- Custom health indicators

## API Design Principles

1. **RESTful**: Standard HTTP methods and status codes
2. **Resource-based**: URLs represent resources
3. **Stateless**: No server-side session state
4. **Versioned**: API versioning support
5. **Documented**: Swagger/OpenAPI documentation
6. **Validated**: Input validation at controller level
7. **Error Handling**: Consistent error response format

## Technology Choices Rationale

### Spring Boot
- Rapid development
- Production-ready features
- Large ecosystem
- Enterprise support

### PostgreSQL
- ACID compliance
- Advanced features (JSON, full-text search)
- Scalability
- Open source

### Apache Kafka
- High throughput messaging
- Event streaming
- Scalability
- Durability

### AWS S3
- Scalable object storage
- High availability
- Cost-effective
- Pre-signed URL support

## Future Enhancements

1. **Microservices**: Split into separate services
2. **API Gateway**: Centralized routing and authentication
3. **Service Mesh**: Istio for service-to-service communication
4. **Caching**: Redis for performance
5. **Search**: Elasticsearch for advanced search
6. **Monitoring**: Prometheus + Grafana
7. **Tracing**: Distributed tracing with Jaeger
8. **CI/CD**: Automated deployment pipeline

