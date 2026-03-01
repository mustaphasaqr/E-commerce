# Load environment variables from .env file and start the application
Write-Host "Loading environment variables from .env file..." -ForegroundColor Cyan

$envFile = ".env"
if (-not (Test-Path $envFile)) {
    Write-Host "ERROR: .env file not found!" -ForegroundColor Red
    Write-Host "Copy .env.example to .env and fill in your credentials" -ForegroundColor Yellow
    exit 1
}

# Read .env file and set environment variables
Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        Set-Item -Path "env:$key" -Value $value
        Write-Host "✓ Set $key" -ForegroundColor Green
    }
}

Write-Host "`nStarting application with loaded environment variables..." -ForegroundColor Cyan
Write-Host ""

# Start the application
mvn spring-boot:run
