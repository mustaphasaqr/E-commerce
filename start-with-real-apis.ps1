# E-Commerce Platform - Production Startup Script
# Loads all real API credentials from .env file
# Usage: .\start-with-real-apis.ps1

Write-Host ""
Write-Host "🚀 Starting E-Commerce Platform with REAL API Integrations" -ForegroundColor Green
Write-Host "=" * 70 -ForegroundColor Gray
Write-Host ""

# Check if .env file exists
if (-not (Test-Path ".env")) {
    Write-Host "❌ ERROR: .env file not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Create .env file with your credentials:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "# Aramex Shipping" -ForegroundColor Cyan
    Write-Host "ARAMEX_USERNAME=your_username"
    Write-Host "ARAMEX_PASSWORD=your_password"
    Write-Host "ARAMEX_ACCOUNT_NUMBER=123456"
    Write-Host "ARAMEX_ACCOUNT_PIN=your_pin"
    Write-Host "ARAMEX_ACCOUNT_ENTITY=DXB"
    Write-Host "ARAMEX_BASE_URL=https://ws.dev.aramex.net/ShippingAPI.V2"
    Write-Host ""
    Write-Host "# Aramex Sender Address" -ForegroundColor Cyan
    Write-Host "ARAMEX_SENDER_NAME=Your Company"
    Write-Host "ARAMEX_SENDER_COMPANY=E-Commerce Platform"
    Write-Host "ARAMEX_SENDER_ADDRESS1=123 Street"
    Write-Host "ARAMEX_SENDER_CITY=Dubai"
    Write-Host "ARAMEX_SENDER_COUNTRY=AE"
    Write-Host "ARAMEX_SENDER_PHONE=+971501234567"
    Write-Host "ARAMEX_SENDER_EMAIL=warehouse@example.com"
    Write-Host ""
    Write-Host "# Accept Paymob Payment" -ForegroundColor Cyan
    Write-Host "ACCEPT_API_KEY=your_key"
    Write-Host "ACCEPT_INTEGRATION_ID=your_id"
    Write-Host "ACCEPT_BASE_URL=https://accept.paymob.com/api"
    Write-Host ""
    Write-Host "# SendGrid Email" -ForegroundColor Cyan
    Write-Host "SENDGRID_API_KEY=SG.your_key"
    Write-Host "SENDGRID_FROM_EMAIL=noreply@example.com"
    Write-Host "SENDGRID_FROM_NAME=E-Commerce Platform"
    Write-Host ""
    exit 1
}

Write-Host "📄 Loading credentials from .env file..." -ForegroundColor Cyan

# Load Aramex Shipping credentials
$env:ARAMEX_USERNAME = (Get-Content .env | Select-String "^ARAMEX_USERNAME=" | ForEach-Object { $_ -replace "ARAMEX_USERNAME=","" })
$env:ARAMEX_PASSWORD = (Get-Content .env | Select-String "^ARAMEX_PASSWORD=" | ForEach-Object { $_ -replace "ARAMEX_PASSWORD=","" })
$env:ARAMEX_ACCOUNT_NUMBER = (Get-Content .env | Select-String "^ARAMEX_ACCOUNT_NUMBER=" | ForEach-Object { $_ -replace "ARAMEX_ACCOUNT_NUMBER=","" })
$env:ARAMEX_ACCOUNT_PIN = (Get-Content .env | Select-String "^ARAMEX_ACCOUNT_PIN=" | ForEach-Object { $_ -replace "ARAMEX_ACCOUNT_PIN=","" })
$env:ARAMEX_ACCOUNT_ENTITY = (Get-Content .env | Select-String "^ARAMEX_ACCOUNT_ENTITY=" | ForEach-Object { $_ -replace "ARAMEX_ACCOUNT_ENTITY=","" })
$env:ARAMEX_BASE_URL = (Get-Content .env | Select-String "^ARAMEX_BASE_URL=" | ForEach-Object { $_ -replace "ARAMEX_BASE_URL=","" })

# Load Aramex Sender Address
$env:ARAMEX_SENDER_NAME = (Get-Content .env | Select-String "^ARAMEX_SENDER_NAME=" | ForEach-Object { $_ -replace "ARAMEX_SENDER_NAME=","" })
$env:ARAMEX_SENDER_COMPANY = (Get-Content .env | Select-String "^ARAMEX_SENDER_COMPANY=" | ForEach-Object { $_ -replace "ARAMEX_SENDER_COMPANY=","" })
$env:ARAMEX_SENDER_ADDRESS1 = (Get-Content .env | Select-String "^ARAMEX_SENDER_ADDRESS1=" | ForEach-Object { $_ -replace "ARAMEX_SENDER_ADDRESS1=","" })
$env:ARAMEX_SENDER_ADDRESS2 = (Get-Content .env | Select-String "^ARAMEX_SENDER_ADDRESS2=" | ForEach-Object { $_ -replace "ARAMEX_SENDER_ADDRESS2=","" })
$env:ARAMEX_SENDER_CITY = (Get-Content .env | Select-String "^ARAMEX_SENDER_CITY=" | ForEach-Object { $_ -replace "ARAMEX_SENDER_CITY=","" })
$env:ARAMEX_SENDER_STATE = (Get-Content .env | Select-String "^ARAMEX_SENDER_STATE=" | ForEach-Object { $_ -replace "ARAMEX_SENDER_STATE=","" })
$env:ARAMEX_SENDER_POSTAL_CODE = (Get-Content .env | Select-String "^ARAMEX_SENDER_POSTAL_CODE=" | ForEach-Object { $_ -replace "ARAMEX_SENDER_POSTAL_CODE=","" })
$env:ARAMEX_SENDER_COUNTRY = (Get-Content .env | Select-String "^ARAMEX_SENDER_COUNTRY=" | ForEach-Object { $_ -replace "ARAMEX_SENDER_COUNTRY=","" })
$env:ARAMEX_SENDER_PHONE = (Get-Content .env | Select-String "^ARAMEX_SENDER_PHONE=" | ForEach-Object { $_ -replace "ARAMEX_SENDER_PHONE=","" })
$env:ARAMEX_SENDER_EMAIL = (Get-Content .env | Select-String "^ARAMEX_SENDER_EMAIL=" | ForEach-Object { $_ -replace "ARAMEX_SENDER_EMAIL=","" })

# Load Accept Paymob credentials
$env:ACCEPT_API_KEY = (Get-Content .env | Select-String "^ACCEPT_API_KEY=" | ForEach-Object { $_ -replace "ACCEPT_API_KEY=","" })
$env:ACCEPT_INTEGRATION_ID = (Get-Content .env | Select-String "^ACCEPT_INTEGRATION_ID=" | ForEach-Object { $_ -replace "ACCEPT_INTEGRATION_ID=","" })
$env:ACCEPT_BASE_URL = (Get-Content .env | Select-String "^ACCEPT_BASE_URL=" | ForEach-Object { $_ -replace "ACCEPT_BASE_URL=","" })

# Load SendGrid credentials
$env:SENDGRID_API_KEY = (Get-Content .env | Select-String "^SENDGRID_API_KEY=" | ForEach-Object { $_ -replace "SENDGRID_API_KEY=","" })
$env:SENDGRID_FROM_EMAIL = (Get-Content .env | Select-String "^SENDGRID_FROM_EMAIL=" | ForEach-Object { $_ -replace "SENDGRID_FROM_EMAIL=","" })
$env:SENDGRID_FROM_NAME = (Get-Content .env | Select-String "^SENDGRID_FROM_NAME=" | ForEach-Object { $_ -replace "SENDGRID_FROM_NAME=","" })

# Set Java Home
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"

Write-Host ""
Write-Host "✅ Credentials Loaded:" -ForegroundColor Green
Write-Host ""

# Aramex Status
if ($env:ARAMEX_USERNAME -and $env:ARAMEX_PASSWORD -and $env:ARAMEX_ACCOUNT_NUMBER -and $env:ARAMEX_ACCOUNT_PIN) {
    Write-Host "   📦 Aramex Shipping: " -NoNewline -ForegroundColor Cyan
    Write-Host "REAL MODE" -ForegroundColor Green
    Write-Host "      Account: $env:ARAMEX_ACCOUNT_NUMBER" -ForegroundColor Gray
    Write-Host "      Entity: $env:ARAMEX_ACCOUNT_ENTITY" -ForegroundColor Gray
    if ($env:ARAMEX_SENDER_NAME -and $env:ARAMEX_SENDER_CITY) {
        Write-Host "      Sender: $env:ARAMEX_SENDER_COMPANY - $env:ARAMEX_SENDER_CITY, $env:ARAMEX_SENDER_COUNTRY" -ForegroundColor Gray
    }
} else {
    Write-Host "   📦 Aramex Shipping: " -NoNewline -ForegroundColor Cyan
    Write-Host "MOCK MODE (no credentials)" -ForegroundColor Yellow
}

# Accept Status
if ($env:ACCEPT_API_KEY -and $env:ACCEPT_INTEGRATION_ID) {
    Write-Host "   💳 Accept Paymob: " -NoNewline -ForegroundColor Cyan
    Write-Host "REAL MODE" -ForegroundColor Green
    Write-Host "      Integration ID: $env:ACCEPT_INTEGRATION_ID" -ForegroundColor Gray
} else {
    Write-Host "   💳 Accept Paymob: " -NoNewline -ForegroundColor Cyan
    Write-Host "MOCK MODE (no credentials)" -ForegroundColor Yellow
}

# SendGrid Status
if ($env:SENDGRID_API_KEY) {
    Write-Host "   📧 SendGrid Email: " -NoNewline -ForegroundColor Cyan
    Write-Host "REAL MODE" -ForegroundColor Green
    Write-Host "      From: $env:SENDGRID_FROM_NAME <$env:SENDGRID_FROM_EMAIL>" -ForegroundColor Gray
} else {
    Write-Host "   📧 SendGrid Email: " -NoNewline -ForegroundColor Cyan
    Write-Host "MOCK MODE (no credentials)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=" * 70 -ForegroundColor Gray
Write-Host ""
Write-Host "🚀 Starting Spring Boot application..." -ForegroundColor Cyan
Write-Host ""

# Start the application
mvn spring-boot:run -DskipTests

Write-Host ""
Write-Host "✅ Application stopped" -ForegroundColor Green
