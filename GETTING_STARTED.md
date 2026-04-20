# 🚀 GET STARTED IN 2 MINUTES

## For First Time Users

### Step 1: Open Command Prompt or PowerShell
- Windows: Press `Win + R`, type `cmd` or `powershell`, press Enter
- Or search for "Command Prompt" or "PowerShell" in Start menu

### Step 2: Navigate to Project
```bash
cd C:\Users\Hanin1\Dropbox\PC\Downloads\team-a
```

### Step 3: Start Services
```bash
start-services.bat
```

Or if using PowerShell:
```bash
.\start-services.ps1
```

Or if you prefer direct Docker:
```bash
docker-compose up -d
```

### Step 4: Wait & Verify
Wait 10-30 seconds, then open a new terminal and run:
```bash
docker-compose ps
```

You should see 3 containers:
- clinic-mariadb (healthy)
- clinic-auth-service (running)
- clinic-submit-service (running)

### Step 5: Access Services

Open your browser:
- **Auth Service**: http://localhost:8080
- **Submit Service**: http://localhost:8081/api/submit

### Done! ✅

Your services are running!

---

## Useful Commands

### Stop Services
```bash
docker-compose down
```

### View Logs
```bash
docker-compose logs -f
```

### Check Status Anytime
```bash
docker-compose ps
```

---

## Need Help?

1. **Quick reference**: See `DOCKER_QUICK_REFERENCE.md`
2. **Detailed guide**: See `DOCKER_README.md`
3. **Troubleshooting**: See `DOCKER_SETUP.md` (last section)
4. **Check logs**: Run `docker-compose logs`

---

## That's It!

You're all set. Services are running and ready to use. 🎉

