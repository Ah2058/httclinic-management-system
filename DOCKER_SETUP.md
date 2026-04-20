# Clinic Application - Docker Setup Guide

This repository contains a clinic application with two Spring Boot microservices:
- **Auth Service**: Authentication and user management (port 8080)
- **Submit Service**: Patient form submission (port 8081)
- **MariaDB**: Shared database (port 3306)

## Prerequisites

- Docker installed (version 20.10 or higher)
- Docker Compose installed (version 2.0 or higher)

## Quick Start

### 1. Start all services

```bash
docker-compose up -d
```

This command will:
- Create and start the MariaDB database container
- Build and start the auth-service container
- Build and start the submit-service container
- Create a shared network for inter-service communication

### 2. Verify services are running

```bash
docker-compose ps
```

You should see three containers running:
- `clinic-mariadb`
- `clinic-auth-service`
- `clinic-submit-service`

### 3. Access the services

- **Auth Service**: http://localhost:8080
- **Submit Service**: http://localhost:8081
- **Database**: localhost:3306

## Service Details

### MariaDB
- **Port**: 3306
- **Database**: clinicdb
- **Root User**: root
- **Root Password**: root

### Auth Service
- **Port**: 8080
- **Database**: clinicdb
- **Application Name**: auth-service
- **Connected to**: MariaDB via hostname `mariadb`

### Submit Service
- **Port**: 8081
- **Database**: clinicdb
- **Application Name**: submit-service
- **Connected to**: MariaDB via hostname `mariadb`

## Common Commands

### View logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f submit-service
docker-compose logs -f auth-service
docker-compose logs -f mariadb
```

### Stop all services
```bash
docker-compose down
```

### Stop services and remove volumes (clean everything)
```bash
docker-compose down -v
```

### Rebuild images
```bash
docker-compose build --no-cache
```

### Restart a specific service
```bash
docker-compose restart submit-service
```

## Configuration

### Environment Variables

The application uses the following environment variables (configured in docker-compose.yml):

**Submit Service & Auth Service:**
- `SPRING_DATASOURCE_URL`: MariaDB connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`: MariaDB JDBC driver
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: Hibernate DDL strategy (update)
- `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT`: Hibernate dialect for MariaDB

To modify these, edit the `docker-compose.yml` file or use the `.env` file.

## Database Connection

From your local machine:
- **Host**: localhost
- **Port**: 3306
- **Database**: clinicdb
- **Username**: root
- **Password**: root

From within a Docker container:
- **Host**: mariadb
- **Port**: 3306
- **Database**: clinicdb
- **Username**: root
- **Password**: root

## Troubleshooting

### Services fail to start
Check the logs:
```bash
docker-compose logs
```

### Database connection refused
Wait for MariaDB to fully start before services connect. The `healthcheck` ensures this.

### Port already in use
If ports 8080, 8081, or 3306 are already in use, modify the port mappings in `docker-compose.yml`:
```yaml
ports:
  - "8080:8080"  # Change first number to use a different port
```

### Container memory issues
Increase Docker's memory allocation in Docker Desktop settings.

## Architecture

```
┌─────────────────────────────────────────┐
│      Docker Network (clinic-network)    │
├──────────────┬──────────────┬───────────┤
│              │              │           │
│  Auth        │   Submit     │  MariaDB  │
│  Service     │   Service    │           │
│  :8080       │   :8081      │   :3306   │
└──────────────┴──────────────┴───────────┘
```

## Development

To modify and rebuild services:

1. Make changes to your Spring Boot code
2. Rebuild the specific service:
   ```bash
   docker-compose build submit-service
   ```
3. Restart the service:
   ```bash
   docker-compose up -d submit-service
   ```

## Next Steps

1. Verify all services are running
2. Test the API endpoints
3. Check database connectivity
4. Deploy to your environment (development/staging/production)

For more information, see the individual service documentation.

