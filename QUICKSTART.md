# Quick Start Guide

Get the Task Management & Notification System up and running in 5 minutes!

## Prerequisites

- Docker and Docker Compose installed
- 8GB RAM minimum
- Ports available: 8080, 5432, 9092, 5050, 8090

## Step 1: Start the System

```bash
# Clone the repository (if not already done)
cd "Notification System"

# Start all services
docker-compose up -d

# Wait for services to be healthy (about 30-60 seconds)
docker-compose ps
```

You should see all services running:
- ✅ taskmanagement-postgres
- ✅ taskmanagement-zookeeper
- ✅ taskmanagement-kafka
- ✅ taskmanagement-app
- ✅ taskmanagement-pgadmin
- ✅ taskmanagement-kafka-ui

## Step 2: Verify the Application

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

## Step 3: Access the Interfaces

### Swagger UI (API Documentation)
Open in browser: http://localhost:8080/swagger-ui.html

### pgAdmin (Database Management)
- URL: http://localhost:5050
- Email: admin@taskmanagement.com
- Password: admin

### Kafka UI (Message Queue Management)
Open in browser: http://localhost:8090

## Step 4: Create Your First User

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "password123",
    "firstName": "Alice",
    "lastName": "Smith",
    "role": "USER"
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "username": "alice",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Smith",
  "role": "USER",
  "active": true
}
```

## Step 5: Create Your First Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project documentation",
    "description": "Write comprehensive documentation for the project",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2025-11-15T17:00:00",
    "assigneeId": 1,
    "estimatedHours": 4,
    "tags": "documentation,important"
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "title": "Complete project documentation",
  "status": "TODO",
  "priority": "HIGH",
  "assigneeId": 1,
  "assigneeName": "alice",
  "reminderSent": false
}
```

## Step 6: Try Bulk Import

```bash
# Import tasks from the sample CSV file
curl -X POST http://localhost:8080/api/tasks/import/csv \
  -F "file=@sample-tasks.csv"
```

This will import 10 sample tasks from the provided CSV file.

## Step 7: Filter Tasks

```bash
# Get all HIGH priority tasks
curl -X POST http://localhost:8080/api/tasks/filter \
  -H "Content-Type: application/json" \
  -d '{
    "priority": "HIGH",
    "status": "TODO"
  }'
```

## Step 8: Upload a File

```bash
# Create a sample file
echo "This is a test document" > test-document.txt

# Upload it to task 1
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@test-document.txt" \
  -F "taskId=1"
```

## Step 9: View Audit Logs

```bash
# Get all audit logs
curl http://localhost:8080/api/audit-logs

# Get audit logs for a specific task
curl http://localhost:8080/api/audit-logs/entity/TASK/1
```

## Step 10: Test Notifications

### Update Task Status (Triggers Real-time Notification)

```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }'
```

Check the application logs to see the notification being sent:
```bash
docker-compose logs -f app | grep "Notification"
```

### Check Kafka Messages

1. Open Kafka UI: http://localhost:8090
2. Navigate to Topics → `notifications`
3. View messages to see notification events

## Common Operations

### View All Tasks
```bash
curl http://localhost:8080/api/tasks
```

### View All Users
```bash
curl http://localhost:8080/api/users
```

### Get Task by ID
```bash
curl http://localhost:8080/api/tasks/1
```

### Update Task
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated task title",
    "priority": "CRITICAL"
  }'
```

### Delete Task
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

### Search Tasks
```bash
curl -X POST http://localhost:8080/api/tasks/filter \
  -H "Content-Type: application/json" \
  -d '{
    "searchTerm": "documentation"
  }'
```

## Troubleshooting

### Services Not Starting

```bash
# Check logs
docker-compose logs app
docker-compose logs postgres
docker-compose logs kafka

# Restart services
docker-compose restart

# Full reset
docker-compose down -v
docker-compose up -d
```

### Port Already in Use

```bash
# Check what's using the port
lsof -i :8080

# Kill the process or change the port in docker-compose.yml
```

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres

# Connect to PostgreSQL directly
docker exec -it taskmanagement-postgres psql -U postgres -d taskmanagement
```

### Application Not Responding

```bash
# Check application logs
docker-compose logs -f app

# Restart application
docker-compose restart app

# Check health endpoint
curl http://localhost:8080/actuator/health
```

## Stopping the System

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes all data)
docker-compose down -v
```

## Next Steps

1. **Explore the API**: Use Swagger UI to try all endpoints
2. **Configure Email**: Update `application.yml` with your SMTP settings
3. **Setup S3**: Configure AWS credentials for cloud file storage
4. **Customize**: Modify notification templates and schedules
5. **Monitor**: Check Actuator endpoints for metrics
6. **Scale**: Add more application instances in docker-compose.yml

## Useful Commands

```bash
# View all running containers
docker-compose ps

# View logs for all services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f app

# Execute command in container
docker exec -it taskmanagement-app bash

# Check database
docker exec -it taskmanagement-postgres psql -U postgres -d taskmanagement

# Rebuild application
docker-compose up -d --build app

# Scale application (multiple instances)
docker-compose up -d --scale app=3
```

## Testing Scheduled Notifications

The scheduled notification service runs every hour by default. To test it:

1. Create a task with a due date within 24 hours
2. Wait for the next hour (or modify the cron expression in `application.yml`)
3. Check logs: `docker-compose logs -f app | grep "Reminder"`
4. Verify the task's `reminderSent` field is set to `true`

## Production Checklist

Before deploying to production:

- [ ] Change default passwords
- [ ] Configure external PostgreSQL
- [ ] Configure external Kafka cluster
- [ ] Set up AWS S3 for file storage
- [ ] Configure SMTP for email notifications
- [ ] Enable HTTPS/TLS
- [ ] Set up monitoring and alerting
- [ ] Configure backup strategy
- [ ] Review security settings
- [ ] Set up CI/CD pipeline
- [ ] Load testing
- [ ] Disaster recovery plan

## Support

For issues or questions:
- Check the logs: `docker-compose logs -f`
- Review the full documentation: `README.md`
- Check API documentation: `API_DOCUMENTATION.md`
- Review architecture: `ARCHITECTURE.md`

Happy task managing! 🚀

