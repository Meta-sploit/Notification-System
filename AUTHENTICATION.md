# JWT Authentication Guide

## Overview

This Task Management & Notification System uses **JWT (JSON Web Token)** based authentication with **Role-Based Access Control (RBAC)** to secure all API endpoints.

---

## Table of Contents

1. [Authentication Flow](#authentication-flow)
2. [User Roles](#user-roles)
3. [Getting Started](#getting-started)
4. [API Endpoints](#api-endpoints)
5. [Using Swagger UI](#using-swagger-ui)
6. [Using cURL](#using-curl)
7. [Role-Based Permissions](#role-based-permissions)
8. [Token Details](#token-details)
9. [Troubleshooting](#troubleshooting)

---

## Authentication Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ 1. POST /api/auth/register or /api/auth/login
       │    { username, password, ... }
       ▼
┌─────────────────┐
│  Auth Service   │
└────────┬────────┘
         │
         │ 2. Validate credentials
         │ 3. Generate JWT token
         ▼
┌─────────────┐
│   Client    │ ◄── JWT Token returned
└──────┬──────┘
       │
       │ 4. Include token in subsequent requests
       │    Authorization: Bearer <token>
       ▼
┌─────────────────┐
│  Protected API  │
└────────┬────────┘
         │
         │ 5. Validate token & check permissions
         │ 6. Return response
         ▼
┌─────────────┐
│   Client    │
└─────────────┘
```

---

## User Roles

The system supports three roles with different permission levels:

| Role | Description | Permissions |
|------|-------------|-------------|
| **ADMIN** | System administrator | Full access to all endpoints (users, tasks, files, audit logs) |
| **MANAGER** | Team manager | Can manage tasks, view users, upload files |
| **USER** | Regular user | Can view tasks, manage own data |

---

## Getting Started

### 1. Start the Application

```bash
docker compose up -d
```

Wait for the application to be healthy:

```bash
curl http://localhost:8080/actuator/health
```

### 2. Register a New User

**Endpoint:** `POST /api/auth/register`

**Request:**
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

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJzdWIiOiJhZG1pbiIsImlhdCI6MTcwOTI5ODAwMCwiZXhwIjoxNzA5Mzg0NDAwfQ.xyz...",
  "username": "admin",
  "email": "admin@example.com",
  "role": "ADMIN",
  "message": "User registered successfully"
}
```

### 3. Login (Existing Users)

**Endpoint:** `POST /api/auth/login`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Response:** Same as registration response with JWT token.

---

## API Endpoints

### Public Endpoints (No Authentication Required)

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login with credentials
- `GET /actuator/health` - Health check
- `GET /swagger-ui/**` - Swagger UI
- `GET /v3/api-docs/**` - OpenAPI documentation
- `GET /api-docs/**` - API documentation

### Protected Endpoints (Authentication Required)

All other endpoints require a valid JWT token in the `Authorization` header.

#### User Management
- `GET /api/users` - Get all users (ADMIN, MANAGER)
- `GET /api/users/{id}` - Get user by ID (ADMIN, MANAGER)
- `GET /api/users/username/{username}` - Get user by username (ADMIN, MANAGER)
- `POST /api/users` - Create user (ADMIN only)
- `PUT /api/users/{id}` - Update user (ADMIN only)
- `DELETE /api/users/{id}` - Delete user (ADMIN only)

#### Task Management
- `GET /api/tasks` - Get all tasks (ADMIN, MANAGER, USER)
- `GET /api/tasks/{id}` - Get task by ID (ADMIN, MANAGER, USER)
- `POST /api/tasks/filter` - Filter tasks (ADMIN, MANAGER, USER)
- `POST /api/tasks` - Create task (ADMIN, MANAGER)
- `PUT /api/tasks/{id}` - Update task (ADMIN, MANAGER)
- `DELETE /api/tasks/{id}` - Delete task (ADMIN only)
- `POST /api/tasks/import/csv` - Bulk import tasks (ADMIN, MANAGER)

#### File Management
- `POST /api/files/upload` - Upload file (ADMIN, MANAGER)
- `GET /api/files/{id}` - Get file metadata (ADMIN, MANAGER, USER)
- `GET /api/files/{id}/download` - Download file (ADMIN, MANAGER, USER)
- `DELETE /api/files/{id}` - Delete file (ADMIN only)

#### Audit Logs
- `GET /api/audit-logs` - Get all audit logs (ADMIN only)
- `GET /api/audit-logs/entity/{entityType}/{entityId}` - Get logs by entity (ADMIN only)
- `GET /api/audit-logs/user/{userId}` - Get logs by user (ADMIN only)

---

## Using Swagger UI

### Step 1: Open Swagger UI

Navigate to: **http://localhost:8080/swagger-ui/index.html**

### Step 2: Register or Login

1. Expand the **"auth-controller"** section
2. Click on **`POST /api/auth/register`** or **`POST /api/auth/login`**
3. Click **"Try it out"**
4. Enter your credentials in the request body
5. Click **"Execute"**
6. **Copy the JWT token** from the response (the long string starting with `eyJ...`)

### Step 3: Authorize

1. Click the **🔓 Authorize** button at the top right of the page
2. In the "Value" field, paste your JWT token (just the token, no "Bearer" prefix needed)
3. Click **"Authorize"**
4. Click **"Close"**

### Step 4: Test Protected Endpoints

Now you can test any protected endpoint:
1. Expand any endpoint (e.g., `GET /api/users`)
2. Click **"Try it out"**
3. Click **"Execute"**
4. The request will automatically include your JWT token

---

## Using cURL

### With JWT Token

After obtaining a token from registration or login, include it in the `Authorization` header:

```bash
# Store the token in a variable
TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4i..."

# Make authenticated requests
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"
```

### Example: Complete Workflow

```bash
# 1. Register a new admin user
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "role": "ADMIN"
  }')

# 2. Extract the token
TOKEN=$(echo $RESPONSE | jq -r '.token')

# 3. Get all users
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" | jq .

# 4. Create a new task
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project documentation",
    "description": "Write comprehensive docs",
    "status": "PENDING",
    "priority": "HIGH",
    "assignedToId": 1,
    "dueDate": "2024-12-31T23:59:59"
  }' | jq .
```

---

## Role-Based Permissions

### ADMIN Role
- ✅ Full access to all endpoints
- ✅ Create, read, update, delete users
- ✅ Create, read, update, delete tasks
- ✅ Upload, download, delete files
- ✅ View all audit logs

### MANAGER Role
- ✅ View all users (read-only)
- ✅ Create, read, update tasks
- ✅ Upload and download files
- ❌ Cannot delete tasks
- ❌ Cannot manage users
- ❌ Cannot view audit logs

### USER Role
- ✅ View tasks (read-only)
- ✅ Download files
- ❌ Cannot create or modify tasks
- ❌ Cannot upload files
- ❌ Cannot view users
- ❌ Cannot view audit logs

---

## Token Details

### Token Structure

JWT tokens consist of three parts separated by dots (`.`):

```
eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJzdWIiOiJhZG1pbiIsImlhdCI6MTcwOTI5ODAwMCwiZXhwIjoxNzA5Mzg0NDAwfQ.signature
│                      │                                                                                │
│      Header          │                          Payload                                              │  Signature
```

### Token Payload

The token contains:
- `sub`: Username
- `role`: User role (ADMIN, MANAGER, USER)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

### Token Expiration

- **Default expiration:** 24 hours (86400000 milliseconds)
- **Configuration:** Set in `application.yml` under `jwt.expiration`
- **After expiration:** You must login again to get a new token

### Security Configuration

- **Algorithm:** HS256 (HMAC with SHA-256)
- **Secret Key:** Configured in `application.yml` under `jwt.secret`
- **Session Management:** Stateless (no server-side sessions)

---

## Troubleshooting

### Error: 403 Forbidden

**Cause:** Missing or invalid JWT token, or insufficient permissions.

**Solutions:**
1. Ensure you're including the token in the `Authorization` header
2. Verify the token format: `Authorization: Bearer <token>`
3. Check if your token has expired (login again)
4. Verify your role has permission for the endpoint

### Error: 401 Unauthorized

**Cause:** Invalid credentials during login.

**Solutions:**
1. Verify username and password are correct
2. Ensure the user exists (register first if needed)

### Token Not Working in Swagger

**Solutions:**
1. Click the **Authorize** button (not just paste in request)
2. Paste only the token value (no "Bearer" prefix in Swagger)
3. Click "Authorize" then "Close"
4. Try the request again

### Cannot Access Swagger UI

**Solutions:**
1. Verify the application is running: `docker compose ps`
2. Check application health: `curl http://localhost:8080/actuator/health`
3. View logs: `docker compose logs app --tail=50`
4. Ensure port 8080 is not blocked by firewall

---

## Configuration

### JWT Settings (application.yml)

```yaml
jwt:
  secret: mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm
  expiration: 86400000  # 24 hours in milliseconds
```

### Changing Token Expiration

To change token expiration time, update `jwt.expiration` in `application.yml`:

```yaml
jwt:
  expiration: 3600000  # 1 hour
  # expiration: 86400000  # 24 hours (default)
  # expiration: 604800000  # 7 days
```

Then rebuild and restart:

```bash
docker compose up -d --build app
```

---

## Security Best Practices

1. **Never commit the JWT secret to version control**
   - Use environment variables in production
   - Rotate secrets regularly

2. **Use HTTPS in production**
   - JWT tokens should only be transmitted over secure connections

3. **Store tokens securely on the client**
   - Use httpOnly cookies or secure storage
   - Never store in localStorage if XSS is a concern

4. **Implement token refresh mechanism**
   - Consider adding refresh tokens for better UX

5. **Monitor and log authentication attempts**
   - Check audit logs regularly for suspicious activity

---

## Next Steps

- ✅ JWT authentication is fully implemented
- ✅ Role-based access control is enforced
- ⚠️ Email notifications require SMTP configuration
- ⚠️ AWS S3 integration requires credentials (currently using local storage)
- 📝 Consider implementing refresh tokens
- 📝 Consider adding password reset functionality
- 📝 Consider adding email verification

---

For more information, see:
- [Main README](README.md)
- [API Documentation](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI Spec](http://localhost:8080/api-docs)

