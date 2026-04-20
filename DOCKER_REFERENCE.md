# Docker Setup - Complete Reference

## 🚀 One-Command Startup

### Windows
```bash
start-services.bat
```

### Any OS
```bash
docker-compose up -d
```

---

## 📊 System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                  LOCAL MACHINE (localhost)                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Docker Engine & Network                     │   │
│  │           (clinic-network - Bridge Mode)                 │   │
│  │                                                          │   │
│  │  ┌─────────────────┐  ┌──────────────────┐  ┌─────────┐│   │
│  │  │  Auth Service   │  │ Submit Service   │  │ MariaDB ││   │
│  │  │  Container      │  │ Container        │  │Container││   │
│  │  ├─────────────────┤  ├──────────────────┤  ├─────────┤│   │
│  │  │ Port: 8080      │  │ Port: 8081       │  │Port:3306││   │
│  │  │ Spring Boot     │  │ Spring Boot      │  │Database ││   │
│  │  │ Java 17         │  │ Java 17          │  │MariaDB11││   │
│  │  │                 │  │                  │  │         ││   │
│  │  │ ✓ Authentication│  │ ✓ Form Submit    │  │ ✓clinicdb││  │
│  │  │ ✓ REST API      │  │ ✓ Validation     │  │ ✓Tables ││   │
│  │  │ ✓ DB Connection │  │ ✓ DB Connection  │  │ ✓VolData││   │
│  │  │                 │  │                  │  │         ││   │
│  │  │ Container Name: │  │ Container Name:  │  │Container││   │
│  │  │ clinic-auth-svc │  │clinic-submit-svc │  │clinic-db││   │
│  │  │                 │  │                  │  │         ││   │
│  │  │ Healthcheck: N/A│  │ Healthcheck: N/A │  │Healthy ✓││   │
│  │  └────────┬────────┘  └────────┬─────────┘  └────┬────┘│   │
│  │           │                    │                 │     │   │
│  │           └────────────────────┼─────────────────┘     │   │
│  │                                │                       │   │
│  │         All connected via: mariadb:3306              │   │
│  │         Shared Database: clinicdb                     │   │
│  │         Shared Volume: mariadb_data                   │   │
│  │                                                       │   │
│  └───────────────────────────────────────────────────────┘   │
│                                                                │
│  Host Ports (External Access)                                 │
│  ├─ 8080  → Auth Service                                      │
│  ├─ 8081  → Submit Service                                    │
│  └─ 3306  → MariaDB Database                                  │
│                                                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📋 File Structure

```
team-a/
│
├── 🐳 Docker Configuration
│   ├── docker-compose.yml              ← Main orchestration file
│   ├── .env                            ← Environment variables
│   ├── start-services.bat              ← Windows startup script
│   └── start-services.ps1              ← PowerShell startup script
│
├── 📖 Documentation
│   ├── DOCKER_README.md                ← START HERE (Quick start)
│   ├── DOCKER_SETUP.md                 ← Detailed setup guide
│   ├── DOCKER_QUICK_REFERENCE.md       ← Command reference
│   ├── DOCKER_SETUP_SUMMARY.md         ← Overview
│   ├── VERIFICATION_CHECKLIST.md       ← Test & verify
│   ├── DOCKER_FILES_CREATED.md         ← All changes made
│   └── THIS_FILE                       ← Reference diagram
│
├── 🔐 Auth Service
│   └── auth-service/
│       ├── Dockerfile                  (Existing)
│       ├── pom.xml                     (Existing)
│       ├── mvnw / mvnw.cmd            (Existing)
│       └── src/
│           └── main/java/infrax/teama/auth_service/
│
├── 📝 Submit Service (Patient Forms)
│   └── submit-service/submit-service/
│       ├── Dockerfile                  ← NEW (Multi-stage build)
│       ├── .dockerignore               ← NEW (Build optimization)
│       ├── pom.xml                     (Existing)
│       ├── mvnw / mvnw.cmd            (Existing)
│       └── src/
│           └── main/
│               ├── java/infrax/teama/submit_service/
│               │   ├── controller/     (API endpoints)
│               │   ├── service/        (Business logic)
│               │   ├── dto/            (Data transfer objects)
│               │   ├── model/          (Domain entities)
│               │   └── repository/     (Database access)
│               └── resources/
│                   └── application.properties  ← UPDATED (Env vars)
│
└── README.md                            (Original project README)
```

---

## 🔄 Data Flow

```
User/Client
    │
    ├─────────────────────┬─────────────────────┐
    │                     │                     │
    ▼                     ▼                     ▼
    
http://localhost:8080     http://localhost:8081/api/submit     localhost:3306
         │                          │                              │
         │                          │                              │
         ▼                          ▼                              ▼
    
┌─────────────────┐   ┌──────────────────┐   ┌──────────────┐
│  Auth Service   │   │ Submit Service   │   │   MariaDB    │
│  Port 8080      │   │ Port 8081        │   │  Database    │
├─────────────────┤   ├──────────────────┤   ├──────────────┤
│ • Login         │   │ • Form Submission│   │ • Queries    │
│ • User Mgmt     │   │ • Validation     │   │ • Tables     │
│ • Authentication   │ • Processing     │   │ • Persistence│
└────────┬────────┘   └────────┬─────────┘   └──────┬───────┘
         │                     │                    │
         └─────────────────────┼────────────────────┘
                               │
                      clinicdb (Shared Database)
                      Connected via mariadb:3306
```

---

## 🎯 Quick Commands

```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Restart specific service
docker-compose restart submit-service

# Rebuild images
docker-compose build --no-cache

# Connect to database
docker exec -it clinic-mariadb mariadb -u root -proot clinicdb

# Test endpoints
curl http://localhost:8080
curl http://localhost:8081/api/submit/forms
```

---

## 🔗 Service URLs & Credentials

| Service | URL | Credentials | Purpose |
|---------|-----|-------------|---------|
| Auth Service | http://localhost:8080 | N/A | Authentication & User Management |
| Submit Service | http://localhost:8081/api/submit | N/A | Patient Form Submission |
| MariaDB | localhost:3306 | root/root | Database Server |

---

## 📊 Service Dependencies

```
                    ┌────────────────┐
                    │   MariaDB      │
                    │   (Database)   │
                    └────────┬───────┘
                             │
                             │ Depends On
                             │ (Health Check)
                             │
                    ┌────────┴───────────────────────┐
                    │                                │
                    ▼                                ▼
            ┌──────────────────┐          ┌──────────────────┐
            │  Auth Service    │          │ Submit Service   │
            │  Port: 8080      │          │ Port: 8081       │
            └──────────────────┘          └──────────────────┘
                    │                                │
                    └────────────┬───────────────────┘
                                 │
                       (Both connect to clinicdb)
```

---

## 🔐 Environment Variables

```yaml
# Database Configuration
SPRING_DATASOURCE_URL: jdbc:mariadb://mariadb:3306/clinicdb
SPRING_DATASOURCE_USERNAME: root
SPRING_DATASOURCE_PASSWORD: root
SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.mariadb.jdbc.Driver

# Hibernate/JPA Configuration
SPRING_JPA_HIBERNATE_DDL_AUTO: update
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT: org.hibernate.dialect.MariaDBDialect
```

---

## ✅ Health Check Flow

```
1. Docker Compose starts
    │
    ├─→ Starts MariaDB container
    │   │
    │   ├─→ Runs healthcheck command
    │   │   "healthcheck.sh --connect --innodb_initialized"
    │   │
    │   └─→ Waits for healthy status
    │       (interval: 10s, timeout: 5s, retries: 5)
    │
    ├─→ Auth Service waits for MariaDB health
    │   │
    │   ├─→ MariaDB reported healthy
    │   │
    │   └─→ Auth Service starts
    │
    └─→ Submit Service waits for MariaDB health
        │
        ├─→ MariaDB reported healthy
        │
        └─→ Submit Service starts

Result: All services running and healthy ✓
```

---

## 📈 Port Mapping

```
Container Port      Host Port       Service
────────────────    ──────────────  ──────────────────────
8080                8080            Auth Service (Spring)
8081                8081            Submit Service (Spring)
3306                3306            MariaDB Database
```

External Access: Use **Host Port**
Internal Access (Container-to-Container): Use **Container Port**

---

## 💾 Volume & Data

```
┌─────────────────────────────────┐
│      Docker Named Volume        │
│      mariadb_data               │
├─────────────────────────────────┤
│ Contains:                       │
│ • MySQL/MariaDB data files      │
│ • Tables                        │
│ • Indexes                       │
│ • Binary logs                   │
│                                 │
│ Mounted at:                     │
│ /var/lib/mysql (in container)   │
│                                 │
│ Persistence:                    │
│ • Survives container restart    │
│ • Survives docker-compose down  │
│ • Only deleted with: down -v    │
└─────────────────────────────────┘
```

---

## 🚦 Status Indicators

### Healthy System
```
$ docker-compose ps

NAME                  COMMAND                  SERVICE         STATUS
clinic-mariadb        "docker-entrypoint..."   mariadb         Up (healthy)
clinic-auth-service   "java -jar app.jar"      auth-service    Up
clinic-submit-service "java -jar app.jar"      submit-service  Up
```

### Problem Indicators
```
- Status shows "Exit" → Container crashed (check logs)
- Status shows "Restarting" → Container repeatedly failing
- MariaDB shows "unhealthy" → Database not ready (wait longer)
- Service shows "error" → Configuration or connection issue
```

---

## 🎯 Common Operations

### Daily Development
```bash
# Start your day
docker-compose up -d

# Work on code...

# Check status
docker-compose ps

# End of day
docker-compose down
```

### After Code Changes
```bash
# Rebuild modified service
docker-compose build submit-service

# Restart service
docker-compose restart submit-service

# View logs
docker-compose logs -f submit-service
```

### Full Fresh Start
```bash
# Stop everything and remove all data
docker-compose down -v

# Rebuild all images
docker-compose build --no-cache

# Start fresh
docker-compose up -d
```

---

## 📚 Documentation Map

```
START HERE
    ↓
DOCKER_README.md ─────────→ Quick start & overview
    │
    ├─→ DOCKER_SETUP.md ─────────→ Detailed configuration
    │
    ├─→ DOCKER_QUICK_REFERENCE.md → Command cheat sheet
    │
    ├─→ VERIFICATION_CHECKLIST.md ─→ Testing & verification
    │
    └─→ THIS FILE ─────────────────→ Diagrams & reference
```

---

## ✨ Key Features

✅ **Multi-stage Docker builds** - Optimized image sizes
✅ **Health checks** - Automatic service readiness detection
✅ **Shared network** - Seamless inter-service communication
✅ **Data persistence** - Database survives restarts
✅ **Environment configuration** - Easy customization
✅ **Startup scripts** - One-command setup
✅ **Comprehensive docs** - Multiple guides & references
✅ **Production-ready** - Proper configuration & best practices

---

## 🎉 Ready to Go!

Everything is configured and ready to use. Simply run:

```bash
start-services.bat    # Windows
# or
docker-compose up -d  # Any OS
```

Then access your services at:
- Auth Service: http://localhost:8080
- Submit Service: http://localhost:8081/api/submit
- Database: localhost:3306

Happy coding! 🚀

