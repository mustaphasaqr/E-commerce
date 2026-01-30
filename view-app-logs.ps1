# View live application logs

param(
    [int]$Lines = 50,
    [switch]$Follow
)

$logFile = "app-output.log"

if (-not (Test-Path $logFile)) {
    Write-Host "[ERROR] Log file not found: $logFile" -ForegroundColor Red
    Write-Host "Make sure the app is running (.\start-app-detached.ps1)" -ForegroundColor Yellow
    exit 1
}

if ($Follow) {
    Write-Host "Following logs (press Ctrl+C to stop)..." -ForegroundColor Cyan
    Write-Host ""
    Get-Content $logFile -Tail $Lines -Wait
} else {
    Write-Host "Last $Lines lines of logs:" -ForegroundColor Cyan
    Write-Host ""
    Get-Content $logFile -Tail $Lines
}
