# Test SendGrid Email Integration
# Run this AFTER starting the app with .\start-app-detached.ps1

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Testing SendGrid Email Integration" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Check if app is running
Write-Host "[1/3] Checking if application is running..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 5
    Write-Host "[SUCCESS] Application is UP!" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "[ERROR] Application is not running!" -ForegroundColor Red
    Write-Host "Please start the app first: .\start-app-detached.ps1" -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

# Generate unique test user
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$testEmail = "test.user.$timestamp@example.com"
$testUsername = "testuser$timestamp"
$testPassword = "SecurePass123!@#"

Write-Host "[2/3] Registering test user..." -ForegroundColor Yellow
Write-Host "Email: $testEmail" -ForegroundColor Cyan
Write-Host "Username: $testUsername" -ForegroundColor Cyan
Write-Host ""

# Prepare request body
$registerBody = @{
    email = $testEmail
    username = $testUsername
    password = $testPassword
    firstName = "Test"
    lastName = "User"
} | ConvertTo-Json

# Register user
try {
    $registerResponse = Invoke-WebRequest `
        -Uri "http://localhost:8080/api/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody `
        -TimeoutSec 10
    
    Write-Host "[SUCCESS] User registered successfully!" -ForegroundColor Green
    Write-Host "Response Status: $($registerResponse.StatusCode)" -ForegroundColor Gray
    Write-Host ""
    
    # Parse response
    $responseData = $registerResponse.Content | ConvertFrom-Json
    Write-Host "User ID: $($responseData.userId)" -ForegroundColor Cyan
    Write-Host "Access Token: $($responseData.accessToken.Substring(0, 30))..." -ForegroundColor Cyan
    Write-Host ""
    
} catch {
    Write-Host "[ERROR] Failed to register user!" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Yellow
    Write-Host ""
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "Response Body: $errorBody" -ForegroundColor Gray
    }
    
    exit 1
}

# Check application logs for email sending
Write-Host "[3/3] Checking application logs for email activity..." -ForegroundColor Yellow
Write-Host ""

Start-Sleep -Seconds 3  # Wait for async email processing

if (Test-Path "app-output.log") {
    $logContent = Get-Content "app-output.log" -Raw
    
    # Look for SendGrid-related log entries
    $emailLogs = $logContent | Select-String -Pattern "(SendGrid|welcome email|email.*sent|EmailService)" -AllMatches
    
    if ($emailLogs) {
        Write-Host "[INFO] Email-related log entries found:" -ForegroundColor Cyan
        Write-Host ""
        
        # Get last 30 lines that might contain email info
        $lastLines = Get-Content "app-output.log" -Tail 30 | Where-Object { 
            $_ -match "SendGrid|welcome|email|EmailService|async-email" 
        }
        
        foreach ($line in $lastLines) {
            if ($line -match "ERROR|Exception") {
                Write-Host $line -ForegroundColor Red
            } elseif ($line -match "SUCCESS|sent") {
                Write-Host $line -ForegroundColor Green
            } else {
                Write-Host $line -ForegroundColor Gray
            }
        }
        Write-Host ""
    } else {
        Write-Host "[WARNING] No email-related logs found" -ForegroundColor Yellow
        Write-Host "This might mean:" -ForegroundColor Gray
        Write-Host "  - Email was sent successfully without verbose logging" -ForegroundColor Gray
        Write-Host "  - Email is being processed asynchronously" -ForegroundColor Gray
        Write-Host "  - SendGrid API key might not be configured" -ForegroundColor Gray
        Write-Host ""
    }
}

Write-Host "=====================================" -ForegroundColor Green
Write-Host "EMAIL TEST COMPLETE" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""
Write-Host "✅ User registered successfully" -ForegroundColor Green
Write-Host "📧 Welcome email should be sent to: $testEmail" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Check your SendGrid dashboard: https://app.sendgrid.com/email_activity" -ForegroundColor Gray
Write-Host "  2. Check the test email inbox (if using a real address)" -ForegroundColor Gray
Write-Host "  3. Check app-output.log for detailed email logs" -ForegroundColor Gray
Write-Host ""
Write-Host "Test Credentials:" -ForegroundColor Cyan
Write-Host "  Email: $testEmail" -ForegroundColor Gray
Write-Host "  Username: $testUsername" -ForegroundColor Gray
Write-Host "  Password: $testPassword" -ForegroundColor Gray
Write-Host ""
