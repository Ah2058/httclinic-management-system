# 🏥 Clinic Application - Complete Docker Deployment Guide

## 📋 Overview

This is a complete, production-ready Docker setup for your clinic patient form submission application. It includes:

- **Auth Service** - Spring Boot authentication and user management (Port 8080)
- **Submit Service** - Patient form submission system (Port 8081)
- **MariaDB** - Shared relational database (Port 3306)

Everything is containerized and runs in isolated Docker containers with proper networking and data persistence.

---

## 🚀 Quick Start (2 Minutes)

### Windows Users

```bash
# Simply double-click this file:
start-services.bat

# OR run in Command Prompt/PowerShell:
start-services.bat
```

### Linux/Mac Users or Docker Compose Direct

```bash
docker-compose up -d
```

### Verify Everything is Running

```bash
docker-compose ps
```

Expected output should show all 3 containers as "running" or "healthy".

---

## 📊 System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   Docker Network                             │
│                  (clinic-network)                           │
├──────────────────┬──────────────────┬──────────────────────┤
│                  │                  │                      │
│  Auth Service    │  Submit Service  │  MariaDB Database   │
│  Port 8080       │  Port 8081       │  Port 3306          │
│  Spring Boot     │  Spring Boot     │  Database Engine    │
│                  │                  │                      │
│ ✓ JPA            │ ✓ JPA            │ ✓ clinicdb          │
│ ✓ REST API       │ ✓ REST API       │ ✓ Persistent Data   │
│ ✓ Validation     │ ✓ Validation     │ ✓ Health Checks     │
│                  │                  │                      │
└──────────────────┴──────────────────┴──────────────────────┘
```

---

## 📁 What's Included

### Configuration Files
- **docker-compose.yml** - Main orchestration file
- **.env** - Environment variables
- **Dockerfile** (submit-service) - Container build specification
- **.dockerignore** (submit-service) - Build optimization

### Startup Scripts
- **start-services.bat** - Windows batch startup script
- **start-services.ps1** - PowerShell startup script

### Documentation
- **DOCKER_SETUP.md** - Detailed setup and configuration guide
- **DOCKER_QUICK_REFERENCE.md** - Common Docker commands
- **DOCKER_SETUP_SUMMARY.md** - Overview and architecture
- **VERIFICATION_CHECKLIST.md** - Verification and testing steps
- **DOCKER_FILES_CREATED.md** - Complete file listing and changes
- **This file** - Getting started guide

---

## 🌐 Accessing Services

After starting with `start-services.bat` or `docker-compose up -d`:

### Auth Service
```
URL: http://localhost:8080
Port: 8080
Purpose: Authentication and user management
```

### Submit Service
```
URL: http://localhost:8081/api/submit
Port: 8081
Path: /api/submit
Purpose: Patient form submission
```

### Database
```
Host: localhost
Port: 3306
Database: clinicdb
Username: root
Password: root
```

---

## 🛠️ Common Tasks

### View Service Status
```bash
docker-compose ps
```

### View Live Logs
```bash
docker-compose logs -f                    # All services
docker-compose logs -f submit-service     # Specific service
```

### Stop All Services
```bash
docker-compose down
```

### Stop and Remove All Data
```bash
docker-compose down -v
```

### Restart a Service
```bash
docker-compose restart submit-service
```

### Rebuild Images
```bash
docker-compose build --no-cache
```

### Connect to Database
```bash
docker exec -it clinic-mariadb mariadb -u root -proot clinicdb
```

---

## 🔍 Service Details

### MariaDB Database
| Property | Value |
|----------|-------|
| Image | mariadb:11 |
| Port | 3306 |
| Database | clinicdb |
| Username | root |
| Password | root |
| Volume | mariadb_data (persistent) |
| Health Check | Enabled |

### Auth Service
| Property | Value |
|----------|-------|
| Build | ./auth-service/Dockerfile |
| Port | 8080 |
| Database | clinicdb |
| Framework | Spring Boot 4.0.5 |
| Java | 17 |
| DDL Strategy | Update (auto-create tables) |
| Depends On | MariaDB (healthy) |

### Submit Service
| Property | Value |
|----------|-------|
| Build | ./submit-service/submit-service/Dockerfile |
| Port | 8081 |
| Context Path | /api/submit |
| Database | clinicdb |
| Framework | Spring Boot 4.0.5 |
| Java | 17 |
| DDL Strategy | Update (auto-create tables) |
| Depends On | MariaDB (healthy) |

---

## 📝 Environment Variables

The following environment variables are configured in docker-compose.yml:

```yaml
SPRING_DATASOURCE_URL: jdbc:mariadb://mariadb:3306/clinicdb
SPRING_DATASOURCE_USERNAME: root
SPRING_DATASOURCE_PASSWORD: root
SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.mariadb.jdbc.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO: update
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT: org.hibernate.dialect.MariaDBDialect
```

To modify, edit `docker-compose.yml` or create `docker-compose.override.yml` for local changes.

---

## ✅ Verification Steps

### 1. Services Running
```bash
docker-compose ps
# All 3 containers should show as "running" or "healthy"
```

### 2. Database Accessible
```bash
docker exec -it clinic-mariadb mariadb -u root -proot -e "SELECT 1;"
# Should return: 1
```

### 3. Auth Service Responsive
```bash
curl http://localhost:8080
# Should return HTTP response (not connection refused)
```

### 4. Submit Service Responsive
```bash
curl http://localhost:8081/api/submit/forms
# Should return JSON or HTTP status (not connection refused)
```

### 5. Check Logs for Errors
```bash
docker-compose logs
# Should show successful startup messages, no critical errors
```

---

## 🐛 Troubleshooting

### Services Won't Start

```bash
docker-compose logs
```
Check for error messages and address accordingly.

### Port Already in Use

Edit `docker-compose.yml` and change the port:
```yaml
ports:
  - "9000:8080"  # Changed from 8080 to 9000
```

### Database Connection Refused

Wait 10-30 seconds for MariaDB to fully start, then retry.

### Out of Memory

Increase Docker's allocated memory in Docker Desktop settings.

### Services Crash Immediately

Check logs with `docker-compose logs` to identify the issue.

### Clean Rebuild

```bash
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

---

## 🔗 Service Communication

Services communicate via the `clinic-network`:
- Auth Service → MariaDB via hostname `mariadb:3306`
- Submit Service → MariaDB via hostname `mariadb:3306`
- Both services use the shared `clinicdb` database

From outside Docker, use `localhost` and their respective ports.

---

## 💾 Data Persistence

Database data is persisted in a Docker named volume `mariadb_data`. This means:
- ✅ Data survives container restarts
- ✅ Data survives `docker-compose down` commands
- ❌ Data is deleted only with `docker-compose down -v`

---

## 📈 Performance Considerations

- **Memory**: Services typically use 200-500MB each (adjust if needed)
- **Disk**: Initial image build ~200-300MB per service
- **Startup Time**: 10-30 seconds for all services to be ready
- **CPU**: Minimal during idle, scales with load

---

## 🔐 Security Notes

### Development (Current)
- Default credentials are for local development only
- MariaDB is exposed on port 3306 (local machine only)
- Health checks are enabled

### Production Deployment
Before deploying to production:
1. ✅ Change default credentials in `.env` and docker-compose.yml
2. ✅ Disable unnecessary port exposures
3. ✅ Enable SSL/TLS for API endpoints
4. ✅ Configure proper backup strategies
5. ✅ Implement proper authentication and authorization
6. ✅ Use secrets management for sensitive data
7. ✅ Enable container logging and monitoring
8. ✅ Set up resource limits for containers

---

## 📚 Documentation

For detailed information, see:

1. **DOCKER_SETUP.md**
   - Comprehensive setup guide
   - Detailed configuration options
   - Environment setup
   - Complete troubleshooting guide

2. **DOCKER_QUICK_REFERENCE.md**
   - Quick command reference
   - Common Docker commands
   - Database connection examples
   - Useful Docker operations

3. **VERIFICATION_CHECKLIST.md**
   - Step-by-step verification
   - Testing procedures
   - Performance checks
   - Cleanup procedures

4. **DOCKER_SETUP_SUMMARY.md**
   - High-level overview
   - Architecture diagram
   - Service configuration
   - Key features

5. **DOCKER_FILES_CREATED.md**
   - Complete file listing
   - What was created/modified
   - Project structure

---

## 🚦 Status Indicators

### Healthy System
- ✅ All containers show "running" or "healthy"
- ✅ API endpoints respond without errors
- ✅ Database queries execute successfully
- ✅ No ERROR messages in logs

### Health Check
```bash
docker-compose ps
# MariaDB should show (healthy)
# Services should show (running)
```

---

## 🎯 Next Steps

### 1. Start Services
```bash
start-services.bat    # or docker-compose up -d
```

### 2. Wait for Health Check
```bash
docker-compose ps    # Wait until mariadb shows (healthy)
```

### 3. Test Endpoints
```bash
curl http://localhost:8081/api/submit/forms
```

### 4. Monitor Logs
```bash
docker-compose logs -f
```

### 5. Develop & Deploy

Your system is now ready for development, testing, and deployment!

---

## 📞 Support Resources

### Quick Reference Commands
```bash
docker-compose up -d          # Start all services
docker-compose down           # Stop all services
docker-compose ps             # Show status
docker-compose logs -f        # View logs
docker-compose restart [service]  # Restart service
```

### Verification Commands
```bash
docker exec -it clinic-mariadb mariadb -u root -proot -e "SELECT 1;"
curl http://localhost:8080
curl http://localhost:8081/api/submit/forms
```

### Database Access
```bash
# From terminal
docker exec -it clinic-mariadb mariadb -u root -proot clinicdb

# From GUI tool (DBeaver, TablePlus, etc.)
Host: localhost
Port: 3306
Username: root
Password: root
Database: clinicdb
```

---

## 🎉 You're All Set!

Your Docker environment is completely set up and ready to use. All services are containerized, networked, and configured for optimal local development and testing.

**Start now with:**
```bash
start-services.bat
```

Or directly with:
```bash
docker-compose up -d
```

Then access:
- Auth Service: http://localhost:8080
- Submit Service: http://localhost:8081/api/submit
- Database: localhost:3306

Happy coding! 🚀

