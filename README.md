# Task Management & Notification System

A scalable, enterprise-grade task management platform built with Java and Spring Boot, featuring dual-channel notifications (real-time and scheduled), file management, and comprehensive audit logging.

## 🚀 Features

### Core Functionality
- **Full CRUD Operations**: Complete REST API for tasks and users
- **Advanced Filtering**: Filter tasks by status, priority, assignee, due date, and search terms
- **Bulk Import**: CSV-based bulk task creation with validation
- **File Management**: Upload/download file attachments with pre-signed URLs (S3 or local storage)
- **Audit Logging**: Comprehensive tracking of all significant actions

### Notification System
- **Real-time Notifications**: Instant notifications on task assignment and status changes
- **Scheduled Reminders**: Automated reminders before task due dates (configurable)
- **Multi-channel Delivery**: Email and Kafka-based event streaming

### Architecture
- **Clean Architecture**: Layered design (Controller → Service → Repository)
- **Event-Driven**: Spring Events and Kafka for asynchronous processing
- **Cloud-Native**: Docker containerization with docker-compose orchestration
- **API Documentation**: Interactive Swagger/OpenAPI documentation

## 🏗️ Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Messaging**: Apache Kafka
- **File Storage**: AWS S3 / Local filesystem
- **Security**: Spring Security with BCrypt
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose (for containerized deployment)
- PostgreSQL 15+ (if running locally without Docker)
- Apache Kafka (if running locally without Docker)

## 🛠️ Installation & Setup

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd "Notification System"
   ```

2. **Build and start all services**
   ```bash
   docker-compose up -d
   ```

   This will start:
   - PostgreSQL database (port 5432)
   - Apache Kafka & Zookeeper (port 9092)
   - Spring Boot application (port 8080)
   - pgAdmin (port 5050) - Database management UI
   - Kafka UI (port 8090) - Kafka management UI

3. **Verify services are running**
   ```bash
   docker-compose ps
   ```

4. **View application logs**
   ```bash
   docker-compose logs -f app
   ```

### Option 2: Local Development

1. **Start PostgreSQL**
   ```bash
   # Using Docker
   docker run -d --name postgres \
     -e POSTGRES_DB=taskmanagement \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:15-alpine
   ```

2. **Start Kafka**
   ```bash
   # Using Docker Compose for Kafka only
   docker-compose up -d zookeeper kafka
   ```

3. **Configure application**
   - Update `src/main/resources/application.yml` with your settings
   - Or create `application-local.yml` for local overrides

4. **Build and run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🔌 API Endpoints

### User Management
- `POST /api/users` - Create a new user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Task Management
- `POST /api/tasks` - Create a new task
- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/tasks/filter` - Get filtered tasks
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task
- `POST /api/tasks/import/csv` - Bulk import tasks from CSV

### File Management
- `POST /api/files/upload` - Upload file attachment
- `GET /api/files/{id}` - Get file details
- `GET /api/files/task/{taskId}` - Get all files for a task
- `GET /api/files/{id}/download-url` - Get pre-signed download URL
- `DELETE /api/files/{id}` - Delete file

### Audit Logs
- `GET /api/audit-logs` - Get all audit logs
- `GET /api/audit-logs/entity/{entityType}/{entityId}` - Get logs for specific entity
- `GET /api/audit-logs/user/{username}` - Get logs by user

## 📝 Usage Examples

### Create a User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }'
```

### Create a Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implement new feature",
    "description": "Add user authentication",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2025-11-15T10:00:00",
    "assigneeId": 1,
    "estimatedHours": 8
  }'
```

### Filter Tasks
```bash
curl -X POST http://localhost:8080/api/tasks/filter \
  -H "Content-Type: application/json" \
  -d '{
    "status": "TODO",
    "priority": "HIGH",
    "assigneeId": 1
  }'
```

### Bulk Import Tasks from CSV
```bash
curl -X POST http://localhost:8080/api/tasks/import/csv \
  -F "file=@sample-tasks.csv"
```

### Upload File Attachment
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@document.pdf" \
  -F "taskId=1"
```

## 🔧 Configuration

### Application Properties

Key configuration options in `application.yml`:

```yaml
# Database
spring.datasource.url: jdbc:postgresql://localhost:5432/taskmanagement
spring.datasource.username: postgres
spring.datasource.password: postgres

# Kafka
spring.kafka.bootstrap-servers: localhost:9092

# File Storage
app.storage.type: S3  # or LOCAL
app.storage.s3.bucket-name: your-bucket-name
app.storage.s3.region: us-east-1

# Notifications
app.notification.enabled: true
app.notification.reminder.hours-before-due: 24
app.notification.reminder.cron: "0 0 * * * *"  # Every hour

# Email
spring.mail.host: smtp.gmail.com
spring.mail.username: your-email@gmail.com
spring.mail.password: your-password
```

### Environment Variables

For Docker deployment, set these environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `AWS_ACCESS_KEY` (for S3)
- `AWS_SECRET_KEY` (for S3)
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

## 🏛️ Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────────┐
│          Controller Layer               │
│  (REST API, Request/Response Handling)  │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│           Service Layer                 │
│    (Business Logic, Validation)         │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Repository Layer                │
│   (Data Access, JPA Repositories)       │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│            Database                     │
│          (PostgreSQL)                   │
└─────────────────────────────────────────┘
```

### Event-Driven Notification Flow

```
Task Event (Assignment/Status Change)
            ↓
    Spring Event Publisher
            ↓
    TaskEventListener
            ↓
    NotificationService
            ↓
    ┌───────────────┬───────────────┐
    ↓               ↓               ↓
  Email         Kafka Topic      Database
```

## 📊 Database Schema

### Main Entities

- **users**: User accounts with roles and authentication
- **tasks**: Task details with status, priority, and assignments
- **file_attachments**: File metadata and storage references
- **audit_logs**: Comprehensive audit trail

### Relationships

- User → Tasks (One-to-Many: created tasks)
- User → Tasks (One-to-Many: assigned tasks)
- Task → FileAttachments (One-to-Many)

## 🔐 Security

- **Password Encryption**: BCrypt hashing
- **API Security**: Spring Security with HTTP Basic (can be extended to JWT)
- **CSRF Protection**: Disabled for REST API (enable for web applications)
- **Input Validation**: Jakarta Validation annotations
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries

## 📈 Monitoring & Health

- **Actuator Endpoints**: http://localhost:8080/actuator
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics

## 🧪 Testing

Run tests with:
```bash
mvn test
```

## 🚀 Deployment

### Production Checklist

1. Update `application.yml` for production settings
2. Configure external PostgreSQL and Kafka
3. Set up AWS S3 for file storage
4. Configure email SMTP settings
5. Enable HTTPS/TLS
6. Set up monitoring and logging
7. Configure backup strategies
8. Review security settings

### Build Production Image

```bash
docker build -t taskmanagement:latest .
```

## 📦 Project Structure

```
src/
├── main/
│   ├── java/com/taskmanagement/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── event/           # Event classes
│   │   ├── exception/       # Custom exceptions
│   │   ├── model/           # Entity classes
│   │   ├── repository/      # JPA repositories
│   │   └── service/         # Business logic
│   └── resources/
│       └── application.yml  # Application configuration
└── test/                    # Test classes
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the Apache License 2.0.

## 📞 Support

For issues and questions:
- Create an issue in the repository
- Email: support@taskmanagement.com

## 🎯 Future Enhancements

- [ ] JWT-based authentication
- [ ] WebSocket support for real-time updates
- [ ] Advanced reporting and analytics
- [ ] Mobile app integration
- [ ] Multi-tenancy support
- [ ] Advanced workflow automation
- [ ] Integration with third-party tools (Jira, Slack, etc.)

