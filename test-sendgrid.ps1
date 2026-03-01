# Test SendGrid Email Integration
# This script starts the app with SendGrid configured and tests email sending

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "SendGrid Email Test" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Set environment variables
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:SENDGRID_API_KEY = "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"
$env:SENDGRID_FROM_EMAIL = "mustaphaosamasaqr@gmail.com"
$env:SENDGRID_FROM_NAME = "E-commerce Platform"

Write-Host "✅ Environment variables set:" -ForegroundColor Green
Write-Host "   JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan
Write-Host "   SENDGRID_API_KEY: $($env:SENDGRID_API_KEY.Substring(0,20))..." -ForegroundColor Cyan
Write-Host "   SENDGRID_FROM_EMAIL: $env:SENDGRID_FROM_EMAIL" -ForegroundColor Cyan
Write-Host ""

Write-Host "🚀 Starting Spring Boot application..." -ForegroundColor Yellow
Write-Host "   (This will take 30-60 seconds)" -ForegroundColor Gray
Write-Host ""

# Start Maven (this will blockand show output)
mvn spring-boot:run -e
