# Docker Setup Verification Checklist

## Pre-Flight Checks

- [ ] Docker is installed (`docker --version` works)
- [ ] Docker Compose is installed (`docker-compose --version` works)
- [ ] You are in the project root directory (where docker-compose.yml is located)
- [ ] Ports 3306, 8080, and 8081 are available on your machine

## Starting Services

- [ ] Run `start-services.bat` or `docker-compose up -d`
- [ ] No error messages appear
- [ ] Services start within 30 seconds

## Verifying Services Are Running

Run this command and verify all three services show status "running" or "healthy":
```bash
docker-compose ps
```

Expected output:
```
NAME                  COMMAND                  SERVICE         STATUS
clinic-mariadb        "docker-entrypoint.s…"   mariadb         Up (healthy)
clinic-auth-service   "java -jar app.jar"      auth-service    Up
clinic-submit-service "java -jar app.jar"      submit-service  Up
```

- [ ] clinic-mariadb shows "healthy" status
- [ ] clinic-auth-service shows "running" status
- [ ] clinic-submit-service shows "running" status

## Testing Database Connectivity

Run this command to test MariaDB connection:
```bash
docker exec -it clinic-mariadb mariadb -u root -proot -e "SELECT 1;"
```

- [ ] Command returns "1" without error

## Testing Service Connectivity

### Check Auth Service
```bash
curl http://localhost:8080
```
- [ ] Returns HTTP response (200, 404, or similar - no connection refused)

### Check Submit Service
```bash
curl http://localhost:8081/api/submit
```
- [ ] Returns HTTP response (no connection refused)

### Test with Actual Endpoints
```bash
# Get all forms
curl http://localhost:8081/api/submit/forms

# Or using PowerShell
Invoke-WebRequest -Uri "http://localhost:8081/api/submit/forms"
```
- [ ] Returns JSON response or appropriate HTTP status

## Checking Logs

Look for any error messages:
```bash
docker-compose logs
```

- [ ] No critical error messages
- [ ] Services show successful startup messages
- [ ] Database connection messages are present

## Database Content

Connect to the database and verify tables were created:

### Using MariaDB CLI
```bash
docker exec -it clinic-mariadb mariadb -u root -proot clinicdb -e "SHOW TABLES;"
```

Expected tables:
- [ ] patient_form (or similar name based on your entity)
- [ ] Other necessary tables for your application

### Using a GUI Tool
- [ ] Connect to localhost:3306 with root/root
- [ ] Select clinicdb database
- [ ] Verify tables are created

## Stopping and Restarting

```bash
# Stop services
docker-compose stop

# Verify all stopped
docker-compose ps
```

- [ ] All services show as "stopped"

```bash
# Start again
docker-compose start

# Verify all started
docker-compose ps
```

- [ ] All services show as "running/healthy"

## Cleanup Test

```bash
# Stop and remove everything
docker-compose down

# List containers (should be empty)
docker ps
```

- [ ] All containers are removed

```bash
# Start fresh
docker-compose up -d

# Wait 10 seconds and verify
docker-compose ps
```

- [ ] All services start fresh correctly
- [ ] No errors on fresh start

## File Structure Verification

- [ ] docker-compose.yml exists and is properly formatted
- [ ] Dockerfile exists in submit-service/submit-service/
- [ ] .dockerignore exists in submit-service/submit-service/
- [ ] application.properties has environment variables
- [ ] All documentation files are present:
  - [ ] DOCKER_SETUP.md
  - [ ] DOCKER_QUICK_REFERENCE.md
  - [ ] DOCKER_SETUP_SUMMARY.md

## Performance Check

```bash
docker stats
```

Monitor for a few seconds:
- [ ] Memory usage is reasonable (< 2GB total)
- [ ] CPU usage is stable
- [ ] No containers are stuck or unresponsive

## Final Validation

- [ ] All three services are running
- [ ] Database is accessible
- [ ] API endpoints respond
- [ ] No console errors
- [ ] Services restart without errors
- [ ] Volume persistence works (data survives restart)

## Troubleshooting Notes

If any checks failed, note the issue and check:

| Issue | Solution |
|-------|----------|
| Services won't start | Run `docker-compose logs` to see errors |
| Port already in use | Change port in docker-compose.yml or stop other services |
| Database connection refused | Wait 30 seconds and retry |
| Out of memory | Increase Docker's memory allocation |
| Services crash on restart | Check logs for configuration issues |

## Success!

If all checks pass: ✅ Your Docker setup is complete and working!

You can now:
- Develop and test your application locally
- Deploy to a production environment
- Scale services as needed
- Collaborate with team members using the same environment

## Next Steps

1. Create API clients to test your endpoints
2. Set up automated backups for the database
3. Configure production settings
4. Set up monitoring and logging
5. Create deployment scripts for other environments

