# Docker Quick Reference

## Starting the Application

### Option 1: Using the startup script (Windows)
```bash
# Batch file (Command Prompt)
start-services.bat

# PowerShell script
.\start-services.ps1
```

### Option 2: Using Docker Compose directly
```bash
docker-compose up -d
```

## Checking Status

```bash
# List all running containers
docker-compose ps

# View logs from all services
docker-compose logs -f

# View logs from specific service
docker-compose logs -f submit-service
docker-compose logs -f auth-service
docker-compose logs -f mariadb
```

## Stopping Services

```bash
# Stop all services (containers still exist)
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop, remove containers, and delete volumes
docker-compose down -v
```

## Rebuilding Services

```bash
# Rebuild all images
docker-compose build

# Rebuild specific service
docker-compose build submit-service

# Rebuild without using cache
docker-compose build --no-cache
```

## Common Issues & Solutions

### Port already in use
If you get "address already in use" error:
- Change the port in docker-compose.yml
- Or stop other services using that port
- Windows: `netstat -ano | findstr :8080`

### Services won't start
Check the logs:
```bash
docker-compose logs
```

### Database connection refused
Wait for MariaDB to fully start (10-30 seconds) before other services attempt to connect.

### Out of disk space
Clean up Docker resources:
```bash
docker system prune -a
```

## Database Connection Details

**From outside Docker (local machine):**
- Host: localhost
- Port: 3306
- Database: clinicdb
- Username: root
- Password: root

**From inside Docker (container to container):**
- Host: mariadb
- Port: 3306
- Database: clinicdb
- Username: root
- Password: root

## Service Endpoints

- **Auth Service**: http://localhost:8080
- **Submit Service**: http://localhost:8081/api/submit

## Useful Docker Commands

```bash
# Enter database container shell
docker exec -it clinic-mariadb bash

# Execute command in container
docker exec -it clinic-mariadb mariadb -u root -proot clinicdb

# View resource usage
docker stats

# Remove unused images
docker image prune -a

# Remove stopped containers
docker container prune
```

## File Structure

```
team-a/
├── docker-compose.yml           # Main configuration file
├── .env                          # Environment variables
├── start-services.bat            # Windows batch startup script
├── start-services.ps1            # PowerShell startup script
├── DOCKER_SETUP.md               # Detailed setup guide
├── auth-service/
│   └── Dockerfile
├── submit-service/
│   └── submit-service/
│       ├── Dockerfile
│       ├── pom.xml
│       └── src/
└── README.md
```

## Next Steps

1. Run `start-services.bat` or `docker-compose up -d`
2. Wait 10-30 seconds for services to be ready
3. Check service status with `docker-compose ps`
4. Access the services on their respective ports
5. View logs if anything goes wrong

For more detailed information, see `DOCKER_SETUP.md`

