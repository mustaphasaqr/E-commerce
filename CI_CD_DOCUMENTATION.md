# CI/CD Pipeline Documentation

## Overview

This project uses **GitHub Actions** with a **3-stage CI/CD pipeline**:

```
┌─────────────────┐
│  Stage 1: BUILD │
│  - Maven test   │
│  - Docker build │
│  - Push to GHCR │
└────────┬────────┘
         │
         ▼
┌──────────────────────────┐
│  Stage 2: TEST & QUALITY │
│  - Security scan        │
│  - Code quality checks  │
│  - Test reports         │
└────────┬─────────────────┘
         │
         ▼
┌──────────────────────────┐
│ Stage 3: DEPLOY STAGING  │
│ ⚠️  MANUAL APPROVAL      │
│  - Deploy to staging    │
│  - Health checks        │
│  - Slack notification   │
└────────┬─────────────────┘
         │
         ▼
┌──────────────────────────┐
│ Stage 4: DEPLOY PROD     │
│ ⚠️  MANUAL APPROVAL      │
│  - Deploy to production │
│  - Health checks        │
│  - Notifications        │
└──────────────────────────┘
```

## Pipeline Stages

### Stage 1: Build
**Triggered On:** Push to `main` or `appmod/java-upgrade-20251219153529`

**What it does:**
- ✅ Checks out code
- ✅ Sets up Java 21
- ✅ Runs: `mvn clean test -B`
- ✅ Builds project: `mvn clean package -DskipTests`
- ✅ Builds Docker image
- ✅ Pushes to GitHub Container Registry (GHCR)

**Success Outcome:** Docker image available at `ghcr.io/mustaphasaqr/e-commerce/ecommerce-app:latest`

### Stage 2: Test & Quality
**Triggered On:** After Build succeeds

**What it does:**
- ✅ Runs Maven tests again
- ✅ Security vulnerability scan (OWASP dependency-check)
- ✅ Code quality analysis
- ✅ Generates coverage reports
- ✅ Uploads test artifacts

### Stage 3: Deploy to Staging
**Triggered On:** After Test succeeds (main branch only)

**⚠️  REQUIRES MANUAL APPROVAL**

**What it does:**
- ✅ Deploys using `docker-compose.staging.yml`
- ✅ Uses different ports (8081, 3307, 6380, etc.)
- ✅ Runs health checks
- ✅ Notifies via Slack (optional)

**How to Approve:**
1. Go to: https://github.com/mustaphasaqr/e-commerce/actions
2. Click the workflow run
3. Click "Deploy to Staging" → "Approve and deploy"

### Stage 4: Deploy to Production (Optional)
**Triggered On:** After Staging succeeds (main branch only)

**⚠️  REQUIRES EXPLICIT MANUAL APPROVAL**

**What it does:**
- ✅ Deploys to production environment
- ✅ Runs production health checks
- ✅ Sends deployment notifications

## Environment Configuration

### Staging Environment

**Ports (different from production):**
- App: `8081` (not 8080)
- MySQL: `3307` (not 3306)
- Redis: `6380` (not 6379)
- Prometheus: `9091` (not 9090)
- Grafana: `3001` (not 3000)
- Zipkin: `9412` (not 9411)

**Database:** `ecommerce_staging` (isolated from production)

**Features:**
- More verbose logging (DEBUG level)
- In-memory test data
- Monitoring with Prometheus/Grafana

**Start Staging Locally:**
```bash
docker-compose -f docker-compose.staging.yml up -d
```

### Production Environment

**Ports:**
- App: `8080` (production)
- MySQL: `3306` (production)
- Redis: `6379` (production)
- All other services at standard ports

**Database:** `ecommerce` (production data)

**Features:**
- Optimized for performance
- SSL/TLS enabled
- Strict validation
- Flyway migrations managed

**Start Production Locally:**
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## GitHub Secrets Required

### For CI/CD to Work:

No additional secrets needed! GitHub automatically provides:
- `GITHUB_TOKEN` - for Docker registry access
- `github.actor` - your username
- `github.repository` - your repo path

### Optional Additions:

Add these to `Settings → Secrets and variables → Actions`:

```bash
SLACK_WEBHOOK_URL     # For Slack notifications
SONAR_TOKEN          # For SonarQube analysis
DOCKER_HUB_USERNAME  # If pushing to Docker Hub
DOCKER_HUB_TOKEN     # If pushing to Docker Hub
```

## Viewing CI/CD Status

### GitHub Actions Dashboard:
```
https://github.com/mustaphasaqr/e-commerce/actions
```

### Pipeline Runs:
- Click any branch/commit
- See real-time logs
- Approve manual deployments

## Troubleshooting

### Build Fails
**Check:**
```bash
# Run locally
mvn clean test -B
mvn clean package
docker build -t ecommerce-app:test .
```

### Test Fails
```bash
# Run tests locally
mvn test -B

# View test reports
open target/site/jacoco/index.html
```

### Docker Push Fails
**Check:**
1. Verify `docker-compose.yml` exists
2. Check Maven build succeeds
3. Verify GitHub Container Registry access

### Deployment Hangs
**Check:**
- Staging environment health: `curl http://localhost:8081/actuator/health`
- Database running: `docker ps | grep mysql`
- Redis running: `docker ps | grep redis`

## Common Commands

### View Pipeline Logs:
```bash
git log --oneline -10
```

### Rebuild Locally:
```bash
./mvnw clean test -B
./mvnw clean package -DskipTests -B
docker build -t ecommerce-app:local .
```

### Test Staging Locally:
```bash
docker-compose -f docker-compose.staging.yml up -d
curl http://localhost:8081/actuator/health
```

### Cleanup:
```bash
# Stop staging
docker-compose -f docker-compose.staging.yml down -v

# Stop production
docker-compose -f docker-compose.prod.yml down -v

# Clean Docker images
docker rmi ghcr.io/mustaphasaqr/e-commerce/ecommerce-app
```

## Next Steps

1. **Commit this CI/CD setup:**
   ```bash
   git add .github/ docker-compose.staging.yml
   git commit -m "ci: Add GitHub Actions CI/CD pipeline with staging deployments"
   git push origin appmod/java-upgrade-20251219153529
   ```

2. **Create Pull Request:**
   - Go to: https://github.com/mustaphasaqr/e-commerce/pulls
   - Merge `appmod/java-upgrade-20251219153529` → `main`
   - This triggers the pipeline!

3. **Test the Pipeline:**
   - Watch the build/test/deploy stages
   - Approve staging deployment manually
   - Verify staging is accessible

4. **Monitor Production:**
   - Set up Slack notifications (optional)
   - Configure environments in Settings
   - Set approval rules for production

## CI/CD Status Badge

Add to your README.md:

```markdown
![CI/CD Pipeline](https://github.com/mustaphasaqr/e-commerce/actions/workflows/ci-cd.yml/badge.svg)
```

## Support

For issues or questions:
- Check GitHub Actions logs: https://github.com/mustaphasaqr/e-commerce/actions
- Review this documentation
- Check Docker Compose files for configuration
