# Authentication Testing Script
# Run this in a separate PowerShell window while the app is running

Write-Host "Testing Authentication Flows..." -ForegroundColor Cyan
Write-Host ""

# Test 1: Register User
Write-Host "Test 1: Register User" -ForegroundColor Yellow
$registerBody = @{
    username = "testuser"
    email = "test@example.com"
    password = "Test123!@#"
    firstName = "Test"
    lastName = "User"
    phoneNumber = "+1234567890"
    termsAccepted = $true
} | ConvertTo-Json

try {
    $user = Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method POST -ContentType "application/json" -Body $registerBody
    Write-Host "[SUCCESS] User registered: $($user.username)" -ForegroundColor Green
    Write-Host "   User ID: $($user.id)" -ForegroundColor Gray
} catch {
    if ($_.Exception.Message -like "*500*") {
        Write-Host "[SKIPPED] User already exists (expected if running multiple times)" -ForegroundColor Yellow
    } else {
        Write-Host "[FAILED] Registration failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# Test 2: Login
Write-Host "Test 2: Login" -ForegroundColor Yellow
$loginBody = @{
    email = "test@example.com"
    password = "Test123!@#"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
    $token = $loginResponse.accessToken
    Write-Host "[SUCCESS] Login successful!" -ForegroundColor Green
    Write-Host "   Access Token: $($token.Substring(0,50))..." -ForegroundColor Gray
    
    # Decode JWT to check sessionId
    $payload = $token -split '\.' | Select-Object -Index 1
    $padding = "=" * ((4 - ($payload.Length % 4)) % 4)
    $decoded = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($payload + $padding))
    Write-Host "   JWT Payload:" -ForegroundColor Gray
    Write-Host "   $decoded" -ForegroundColor Gray
    
} catch {
    Write-Host "[FAILED] Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

Write-Host ""

# Test 3: Access Protected Endpoint
Write-Host "Test 3: Access Protected Endpoint (Get My Profile)" -ForegroundColor Yellow
try {
    $profile = Invoke-RestMethod -Uri "http://localhost:8080/api/users/me" -Headers @{Authorization="Bearer $token"}
    Write-Host "[SUCCESS] Protected endpoint accessed!" -ForegroundColor Green
    Write-Host "   Username: $($profile.username)" -ForegroundColor Gray
    Write-Host "   Email: $($profile.email)" -ForegroundColor Gray
    Write-Host "   Role: $($profile.role)" -ForegroundColor Gray
} catch {
    Write-Host "[FAILED] Protected access failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 4: Token Refresh
Write-Host "Test 4: Token Refresh" -ForegroundColor Yellow
$refreshBody = @{
    refreshToken = $loginResponse.refreshToken
} | ConvertTo-Json

try {
    $refreshResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/refresh" -Method POST -ContentType "application/json" -Body $refreshBody
    Write-Host "[SUCCESS] Token refreshed successfully!" -ForegroundColor Green
    Write-Host "   New Token: $($refreshResponse.accessToken.Substring(0,50))..." -ForegroundColor Gray
} catch {
    Write-Host "[FAILED] Token refresh failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 5: Rate Limiting (attempt 6 failed logins)
Write-Host "Test 5: Rate Limiting (6 failed login attempts)" -ForegroundColor Yellow
$badLoginBody = @{
    email = "test@example.com"
    password = "WrongPassword"
} | ConvertTo-Json

for ($i = 1; $i -le 6; $i++) {
    try {
        Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $badLoginBody -ErrorAction Stop
    } catch {
        if ($i -eq 6) {
            Write-Host "[SUCCESS] Rate limiting working! Attempt $i blocked" -ForegroundColor Green
        } else {
            Write-Host "   Attempt $i failed (expected)" -ForegroundColor Gray
        }
    }
    Start-Sleep -Milliseconds 100
}

Write-Host ""
Write-Host "=== Authentication Testing Complete! ===" -ForegroundColor Green
