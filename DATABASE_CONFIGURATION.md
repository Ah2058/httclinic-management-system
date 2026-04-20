# ✅ SEPARATE DATABASES CONFIGURATION - COMPLETE

## Status: FIXED ✔️

Your microservices now have **separate databases** for proper microservices isolation.

---

## Database Architecture

```
┌─────────────────────────────────────────────────────────┐
│             MariaDB Server (localhost:3306)             │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────────────┐  ┌──────────────────────────┐ │
│  │   auth_db            │  │   clinicdb               │ │
│  │  (Auth Service)      │  │  (Submit Service)        │ │
│  │                      │  │                          │ │
│  │  Tables:             │  │  Tables:                 │ │
│  │  - users             │  │  - patient_form          │ │
│  │  - roles             │  │  - patient_form_symptoms │ │
│  │  - user_roles        │  │  - patient_form_allergies│ │
│  │                      │  │  - patient_form_medications
│  │                      │  │  - patient_form_pre_existing
│  └──────────────────────┘  └──────────────────────────┘ │
│                                                          │
└─────────────────────────────────────────────────────────┘
         ▲                              ▲
         │                              │
    8080:8080                       8081:8080
         │                              │
    ┌────┴──────┐                ┌─────┴────┐
    │  Auth     │                │  Submit  │
    │  Service  │                │  Service │
    └───────────┘                └──────────┘
```

---

## Database Configuration

### Auth Service
- **Database**: `auth_db`
- **Docker Port**: 3306
- **Host Port**: 3306
- **Username**: root
- **Password**: root
- **Connection String**: `jdbc:mariadb://mariadb:3306/auth_db`

**Tables:**
- `users` - User credentials
- `roles` - Role definitions
- `user_roles` - User-Role mapping

### Submit Service
- **Database**: `clinicdb`
- **Docker Port**: 3306
- **Host Port**: 3306
- **Username**: root
- **Password**: root
- **Connection String**: `jdbc:mariadb://mariadb:3306/clinicdb`

**Tables:**
- `patient_form` - Patient form records
- `patient_form_symptoms` - Symptoms list for each patient
- `patient_form_allergies` - Allergies list for each patient
- `patient_form_medications` - Medications list for each patient
- `patient_form_pre_existing_conditions` - Pre-existing conditions list

---

## Files Modified/Created

### Created Files:
- ✅ `init-databases.sql` - Initialization script to create both databases

### Modified Files:
- ✅ `docker-compose.yml` - Updated MariaDB service with database initialization
  - Added volume mount for `init-databases.sql`
  - Updated auth-service to use `auth_db`
  - Submit-service already uses `clinicdb`

---

## Verification

### Check Databases
```bash
docker exec clinic-mariadb mariadb -u root -proot -e "SHOW DATABASES;"
```

**Output:**
```
auth_db
clinicdb
information_schema
mysql
performance_schema
sys
```

### Check Auth Service Tables
```bash
docker exec clinic-mariadb mariadb -u root -proot -D auth_db -e "SHOW TABLES;"
```

**Output:**
```
roles
user_roles
users
```

### Check Submit Service Tables
```bash
docker exec clinic-mariadb mariadb -u root -proot -D clinicdb -e "SHOW TABLES;"
```

**Output:**
```
patient_form
patient_form_allergies
patient_form_medications
patient_form_pre_existing_conditions
patient_form_symptoms
```

---

## Access Databases via GUI

### Using DBeaver or MySQL Workbench:

**Connection 1 - Auth Database:**
- **Host**: localhost
- **Port**: 3306
- **Username**: root
- **Password**: root
- **Database**: auth_db

**Connection 2 - Patient Database:**
- **Host**: localhost
- **Port**: 3306
- **Username**: root
- **Password**: root
- **Database**: clinicdb

---

## Command Line Access

### Access Auth Database
```bash
mysql -h localhost -P 3306 -u root -p auth_db
# Password: root
```

### Access Patient Database
```bash
mysql -h localhost -P 3306 -u root -p clinicdb
# Password: root
```

### Access from within Docker
```bash
docker exec -it clinic-mariadb mariadb -u root -p clinicdb
# Password: root
```

---

## Microservices Isolation Benefits

✅ **Separation of Concerns** - Auth data separate from patient data
✅ **Database Per Service Pattern** - Each microservice owns its database
✅ **Independent Scaling** - Each database can be scaled independently
✅ **Independent Backups** - Backup each database separately
✅ **Independent Migrations** - Schema changes don't affect other services
✅ **Security** - Can have different access controls per database
✅ **Multi-Database Deployment** - Can move databases to different servers

---

## Docker Compose Changes

**Before:**
- Single `clinicdb` database
- Both services used same database
- No database initialization script

**After:**
- Two separate databases: `auth_db` and `clinicdb`
- Auth service uses `auth_db`
- Submit service uses `clinicdb`
- Initialization script creates both databases automatically

---

## Connection Status Check

### View All Connections
```bash
docker exec clinic-mariadb mariadb -u root -proot -e "SHOW PROCESSLIST;"
```

### Check Auth Service Connection
```bash
docker logs clinic-auth-service | grep "auth_db"
```

**Expected Output:**
```
Database JDBC URL [jdbc:mariadb://mariadb/auth_db?user=root&password=***]
```

### Check Submit Service Connection
```bash
docker logs clinic-submit-service | grep "clinicdb"
```

**Expected Output:**
```
Database JDBC URL [jdbc:mariadb://mariadb/clinicdb?user=root&password=***]
```

---

## Next Steps

✅ Services are configured with separate databases
✅ Both databases are created and initialized
✅ All tables are created automatically by Hibernate
✅ Ready for testing!

### Test the Complete Workflow:

1. **Login**: `POST http://localhost:8080/api/auth/login`
2. **Submit Form**: `POST http://localhost:8081/api/submit/api/forms`
3. **Get Forms**: `GET http://localhost:8081/api/submit/api/forms` (with JWT)
4. **Update Form**: `PATCH http://localhost:8081/api/submit/api/forms/{id}/admin` (with JWT)

---

## Troubleshooting

### If databases are not created:
```bash
# Restart services
docker-compose down
docker-compose up -d
```

### If services can't connect to database:
```bash
# Check database logs
docker logs clinic-mariadb

# Verify database is healthy
docker ps | grep mariadb
```

### If tables are missing:
```bash
# Restart services (Hibernate will recreate tables)
docker-compose restart clinic-auth-service
docker-compose restart clinic-submit-service
```


