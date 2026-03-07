# Docker Build and Run Script for E-commerce Application
# This script builds the application and starts all services

Write-Host "🚀 Building and Starting E-commerce Application with Docker" -ForegroundColor Green
Write-Host "=" * 60 -ForegroundColor Yellow

# Function to check if command succeeded
function Test-LastCommand {
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Command failed with exit code $LASTEXITCODE" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

# Stop and remove existing containers
Write-Host "`n🛑 Stopping existing containers..." -ForegroundColor Yellow
docker-compose down --remove-orphans
Test-LastCommand

# Build the application image
Write-Host "`n🔨 Building Spring Boot application..." -ForegroundColor Yellow
docker-compose build --no-cache app
Test-LastCommand

# Start all services
Write-Host "`n🚀 Starting all services..." -ForegroundColor Yellow
Write-Host "This may take a few minutes for the first time..." -ForegroundColor Cyan
docker-compose up -d
Test-LastCommand

# Wait for services to be healthy
Write-Host "`n⏳ Waiting for services to be healthy..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Check service status
Write-Host "`n📊 Service Status:" -ForegroundColor Green
docker-compose ps

# Check application health
Write-Host "`n🏥 Application Health Check:" -ForegroundColor Green
try {
    $health = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 10
    if ($health.StatusCode -eq 200) {
        Write-Host "✅ Application is healthy!" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Application health check returned status $($health.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Application health check failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 Docker setup complete!" -ForegroundColor Green
Write-Host "`n📋 Access URLs:" -ForegroundColor Cyan
Write-Host "  🌐 Application: http://localhost:8080" -ForegroundColor White
Write-Host "  📊 Prometheus:  http://localhost:9090" -ForegroundColor White
Write-Host "  📈 Grafana:     http://localhost:3000 (admin/admin)" -ForegroundColor White
Write-Host "  🔍 Zipkin:      http://localhost:9411" -ForegroundColor White
Write-Host "  🗄️  MySQL:       localhost:3306 (user: ecommerce_user, pass: ecommerce_pass)" -ForegroundColor White
Write-Host "  🔴 Redis:       localhost:6379 (pass: ecommerce_redis_pass)" -ForegroundColor White

Write-Host "`n📝 Useful commands:" -ForegroundColor Cyan
Write-Host "  docker-compose logs -f app          # View application logs" -ForegroundColor White
Write-Host "  docker-compose logs -f mysql        # View MySQL logs" -ForegroundColor White
Write-Host "  docker-compose down                 # Stop all services" -ForegroundColor White
Write-Host "  docker-compose restart app          # Restart application only" -ForegroundColor White

Write-Host "`n✨ Happy coding!" -ForegroundColor Green