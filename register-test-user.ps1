# Test User Registration with SendGrid Email
Write-Host "Testing User Registration..." -ForegroundColor Cyan

$body = ConvertTo-Json -Depth 3 @{
    email = "mustaphaosamasaqr@gmail.com"
    password = "xK9#vNm2@pLq5$Rw"
    username = "mustapha_saqr"
    termsAccepted = $true
}

Write-Host "Request body: $body"

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/users" `
        -Method POST `
        -Body $body `
        -ContentType "application/json" `
        -TimeoutSec 30
    
    Write-Host "`n=== SUCCESS ===" -ForegroundColor Green
    Write-Host "User registered successfully!"
    Write-Host ""
    Write-Host "User Details:"
    $response | ConvertTo-Json -Depth 3
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Yellow
    Write-Host "CHECK YOUR EMAIL: mustaphaosamasaqr@gmail.com" -ForegroundColor Yellow
    Write-Host "Look for welcome email from 'E-Commerce Platform'" -ForegroundColor Yellow
    Write-Host "=====================================" -ForegroundColor Yellow
    
} catch {
    Write-Host "`n=== FAILED ===" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)"
    Write-Host ""
    
    if ($_.ErrorDetails.Message) {
        Write-Host "Error Details:"
        $_.ErrorDetails.Message | ConvertFrom-Json | ConvertTo-Json -Depth 3
    } else {
        Write-Host "Exception: $($_.Exception.Message)"
    }
}
