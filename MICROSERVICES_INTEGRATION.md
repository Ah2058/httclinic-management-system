# Microservices Integration: Auth Service ↔ Submit Service

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT (Postman)                        │
└────────────────┬────────────────────────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
┌──────────────────┐  ┌──────────────────┐
│  AUTH SERVICE    │  │ SUBMIT SERVICE   │
│  Port: 8080      │  │  Port: 8081      │
│                  │  │                  │
│ GET TOKEN        │  │ VALIDATE TOKEN   │
│ (/api/auth/      │  │ (/api/submit/    │
│  login)          │  │  api/forms)      │
└────────┬─────────┘  └──────────┬───────┘
         │                       │
         └───────────┬───────────┘
                     │
                     ▼
        ┌────────────────────────┐
        │    MariaDB Database    │
        │  (clinicdb)            │
        │  - Shared user table   │
        └────────────────────────┘
```

## Service Details

### 1️⃣ AUTH SERVICE (Port 8080)
- **Base URL**: `http://localhost:8080/api/auth`
- **Endpoint**: `POST /login`
- **Function**: Generate JWT tokens for authentication
- **Request**:
  ```json
  {
    "username": "admin",
    "password": "password"
  }
  ```
- **Response**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```

### 2️⃣ SUBMIT SERVICE (Port 8081)
- **Base URL**: `http://localhost:8081/api/submit`
- **Endpoints**:
  - `POST /api/forms` - Submit patient form (No auth needed)
  - `GET /api/forms` - Get all forms (Requires JWT)
  - `PATCH /api/forms/{id}/admin` - Update form (Requires JWT)

### Security Levels:
✅ Public: /api/forms POST (submit form - no auth needed)
🔐 Protected: /api/forms GET (get all forms - requires JWT)
🔐 Protected: /api/forms/{id}/admin PATCH (update form - requires JWT)

## How They Connect

### Flow Diagram:
```
1. USER LOGS IN
   └─► POST http://localhost:8080/api/auth/login
       └─► Returns JWT Token

2. USER SUBMITS FORM (No auth needed)
   └─► POST http://localhost:8081/api/submit/api/forms
       └─► Form submitted successfully

3. ADMIN ACCESSES FORMS (Auth required)
   └─► GET http://localhost:8081/api/submit/api/forms
       └─► Header: Authorization: Bearer {JWT_TOKEN}
       └─► Returns list of all forms

4. ADMIN UPDATES FORM (Auth required)
   └─► PATCH http://localhost:8081/api/submit/api/forms/{id}/admin
       └─► Header: Authorization: Bearer {JWT_TOKEN}
       └─► Diagnosis and notes updated
```

## JWT Token Validation

**Both services use the same JWT secret:**
```
Secret Key: VGhpcyBpcyBhIHNhbXBsZSBzZWNyZXQga2V5IGZvciBKV1Qgc2lnbmluZyE=
Expiration: 86400000 ms (24 hours)
```

**Token Format:**
```
Authorization: Bearer {JWT_TOKEN}
```

## Testing Workflow

### Step 1: Get JWT Token
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

**Response:**
```
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMzYwNDU2MCwiZXhwIjoxNzEzNjkwOTYwfQ..."
}
```

### Step 2: Submit Patient Form (No Auth)
```
POST http://localhost:8081/api/submit/api/forms
Content-Type: application/json

{
  "firstName": "JOHN",
  "lastName": "SMITH",
  "dateOfBirth": "1990-05-15",
  "streetName": "MAIN",
  "streetNumber": "123",
  "city": "TORONTO",
  "postalCode": "12345",
  "phoneNumber": "+1-416-555-1234",
  "emailAddress": "john@example.com"
}
```

### Step 3: Get All Forms (Requires Auth)
```
GET http://localhost:8081/api/submit/api/forms
Authorization: Bearer {JWT_TOKEN_FROM_STEP1}
```

### Step 4: Update Form (Requires Auth)
```
PATCH http://localhost:8081/api/submit/api/forms/1/admin
Authorization: Bearer {JWT_TOKEN_FROM_STEP1}
Content-Type: application/json

{
  "diagnosis": "Flu",
  "notes": "Patient requires rest and fluids"
}
```

## Microservices Communication

- ✅ **Both services share:** Same JWT secret and database connection
- ✅ **Auth Service:** Generates tokens
- ✅ **Submit Service:** Validates tokens using same secret
- ✅ **Database:** MariaDB (clinicdb) stores both auth and patient data
- ✅ **Service Discovery:** Docker Compose networks handle inter-service communication

## Security Flow

```
Client Request → Submit Service
                    ↓
            JwtAuthenticationFilter
                    ↓
           Check Authorization Header
                    ↓
         Validate Token (JwtProvider)
                    ↓
         Token Valid? → Continue to Controller
                 ↓
         Token Invalid? → Return 401 Unauthorized
```

## Architecture Benefits

✅ **Separation of Concerns**: Auth and Submit are independent services
✅ **Scalability**: Each service can scale independently
✅ **Security**: JWT-based stateless authentication
✅ **Reusability**: Other microservices can also use the Auth Service
✅ **Maintainability**: Changes in one service don't affect others

