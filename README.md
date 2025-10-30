# Task Management & Notification System

A scalable, enterprise-grade task management platform built with Java and Spring Boot, featuring dual-channel notifications (real-time and scheduled), file management, and comprehensive audit logging.

## 🚀 Features

### Core Functionality
- **JWT Authentication**: Secure token-based authentication with role-based access control (RBAC)
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
- **Security**: Spring Security with JWT (JJWT 0.12.3) and BCrypt
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

## 🔐 Authentication

This application uses **JWT (JSON Web Token)** based authentication with role-based access control.

### Quick Start

1. **Register a new user:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "admin",
       "email": "admin@example.com",
       "password": "admin123",
       "role": "ADMIN"
     }'
   ```

2. **Copy the JWT token** from the response

3. **Use the token** in subsequent requests:
   ```bash
   curl -X GET http://localhost:8080/api/users \
     -H "Authorization: Bearer <your-token>"
   ```

### User Roles

- **ADMIN**: Full access to all endpoints
- **MANAGER**: Can manage tasks and view users
- **USER**: Can view tasks and manage own data

📖 **For detailed authentication guide, see [AUTHENTICATION.md](AUTHENTICATION.md)**

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Using Swagger UI with Authentication

1. Open Swagger UI at http://localhost:8080/swagger-ui/index.html
2. Register or login via `/api/auth/register` or `/api/auth/login`
3. Copy the JWT token from the response
4. Click the **🔓 Authorize** button at the top right
5. Paste your token and click **Authorize**
6. Now you can test all protected endpoints

## 🔌 API Endpoints

### Authentication (Public)
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login with credentials

### User Management (Protected)
- `POST /api/users` - Create a new user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Task Management (Protected)
- `POST /api/tasks` - Create a new task (ADMIN, MANAGER)
- `GET /api/tasks` - Get all tasks (ADMIN, MANAGER, USER)
- `GET /api/tasks/{id}` - Get task by ID (ADMIN, MANAGER, USER)
- `POST /api/tasks/filter` - Get filtered tasks (ADMIN, MANAGER, USER)
- `PUT /api/tasks/{id}` - Update task (ADMIN, MANAGER)
- `DELETE /api/tasks/{id}` - Delete task (ADMIN only)
- `POST /api/tasks/import/csv` - Bulk import tasks from CSV (ADMIN, MANAGER)

### File Management (Protected)
- `POST /api/files/upload` - Upload file attachment (ADMIN, MANAGER)
- `GET /api/files/{id}` - Get file details (ADMIN, MANAGER, USER)
- `GET /api/files/task/{taskId}` - Get all files for a task (ADMIN, MANAGER, USER)
- `GET /api/files/{id}/download-url` - Get pre-signed download URL (ADMIN, MANAGER, USER)
- `DELETE /api/files/{id}` - Delete file (ADMIN only)

### Audit Logs (Protected)
- `GET /api/audit-logs` - Get all audit logs (ADMIN only)
- `GET /api/audit-logs/entity/{entityType}/{entityId}` - Get logs for specific entity (ADMIN only)
- `GET /api/audit-logs/user/{username}` - Get logs by user (ADMIN only)

## 📝 Usage Examples

### 1. Register and Authenticate

```bash
# Register a new admin user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "role": "ADMIN"
  }'

# Save the token from the response
TOKEN="<your-jwt-token-here>"
```

### 2. Create a User (Requires ADMIN role)
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
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

### 3. Create a Task (Requires ADMIN or MANAGER role)
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
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

### 4. Filter Tasks (All authenticated users)
```bash
curl -X POST http://localhost:8080/api/tasks/filter \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "TODO",
    "priority": "HIGH",
    "assigneeId": 1
  }'
```

### 5. Bulk Import Tasks from CSV (Requires ADMIN or MANAGER role)
```bash
curl -X POST http://localhost:8080/api/tasks/import/csv \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@sample-tasks.csv"
```

### 6. Upload File Attachment (Requires ADMIN or MANAGER role)
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer $TOKEN" \
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

1. **Security**
   - Change JWT secret key (use environment variable)
   - Update JWT token expiration as needed
   - Enable HTTPS/TLS
   - Review and update CORS settings
   - Implement rate limiting

2. **Infrastructure**
   - Configure external PostgreSQL and Kafka
   - Set up AWS S3 for file storage
   - Configure email SMTP settings
   - Set up monitoring and logging
   - Configure backup strategies

3. **Configuration**
   - Update `application.yml` for production settings
   - Use environment variables for sensitive data
   - Configure proper logging levels

### Build Production Image

```bash
docker build -t taskmanagement:latest .
```

## 📦 Project Structure

```
src/
├── main/
│   ├── java/com/taskmanagement/
│   │   ├── config/          # Configuration classes (Security, OpenAPI, etc.)
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── event/           # Event classes
│   │   ├── exception/       # Custom exceptions
│   │   ├── model/           # Entity classes
│   │   ├── repository/      # JPA repositories
│   │   ├── security/        # JWT authentication & filters
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

## 🎯 Roadmap

### ✅ Completed
- [x] JWT-based authentication with RBAC
- [x] User and task CRUD operations
- [x] CSV bulk import
- [x] File management (local storage)
- [x] Audit logging
- [x] Swagger API documentation
- [x] Docker containerization

### 🔄 In Progress
- [ ] Email notifications (SMTP configuration needed)
- [ ] AWS S3 integration (credentials needed)

### 📋 Planned
- [ ] Refresh token mechanism
- [ ] Password reset functionality
- [ ] Email verification
- [ ] WebSocket support for real-time updates
- [ ] Advanced reporting and analytics
- [ ] Mobile app integration
- [ ] Multi-tenancy support
- [ ] Advanced workflow automation
- [ ] Integration with third-party tools (Jira, Slack, etc.)

## 📚 Documentation

- **[AUTHENTICATION.md](AUTHENTICATION.md)** - Comprehensive JWT authentication guide
- **[README.md](README.md)** - This file (project overview)
- **Swagger UI** - Interactive API documentation at http://localhost:8080/swagger-ui/index.html

