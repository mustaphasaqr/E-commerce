# Stop the detached Spring Boot application

Write-Host "Stopping E-commerce Application..." -ForegroundColor Yellow

# Check if PID file exists
if (Test-Path "app.pid") {
    $pid = Get-Content "app.pid" -Raw
    $pid = $pid.Trim()
    
    try {
        $process = Get-Process -Id $pid -ErrorAction Stop
        Write-Host "Found process $pid. Stopping..." -ForegroundColor Cyan
        Stop-Process -Id $pid -Force
        Remove-Item "app.pid" -Force
        Write-Host "[SUCCESS] Application stopped" -ForegroundColor Green
    } catch {
        Write-Host "[INFO] Process $pid not found (may have already stopped)" -ForegroundColor Gray
        Remove-Item "app.pid" -Force -ErrorAction SilentlyContinue
    }
} else {
    # Try to find and kill any Java Spring Boot processes
    Write-Host "No PID file found. Searching for Spring Boot processes..." -ForegroundColor Cyan
    
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
    $stopped = 0
    
    foreach ($proc in $javaProcesses) {
        try {
            $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($proc.Id)").CommandLine
            if ($cmdLine -like "*spring-boot*" -or $cmdLine -like "*ECommerceApplication*") {
                Stop-Process -Id $proc.Id -Force
                $stopped++
                Write-Host "[SUCCESS] Stopped Java process $($proc.Id)" -ForegroundColor Green
            }
        } catch {
            # Ignore processes we can't access
        }
    }
    
    if ($stopped -eq 0) {
        Write-Host "[INFO] No Spring Boot processes found" -ForegroundColor Gray
    } else {
        Write-Host "[SUCCESS] Stopped $stopped process(es)" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "Application stopped. You can start it again with .\start-app-detached.ps1" -ForegroundColor Cyan
