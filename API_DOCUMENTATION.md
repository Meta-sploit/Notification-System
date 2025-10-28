# API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
Currently using HTTP Basic Authentication. Default credentials:
- Username: `admin`
- Password: `admin123`

---

## User Management APIs

### 1. Create User
**Endpoint**: `POST /users`

**Request Body**:
```json
{
  "username": "john.doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "role": "USER"
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "role": "USER",
  "active": true,
  "createdAt": "2025-10-28T10:00:00",
  "updatedAt": "2025-10-28T10:00:00"
}
```

**Validation Rules**:
- `username`: Required, 3-50 characters
- `email`: Required, valid email format
- `password`: Required, minimum 6 characters
- `role`: Required, one of: ADMIN, MANAGER, USER

---

### 2. Get All Users
**Endpoint**: `GET /users`

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER",
    "active": true,
    "createdAt": "2025-10-28T10:00:00",
    "updatedAt": "2025-10-28T10:00:00"
  }
]
```

---

### 3. Get User by ID
**Endpoint**: `GET /users/{id}`

**Response**: `200 OK`
```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "USER",
  "active": true
}
```

---

### 4. Update User
**Endpoint**: `PUT /users/{id}`

**Request Body**:
```json
{
  "email": "john.updated@example.com",
  "firstName": "John",
  "lastName": "Doe Updated",
  "phoneNumber": "+1234567890",
  "role": "MANAGER",
  "active": true
}
```

**Response**: `200 OK`

---

### 5. Delete User
**Endpoint**: `DELETE /users/{id}`

**Response**: `204 No Content`

---

## Task Management APIs

### 1. Create Task
**Endpoint**: `POST /tasks`

**Request Body**:
```json
{
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication to the API",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2025-11-15T10:00:00",
  "assigneeId": 1,
  "createdById": 2,
  "estimatedHours": 8,
  "tags": "backend,security"
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication to the API",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2025-11-15T10:00:00",
  "assigneeId": 1,
  "assigneeName": "john.doe",
  "createdById": 2,
  "createdByName": "admin",
  "reminderSent": false,
  "estimatedHours": 8,
  "tags": "backend,security",
  "createdAt": "2025-10-28T10:00:00",
  "updatedAt": "2025-10-28T10:00:00"
}
```

**Validation Rules**:
- `title`: Required
- `status`: Required, one of: TODO, IN_PROGRESS, IN_REVIEW, COMPLETED, CANCELLED
- `priority`: Required, one of: LOW, MEDIUM, HIGH, CRITICAL

---

### 2. Get All Tasks
**Endpoint**: `GET /tasks`

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "title": "Implement user authentication",
    "status": "TODO",
    "priority": "HIGH",
    "assigneeName": "john.doe"
  }
]
```

---

### 3. Get Task by ID
**Endpoint**: `GET /tasks/{id}`

**Response**: `200 OK`

---

### 4. Filter Tasks
**Endpoint**: `POST /tasks/filter`

**Request Body**:
```json
{
  "status": "TODO",
  "priority": "HIGH",
  "assigneeId": 1,
  "dueDateFrom": "2025-10-01T00:00:00",
  "dueDateTo": "2025-12-31T23:59:59",
  "searchTerm": "authentication",
  "reminderSent": false
}
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "title": "Implement user authentication",
    "status": "TODO",
    "priority": "HIGH"
  }
]
```

**Filter Parameters** (all optional):
- `status`: Filter by task status
- `priority`: Filter by priority level
- `assigneeId`: Filter by assigned user
- `dueDateFrom`: Filter tasks due after this date
- `dueDateTo`: Filter tasks due before this date
- `searchTerm`: Search in title and description
- `reminderSent`: Filter by reminder status

---

### 5. Update Task
**Endpoint**: `PUT /tasks/{id}`

**Request Body**:
```json
{
  "title": "Implement user authentication - Updated",
  "status": "IN_PROGRESS",
  "priority": "CRITICAL",
  "assigneeId": 2,
  "actualHours": 4
}
```

**Response**: `200 OK`

**Note**: Updating status or assignee triggers real-time notifications.

---

### 6. Delete Task
**Endpoint**: `DELETE /tasks/{id}`

**Response**: `204 No Content`

---

### 7. Bulk Import Tasks from CSV
**Endpoint**: `POST /tasks/import/csv`

**Request**: `multipart/form-data`
- `file`: CSV file

**CSV Format**:
```csv
title,description,status,priority,due_date,assignee_id,estimated_hours,tags
Implement feature,Description here,TODO,HIGH,2025-11-15 10:00:00,1,8,backend;security
```

**Response**: `201 Created`
```json
[
  {
    "id": 1,
    "title": "Implement feature",
    "status": "TODO"
  }
]
```

---

## File Management APIs

### 1. Upload File
**Endpoint**: `POST /files/upload`

**Request**: `multipart/form-data`
- `file`: File to upload
- `taskId`: Task ID (required)
- `uploadedById`: User ID (optional)

**Response**: `201 Created`
```json
{
  "id": 1,
  "fileName": "document.pdf",
  "fileKey": "abc123-def456.pdf",
  "contentType": "application/pdf",
  "fileSize": 1024000,
  "taskId": 1,
  "uploadedAt": "2025-10-28T10:00:00",
  "downloadUrl": "https://s3.amazonaws.com/bucket/abc123-def456.pdf?signature=..."
}
```

---

### 2. Get File by ID
**Endpoint**: `GET /files/{id}`

**Response**: `200 OK`

---

### 3. Get Files by Task ID
**Endpoint**: `GET /files/task/{taskId}`

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "fileName": "document.pdf",
    "fileSize": 1024000,
    "downloadUrl": "https://..."
  }
]
```

---

### 4. Get Download URL
**Endpoint**: `GET /files/{id}/download-url`

**Response**: `200 OK`
```
https://s3.amazonaws.com/bucket/file.pdf?signature=...
```

**Note**: Pre-signed URLs expire after 1 hour (configurable).

---

### 5. Delete File
**Endpoint**: `DELETE /files/{id}`

**Response**: `204 No Content`

---

## Audit Log APIs

### 1. Get All Audit Logs
**Endpoint**: `GET /audit-logs`

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "entityType": "TASK",
    "entityId": 1,
    "action": "CREATE",
    "performedBy": "SYSTEM",
    "details": "Task created",
    "timestamp": "2025-10-28T10:00:00"
  }
]
```

---

### 2. Get Audit Logs for Entity
**Endpoint**: `GET /audit-logs/entity/{entityType}/{entityId}`

**Example**: `GET /audit-logs/entity/TASK/1`

**Response**: `200 OK`

---

### 3. Get Audit Logs by User
**Endpoint**: `GET /audit-logs/user/{username}`

**Response**: `200 OK`

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2025-10-28T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input parameters",
  "path": "/api/users",
  "validationErrors": {
    "email": "Email should be valid",
    "password": "Password must be at least 6 characters"
  }
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-10-28T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: '999'",
  "path": "/api/users/999"
}
```

### 409 Conflict
```json
{
  "timestamp": "2025-10-28T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "User already exists with email: 'john@example.com'",
  "path": "/api/users"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2025-10-28T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/tasks"
}
```

---

## Notification Events

### Real-time Notifications

Triggered automatically on:
1. **Task Assignment**: When a task is assigned to a user
2. **Status Change**: When task status is updated

### Scheduled Notifications

- **Task Reminders**: Sent 24 hours before due date (configurable)
- **Cron Schedule**: Runs every hour by default

### Notification Channels

1. **Email**: Direct email to user's registered email
2. **Kafka**: Published to `notifications` topic for external consumers

---

## Rate Limits

Currently no rate limits are enforced. Consider implementing rate limiting for production use.

---

## Pagination

Currently not implemented. All list endpoints return complete results. Consider adding pagination for large datasets.

---

## Versioning

API Version: v1 (implicit in base URL)

Future versions will use: `/api/v2/...`
