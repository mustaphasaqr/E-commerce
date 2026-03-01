# Start E-commerce App as Detached Process
# This allows VS Code terminal commands to run WITHOUT terminating the app

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Starting E-commerce Application" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Clear Redis rate limits first
Write-Host "[1/4] Clearing Redis rate limits..." -ForegroundColor Yellow
try {
    docker exec ecommerce-redis redis-cli -a ecommerce_redis_pass FLUSHDB | Out-Null
    Write-Host "[SUCCESS] Redis cleared" -ForegroundColor Green
} catch {
    Write-Host "[WARNING] Could not clear Redis: $_" -ForegroundColor Yellow
}
Write-Host ""

# Set Java home
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
Write-Host "[2/4] Using Java: $env:JAVA_HOME" -ForegroundColor Yellow

# Set SendGrid environment variables
$env:SENDGRID_API_KEY = "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"
$env:SENDGRID_FROM_EMAIL = "mustaphaosamasaqr@gmail.com"
$env:SENDGRID_FROM_NAME = "E-commerce Platform"
Write-Host "[INFO] SendGrid configured: $env:SENDGRID_FROM_EMAIL" -ForegroundColor Cyan

# Set Accept (Paymob) environment variables
$env:ACCEPT_API_KEY = (Get-Content .env | Select-String "ACCEPT_API_KEY" | ForEach-Object {$_ -replace "ACCEPT_API_KEY=",""})
$env:ACCEPT_INTEGRATION_ID = (Get-Content .env | Select-String "ACCEPT_INTEGRATION_ID" | ForEach-Object {$_ -replace "ACCEPT_INTEGRATION_ID=",""})
$env:ACCEPT_BASE_URL = (Get-Content .env | Select-String "ACCEPT_BASE_URL" | ForEach-Object {$_ -replace "ACCEPT_BASE_URL=",""})
Write-Host "[INFO] Accept (Paymob) configured: Integration ID $env:ACCEPT_INTEGRATION_ID" -ForegroundColor Cyan
Write-Host ""

# Kill any existing Java processes running Spring Boot
Write-Host "[3/4] Checking for existing Spring Boot processes..." -ForegroundColor Yellow
$existingJava = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*spring-boot*" -or $_.CommandLine -like "*ECommerceApplication*"
}
if ($existingJava) {
    Write-Host "Found $($existingJava.Count) existing process(es). Stopping..." -ForegroundColor Yellow
    $existingJava | Stop-Process -Force
    Start-Sleep -Seconds 2
    Write-Host "[SUCCESS] Old processes stopped" -ForegroundColor Green
} else {
    Write-Host "[INFO] No existing processes found" -ForegroundColor Gray
}
Write-Host ""

# Start Maven as detached process
Write-Host "[4/4] Starting Spring Boot as detached process..." -ForegroundColor Yellow
Write-Host "[INFO] App will run in background - check logs in app-output.log" -ForegroundColor Gray
Write-Host ""

# Create log file path
$logFile = Join-Path (Get-Location) "app-output.log"
if (Test-Path $logFile) { Remove-Item $logFile -Force }

# Start Maven in a completely separate process (not tied to this terminal)
$processInfo = New-Object System.Diagnostics.ProcessStartInfo
$processInfo.FileName = "cmd.exe"
$processInfo.Arguments = "/c `"set `"JAVA_HOME=$env:JAVA_HOME`" && mvn spring-boot:run -e -X > `"$logFile`" 2>&1`""
$processInfo.UseShellExecute = $false
$processInfo.CreateNoWindow = $true
$processInfo.WorkingDirectory = Get-Location
$processInfo.EnvironmentVariables["JAVA_HOME"] = $env:JAVA_HOME
$processInfo.EnvironmentVariables["SENDGRID_API_KEY"] = $env:SENDGRID_API_KEY
$processInfo.EnvironmentVariables["SENDGRID_FROM_EMAIL"] = $env:SENDGRID_FROM_EMAIL
$processInfo.EnvironmentVariables["SENDGRID_FROM_NAME"] = $env:SENDGRID_FROM_NAME
$processInfo.EnvironmentVariables["ACCEPT_API_KEY"] = $env:ACCEPT_API_KEY
$processInfo.EnvironmentVariables["ACCEPT_INTEGRATION_ID"] = $env:ACCEPT_INTEGRATION_ID
$processInfo.EnvironmentVariables["ACCEPT_BASE_URL"] = $env:ACCEPT_BASE_URL

$process = New-Object System.Diagnostics.Process
$process.StartInfo = $processInfo
$process.Start() | Out-Null

Write-Host "[SUCCESS] Spring Boot process started (PID: $($process.Id))" -ForegroundColor Green
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Waiting for application startup..." -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Wait for app to start by checking log file
$maxWait = 120 # 2 minutes
$waited = 0
$started = $false

while ($waited -lt $maxWait -and -not $started) {
    Start-Sleep -Seconds 2
    $waited += 2
    
    if (Test-Path $logFile) {
        $logContent = Get-Content $logFile -Raw -ErrorAction SilentlyContinue
        
        # Check for startup message
        if ($logContent -match "Started ECommerceApplication") {
            $started = $true
            Write-Host "[SUCCESS] Application started successfully!" -ForegroundColor Green
            
            # Extract startup time
            if ($logContent -match "Started ECommerceApplication in ([\d.]+) seconds") {
                Write-Host "[INFO] Startup time: $($matches[1]) seconds" -ForegroundColor Gray
            }
            
            # Check for DevTools restart (token refresh fix compilation)
            if ($logContent -match "Restarting due to \d+ class path changes") {
                Write-Host "[INFO] DevTools detected code changes and recompiled" -ForegroundColor Cyan
            }
        }
        
        # Check for errors
        if ($logContent -match "BUILD FAILURE" -or $logContent -match "APPLICATION FAILED TO START") {
            Write-Host "[ERROR] Application failed to start!" -ForegroundColor Red
            Write-Host "Check app-output.log for details" -ForegroundColor Yellow
            break
        }
        
        # Show progress
        $dots = "." * ($waited / 2)
        Write-Host "`rWaiting$dots ($waited seconds)" -NoNewline -ForegroundColor Gray
    }
}

Write-Host "" # New line after progress

if ($started) {
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host "APPLICATION IS RUNNING" -ForegroundColor Green
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Process ID: $($process.Id)" -ForegroundColor Cyan
    Write-Host "Log file: app-output.log" -ForegroundColor Cyan
    Write-Host "Base URL: http://localhost:8080" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "[READY] You can now run tests with .\test-auth.ps1" -ForegroundColor Yellow
    Write-Host "[READY] VS Code terminal commands will NOT stop the app" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To stop the app, run: .\stop-app.ps1" -ForegroundColor Gray
    Write-Host ""
    
    # Save PID for later stopping
    $process.Id | Out-File "app.pid" -Force
    
} else {
    Write-Host ""
    Write-Host "[TIMEOUT] App did not start within $maxWait seconds" -ForegroundColor Red
    Write-Host "Check app-output.log for errors" -ForegroundColor Yellow
    Write-Host ""
    
    # Show last 20 lines of log
    if (Test-Path $logFile) {
        Write-Host "Last 20 lines of log:" -ForegroundColor Yellow
        Get-Content $logFile -Tail 20
    }
}
