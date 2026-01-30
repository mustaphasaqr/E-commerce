# Production Migration Script
# Run this to migrate from old setup to production-ready setup

Write-Host "🚀 Migrating to Production-Ready Setup..." -ForegroundColor Green
Write-Host ""

# Step 1: Backup existing data
Write-Host "📦 Step 1: Backing up existing data..." -ForegroundColor Yellow
$backupDate = Get-Date -Format "yyyy-MM-dd-HHmmss"
$backupDir = "backup_$backupDate"
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

# Backup MySQL
Write-Host "  → Backing up MySQL database..."
docker exec ecommerce-mysql mysqldump -uroot -proot --all-databases > "$backupDir/mysql_backup.sql" 2>$null
if ($?) {
    Write-Host "  ✅ MySQL backup saved to $backupDir/mysql_backup.sql" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  MySQL backup failed (container may not exist)" -ForegroundColor Yellow
}

# Backup Redis (if data exists)
Write-Host "  → Backing up Redis data..."
docker exec ecommerce-redis redis-cli SAVE 2>$null
if ($?) {
    Write-Host "  ✅ Redis backup completed" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Redis backup failed (container may not exist)" -ForegroundColor Yellow
}

Write-Host ""

# Step 2: Stop old containers
Write-Host "🛑 Step 2: Stopping old containers..." -ForegroundColor Yellow
docker stop ecommerce-mysql ecommerce-redis 2>$null
docker rm ecommerce-mysql ecommerce-redis 2>$null
Write-Host "  ✅ Old containers removed" -ForegroundColor Green
Write-Host ""

# Step 3: Start production setup
Write-Host "🐳 Step 3: Starting production Docker Compose..." -ForegroundColor Yellow
docker-compose up -d

if ($?) {
    Write-Host "  ✅ Production containers started!" -ForegroundColor Green
} else {
    Write-Host "  ❌ Failed to start containers" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 4: Wait for services to be healthy
Write-Host "⏳ Step 4: Waiting for services to be healthy..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

$maxRetries = 30
$retry = 0
while ($retry -lt $maxRetries) {
    $mysqlHealth = docker inspect ecommerce-mysql --format='{{.State.Health.Status}}' 2>$null
    $redisHealth = docker inspect ecommerce-redis --format='{{.State.Health.Status}}' 2>$null
    
    if ($mysqlHealth -eq "healthy" -and $redisHealth -eq "healthy") {
        Write-Host "  ✅ All services healthy!" -ForegroundColor Green
        break
    }
    
    Write-Host "  ⏳ MySQL: $mysqlHealth | Redis: $redisHealth (retry $retry/$maxRetries)"
    Start-Sleep -Seconds 2
    $retry++
}

if ($retry -eq $maxRetries) {
    Write-Host "  ⚠️  Services took longer than expected to start" -ForegroundColor Yellow
}
Write-Host ""

# Step 5: Restore data (if backup exists)
if (Test-Path "$backupDir/mysql_backup.sql") {
    Write-Host "📥 Step 5: Restoring MySQL data..." -ForegroundColor Yellow
    Get-Content "$backupDir/mysql_backup.sql" | docker exec -i ecommerce-mysql mysql -uroot -proot 2>$null
    if ($?) {
        Write-Host "  ✅ MySQL data restored!" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  MySQL restore had issues (may be normal if database was empty)" -ForegroundColor Yellow
    }
} else {
    Write-Host "📥 Step 5: No backup to restore (fresh start)" -ForegroundColor Yellow
}
Write-Host ""

# Step 6: Verify setup
Write-Host "🔍 Step 6: Verifying setup..." -ForegroundColor Yellow

# Check volumes
Write-Host "  Volumes created:"
docker volume ls | Select-String "e-commerce"

# Check containers
Write-Host "`n  Container status:"
docker-compose ps

Write-Host ""
Write-Host "✅ Production setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Next steps:" -ForegroundColor Cyan
Write-Host "  1. Update application.properties with Redis password (already done)"
Write-Host "  2. Start application: mvn spring-boot:run"
Write-Host "  3. Test rate limiting with Redis"
Write-Host ""
Write-Host "📚 Useful commands:" -ForegroundColor Cyan
Write-Host "  docker-compose ps              # Check status"
Write-Host "  docker-compose logs -f         # View logs"
Write-Host "  docker-compose down            # Stop (keeps data)"
Write-Host "  docker-compose restart         # Restart services"
Write-Host ""
Write-Host "💾 Backup saved to: $backupDir" -ForegroundColor Cyan
