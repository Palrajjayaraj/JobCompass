---
description: Quick restart of services (no rebuild, preserves data)
---

# Restart Services

This workflow quickly restarts all JobCompass services without rebuilding containers. Use this for quick restarts or configuration changes.

## When to Use
- Services are unresponsive
- After environment variable changes
- Quick restart without code changes
- Container health check failures

## Steps

// turbo-all

1. Stop all running containers
```bash
cd /Users/palrajjayaraj/Documents/GitHub/HobbyProjects/JobCompass/sourceCode/job-compass-app
docker-compose stop
```

2. Start all services (reuses existing containers)
```bash
docker-compose start
```

3. Verify services are running
```bash
sleep 5
docker-compose ps
```

4. Check health endpoints
```bash
curl -s http://localhost:8081/api/jobs | jq 'length'
curl -s https://localhost:8085 -k | head -n 20
```

## Alternative: Restart Specific Service

To restart only one service (e.g., scraper-service):
```bash
docker-compose restart scraper-service
```

## Expected Result
- ✅ Services restart quickly (no rebuild)
- ✅ All data preserved
- ✅ Same container images used
- ⏱️ Faster than full redeploy (~30 seconds vs ~2 minutes)

## Notes
- No rebuild occurs - uses cached containers
- Faster than `deploy` workflow
- Does not pick up code changes (use `deploy` for that)
