# 🚀 Production-Ready Docker Setup

## What Changed?

### 1. ✅ Volume Mounting (Data Persistence)
**Before:** Data lost when containers deleted
**After:** Data survives container deletion, server restarts, redeployments

```bash
# Old containers (no volumes)
docker run mysql:8.0  # ← Data lost on deletion

# New setup (with volumes)
docker-compose up -d  # ← Data persists forever
```

**Volumes Created:**
- `mysql-data` → `/var/lib/mysql` (all database tables, indexes, data)
- `redis-data` → `/data` (all sessions, tokens, rate limit counters)

### 2. ✅ Redis Rate Limiting (Distributed Systems)
**Before:** `InMemoryLoginRateLimitPolicy` - ConcurrentHashMap (single server only)
**After:** `RedisLoginRateLimitPolicy` - Centralized in Redis (multi-server ready)

**Benefits:**
- Works with load balancers (Nginx, HAProxy)
- Persists across application restarts
- Shared rate limits across microservices
- Auto-cleanup via Redis TTL (30min user, 60min IP)

### 3. ✅ Production Security
- MySQL: Separate user account (`ecommerce_user`)
- Redis: Password protected (`ecommerce_redis_pass`)
- Health checks: Auto-restart unhealthy containers
- Networks: Isolated bridge network

---

## 🔄 Migration Steps

### Step 1: Stop Old Containers (Data Will Be Lost!)
```powershell
# WARNING: This deletes your current data!
# Export data first if needed:
docker exec ecommerce-mysql mysqldump -uroot -proot ecommerce > backup.sql

# Stop and remove old containers
docker stop ecommerce-mysql ecommerce-redis
docker rm ecommerce-mysql ecommerce-redis
```

### Step 2: Start Production Setup
```powershell
cd C:\Users\t-mattia\development\E-commerce

# Start with Docker Compose (creates volumes automatically)
docker-compose up -d

# Verify containers are running
docker-compose ps

# Check logs
docker-compose logs -f
```

### Step 3: Verify Volumes
```powershell
# List volumes
docker volume ls
# Expected output:
# e-commerce_mysql-data
# e-commerce_redis-data

# Inspect volume
docker volume inspect e-commerce_mysql-data
```

### Step 4: Test Application
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
mvn spring-boot:run

# Application will connect to password-protected Redis
```

---

## 📊 Docker Compose Commands

```powershell
# Start all services
docker-compose up -d

# Stop all services (data persists)
docker-compose down

# Stop and DELETE volumes (⚠️ DATA LOSS!)
docker-compose down -v

# View logs
docker-compose logs mysql
docker-compose logs redis
docker-compose logs -f  # Follow all logs

# Restart services
docker-compose restart

# Check health status
docker-compose ps
```

---

## 🔍 Verify Data Persistence

### Test MySQL Volume
```powershell
# Create test data
docker exec ecommerce-mysql mysql -uroot -proot ecommerce -e "CREATE TABLE test_table (id INT);"

# Stop and restart container
docker-compose restart mysql

# Check data still exists
docker exec ecommerce-mysql mysql -uroot -proot ecommerce -e "SHOW TABLES;"
# ✅ test_table should still be there!
```

### Test Redis Volume
```powershell
# Set test key
docker exec ecommerce-redis redis-cli -a ecommerce_redis_pass SET test_key "test_value"

# Restart container
docker-compose restart redis

# Check key persists
docker exec ecommerce-redis redis-cli -a ecommerce_redis_pass GET test_key
# ✅ Should return "test_value"
```

---

## 🗄️ Database Access

### MySQL (New Credentials)
```bash
# Root access
docker exec -it ecommerce-mysql mysql -uroot -proot ecommerce

# Application user (limited permissions)
docker exec -it ecommerce-mysql mysql -uecommerce_user -pecommerce_pass ecommerce
```

### Redis (Password Protected)
```bash
# CLI access
docker exec -it ecommerce-redis redis-cli -a ecommerce_redis_pass

# Check rate limit keys
docker exec ecommerce-redis redis-cli -a ecommerce_redis_pass KEYS "rate_limit:*"

# Check all keys
docker exec ecommerce-redis redis-cli -a ecommerce_redis_pass KEYS "*"
```

---

## 🚨 Important Notes

### Volume Backup
```powershell
# Backup MySQL volume
docker exec ecommerce-mysql mysqldump -uroot -proot --all-databases > mysql_backup_$(Get-Date -Format 'yyyy-MM-dd').sql

# Backup Redis volume
docker exec ecommerce-redis redis-cli -a ecommerce_redis_pass --rdb /data/backup.rdb
```

### Volume Location
- **Windows:** `C:\ProgramData\docker\volumes\`
- **Linux:** `/var/lib/docker/volumes/`
- **Mac:** `~/Library/Containers/com.docker.docker/Data/`

### Production Deployment
When deploying to cloud (AWS, Azure, GCP):
1. Use managed services (RDS for MySQL, ElastiCache for Redis)
2. Or: Mount volumes to cloud storage (EBS, Azure Disk, Persistent Disk)
3. Implement automated backups
4. Set up monitoring and alerts

---

## ✅ What's Production-Ready Now

- ✅ Data persistence (volumes)
- ✅ Redis rate limiting (distributed)
- ✅ Password protection (MySQL + Redis)
- ✅ Health checks (auto-restart)
- ✅ Network isolation
- ✅ Restart policies
- ✅ Redis AOF persistence (append-only file)

**Next production steps (later):**
- SSL/TLS certificates
- Database replication
- Redis clustering
- Monitoring (Prometheus + Grafana)
- Logging (ELK stack)
- CI/CD pipeline
