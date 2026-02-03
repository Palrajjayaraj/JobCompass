---
description: Full system reset - wipes all data and rebuilds from scratch
---

# Full System Reset

This workflow completely resets the JobCompass application, **wiping all data** including the job database, Kafka messages, and configuration.

## ⚠️ WARNING
**This will DELETE all stored jobs and data!** Only use this when you explicitly want to start fresh.

## When to Use
- Development reset to clean state
- Testing from scratch
- Corrupted database that needs to be rebuilt
- Explicitly requested full wipe

## Steps

1. Stop all containers and remove all volumes (⚠️ DATA LOSS)
```bash
cd /Users/palrajjayaraj/Documents/GitHub/HobbyProjects/JobCompass/sourceCode/job-compass-app
docker-compose down -v
```

2. Rebuild all images from scratch
```bash
docker-compose build --no-cache
```

3. Start all services with fresh volumes
```bash
docker-compose up -d
```

4. Verify services are running
```bash
sleep 10
docker-compose ps
```

5. Confirm database is empty
```bash
curl -s http://localhost:8081/api/jobs | jq 'length'
# Should return: 0
```

## Expected Result
- ✅ All services restart with new code
- ⚠️ Database is empty (0 jobs)
- ⚠️ All previous data is lost
- ✅ UI accessible at https://localhost:8085

## Notes
- The `-v` flag with `docker-compose down` removes ALL volumes
- This includes: `postgres-data`, `kafka-data`, `zookeeper-data`, `zookeeper-log`
- You will need to trigger new scrapes to populate the database
- **Cannot be undone** unless you have a backup
