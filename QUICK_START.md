# Quick Start Guide

Get up and running with the Task Management & Notification System in 5 minutes!

---

## 🚀 Step 1: Start the Application

```bash
# Clone the repository (if not already done)
git clone <repository-url>
cd Notification\ System

# Start all services with Docker Compose
docker compose up -d

# Wait for services to be healthy (about 15-20 seconds)
sleep 20

# Verify application is running
curl http://localhost:8080/actuator/health
```

**Expected output:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

---

## 🔐 Step 2: Create Your First User

```bash
# Register an admin user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "role": "ADMIN"
  }' | jq .
```

**Expected output:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "email": "admin@example.com",
  "role": "ADMIN",
  "message": "User registered successfully"
}
```

**💡 Copy the token value** - you'll need it for the next steps!

---

## 📝 Step 3: Create Your First Task

```bash
# Set your token as an environment variable
export TOKEN="<paste-your-token-here>"

# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Task",
    "description": "Testing the task management system",
    "status": "PENDING",
    "priority": "HIGH",
    "dueDate": "2024-12-31T23:59:59"
  }' | jq .
```

**Expected output:**
```json
{
  "id": 1,
  "title": "My First Task",
  "description": "Testing the task management system",
  "status": "PENDING",
  "priority": "HIGH",
  "dueDate": "2024-12-31T23:59:59",
  "createdAt": "2024-10-30T...",
  "updatedAt": "2024-10-30T..."
}
```

---

## 📊 Step 4: View Your Tasks

```bash
# Get all tasks
curl -X GET http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" | jq .
```

---

## 🌐 Step 5: Explore with Swagger UI

1. **Open Swagger UI** in your browser:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

2. **Authorize with your token:**
   - Click the **🔓 Authorize** button (top right)
   - Paste your token in the "Value" field
   - Click **Authorize**, then **Close**

3. **Try the APIs:**
   - Expand any endpoint
   - Click **"Try it out"**
   - Fill in the parameters
   - Click **"Execute"**

---

## 🎯 Common Tasks

### Create Additional Users

```bash
# Create a manager user
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "manager1",
    "email": "manager@example.com",
    "password": "manager123",
    "firstName": "Jane",
    "lastName": "Manager",
    "role": "MANAGER"
  }' | jq .

# Create a regular user
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "email": "user@example.com",
    "password": "user123",
    "firstName": "John",
    "lastName": "User",
    "role": "USER"
  }' | jq .
```

### Filter Tasks

```bash
# Get all high-priority pending tasks
curl -X POST http://localhost:8080/api/tasks/filter \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PENDING",
    "priority": "HIGH"
  }' | jq .
```

### Update a Task

```bash
# Update task status to IN_PROGRESS
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Task",
    "description": "Testing the task management system",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "dueDate": "2024-12-31T23:59:59"
  }' | jq .
```

### Upload a File

```bash
# Upload a file attachment to a task
curl -X POST "http://localhost:8080/api/files/upload?taskId=1" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/your/file.pdf" | jq .
```

### Bulk Import Tasks from CSV

```bash
# Create a sample CSV file
cat > tasks.csv << 'EOF'
title,description,status,priority,dueDate
Task 1,Description 1,PENDING,HIGH,2024-12-31T23:59:59
Task 2,Description 2,IN_PROGRESS,MEDIUM,2024-12-25T23:59:59
Task 3,Description 3,PENDING,LOW,2024-12-20T23:59:59
EOF

# Import the tasks
curl -X POST http://localhost:8080/api/tasks/import/csv \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@tasks.csv" | jq .
```

---

## 🔍 Monitoring & Management

### View Application Logs

```bash
# View real-time logs
docker compose logs -f app

# View last 50 lines
docker compose logs app --tail=50
```

### Access Database (pgAdmin)

1. Open pgAdmin: http://localhost:5050
2. Login:
   - Email: `admin@admin.com`
   - Password: `admin`
3. Add server:
   - Host: `postgres`
   - Port: `5432`
   - Database: `taskmanagement`
   - Username: `postgres`
   - Password: `postgres`

### Access Kafka UI

1. Open Kafka UI: http://localhost:8090
2. View topics, messages, and consumer groups

---

## 🛑 Stop the Application

```bash
# Stop all services
docker compose down

# Stop and remove volumes (⚠️ deletes all data)
docker compose down -v
```

---

## 🔧 Troubleshooting

### Port Already in Use

If port 8080 is already in use, change it in `docker-compose.yml`:

```yaml
services:
  app:
    ports:
      - "8081:8080"  # Change 8080 to 8081
```

### Token Expired

If you get a 403 error, your token may have expired (default: 24 hours). Login again:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }' | jq -r '.token'
```

### Application Not Starting

Check the logs:

```bash
docker compose logs app --tail=100
```

Common issues:
- Database not ready: Wait a few more seconds
- Port conflict: Change the port mapping
- Build error: Run `docker compose up -d --build app`

---

## 📚 Next Steps

- **Read the full authentication guide:** [AUTHENTICATION.md](AUTHENTICATION.md)
- **Explore the API documentation:** http://localhost:8080/swagger-ui/index.html
- **Check the main README:** [README.md](README.md)

---

## 🎓 Role-Based Access Quick Reference

| Action | ADMIN | MANAGER | USER |
|--------|-------|---------|------|
| View tasks | ✅ | ✅ | ✅ |
| Create tasks | ✅ | ✅ | ❌ |
| Update tasks | ✅ | ✅ | ❌ |
| Delete tasks | ✅ | ❌ | ❌ |
| View users | ✅ | ✅ | ❌ |
| Create users | ✅ | ❌ | ❌ |
| Update users | ✅ | ❌ | ❌ |
| Delete users | ✅ | ❌ | ❌ |
| Upload files | ✅ | ✅ | ❌ |
| Download files | ✅ | ✅ | ✅ |
| Delete files | ✅ | ❌ | ❌ |
| View audit logs | ✅ | ❌ | ❌ |

---

**🎉 You're all set! Happy task managing!**

