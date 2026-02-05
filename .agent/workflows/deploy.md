---
description: Redeploy application with latest code (preserves database)
---

# Deploy Application

This workflow redeploys the JobCompass application with the latest code changes while **preserving all existing data** (jobs, configuration, etc.).

## When to Use
- After making code changes to backend services or frontend
- Updating dependencies or fixing bugs
- Normal application updates

## Steps

// turbo
1. Run Backend Tests (Spring Boot)
```bash
cd /Users/palrajjayaraj/Documents/GitHub/HobbyProjects/JobCompass/sourceCode/job-compass-app
docker run --rm -v $(pwd):/app -v $HOME/.m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-21 mvn test
```

// turbo
2. Run Frontend Tests (Svelte/Vitest)
```bash
cd /Users/palrajjayaraj/Documents/GitHub/HobbyProjects/JobCompass/sourceCode/job-compass-app/web-ui-svelte
docker run --rm -v $(pwd):/app -w /app node:20-alpine /bin/sh -c "npm ci && npm test -- --run"
```

// turbo
3. Stop all running containers (keeps volumes intact)
```bash
cd /Users/palrajjayaraj/Documents/GitHub/HobbyProjects/JobCompass/sourceCode/job-compass-app
docker-compose down
```

// turbo
2. Rebuild all application images with latest code
```bash
docker-compose build --no-cache
```

// turbo
3. Start all services with existing data
```bash
docker-compose up -d
```

// turbo
4. Verify services are running
```bash
sleep 10
docker-compose ps
```

// turbo
5. Check job count (should match previous count)
```bash
curl -s http://localhost:8081/api/jobs | jq 'length'
```

## Expected Result
- ✅ All services restart with new code
- ✅ Database preserves all existing jobs
- ✅ No data loss
- ✅ UI accessible at https://localhost:8085

## Notes
- The `-v` flag is **NOT** used with `docker-compose down` to preserve volumes
- Build cache is cleared with `--no-cache` to ensure fresh builds
- Data volumes (`postgres-data`, `kafka-data`, etc.) persist across deployments
