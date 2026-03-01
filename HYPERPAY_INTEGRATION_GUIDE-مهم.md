# 🚀 HyperPay Payment Integration Guide

## Table of Contents
1. [Environment Configuration](#1-environment-configuration)
2. [GitHub Actions & Secrets](#2-github-actions--secrets)
3. [Database Migration](#3-database-migration)
4. [HyperPay Account Setup](#4-hyperpay-account-setup)
5. [Free Hosting Options](#5-free-hosting-options)
6. [Testing Payment Flow](#6-testing-payment-flow)
7. [Frontend Integration](#7-frontend-integration-todo)

---

## 1. Environment Configuration

### Local Development (.env file)

Create a `.env` file in the project root (copy from `.env.example`):

```bash
# HyperPay Payment Gateway
HYPERPAY_ENTITY_ID_VISA=
HYPERPAY_ENTITY_ID_MASTERCARD=
HYPERPAY_ENTITY_ID_MADA=
HYPERPAY_ACCESS_TOKEN=
HYPERPAY_BASE_URL=https://test.oppwa.com
```

**Important Notes:**
- ✅ **Without credentials**: App runs in **MOCK MODE** (simulated payments for development)
- ✅ **With credentials**: App uses **REAL HyperPay integration**
- ⚠️ Never commit `.env` file to Git (already in `.gitignore`)

### Load Environment Variables in PowerShell

```powershell
# Run this before starting the app (if not using .env file)
$env:HYPERPAY_ENTITY_ID_VISA="your_visa_entity_id"
$env:HYPERPAY_ENTITY_ID_MASTERCARD="your_mastercard_entity_id"
$env:HYPERPAY_ENTITY_ID_MADA="your_mada_entity_id"
$env:HYPERPAY_ACCESS_TOKEN="your_access_token"
$env:HYPERPAY_BASE_URL="https://test.oppwa.com"
```

---

## 2. GitHub Actions & Secrets

### Add Secrets to GitHub Repository

1. Go to your GitHub repository
2. Navigate to: **Settings** → **Secrets and variables** → **Actions**
3. Click **"New repository secret"**
4. Add each secret:

| Secret Name | Value |
|------------|-------|
| `HYPERPAY_ENTITY_ID_VISA` | Your Visa entity ID |
| `HYPERPAY_ENTITY_ID_MASTERCARD` | Your Mastercard entity ID |
| `HYPERPAY_ENTITY_ID_MADA` | Your Mada entity ID |
| `HYPERPAY_ACCESS_TOKEN` | Your HyperPay access token |
| `HYPERPAY_BASE_URL` | `https://test.oppwa.com` or `https://oppwa.com` (production) |

### Use Secrets in GitHub Actions Workflow

```yaml
# .github/workflows/deploy.yml
name: Deploy Application

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    env:
      HYPERPAY_ENTITY_ID_VISA: ${{ secrets.HYPERPAY_ENTITY_ID_VISA }}
      HYPERPAY_ENTITY_ID_MASTERCARD: ${{ secrets.HYPERPAY_ENTITY_ID_MASTERCARD }}
      HYPERPAY_ENTITY_ID_MADA: ${{ secrets.HYPERPAY_ENTITY_ID_MADA }}
      HYPERPAY_ACCESS_TOKEN: ${{ secrets.HYPERPAY_ACCESS_TOKEN }}
      HYPERPAY_BASE_URL: ${{ secrets.HYPERPAY_BASE_URL }}
    
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Build with Maven
        run: mvn clean package -DskipTests
      
      - name: Deploy to hosting platform
        run: |
          # Your deployment commands here
```

**✅ GitHub Actions is 100% FREE** for public repositories (unlimited minutes)

---

## 3. Database Migration

### ✅ Migration Already Created!

File: `src/main/resources/db/migration/V1_2__Add_Payment_Tracking_Columns.sql`

```sql
ALTER TABLE orders ADD COLUMN checkout_id VARCHAR(255);
ALTER TABLE orders ADD COLUMN transaction_id VARCHAR(255);
CREATE INDEX idx_orders_checkout_id ON orders(checkout_id);
```

**Migration runs automatically** when you start the application (Flyway will detect and execute it).

### Verify Migration

Check the Spring Boot logs for:
```
INFO: Migrating schema `ecommerce` to version "1.2 - Add Payment Tracking Columns"
INFO: Successfully applied 1 migration
```

Or query the database:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1;
```

---

## 4. HyperPay Account Setup

### Step 1: Register for HyperPay Test Account

1. Visit: **https://www.hyperpay.com/**
2. Click **"Sign Up"** or **"Get Started"**
3. Fill in business details:
   - Company name
   - Email address
   - Phone number
   - Country (Egypt, Saudi Arabia, or UAE)

### Step 2: Get Test Credentials

After account approval:

1. **Login to HyperPay Dashboard**: https://dashboard.hyperpay.com/
2. Navigate to: **Integration** → **API Credentials**
3. Copy the following credentials:
   - **Entity ID (VISA)**: Used for Visa card payments
   - **Entity ID (MASTERCARD)**: Used for Mastercard payments
   - **Entity ID (MADA)**: Used for Mada payments (Saudi Arabia)
   - **Access Token (Bearer Token)**: Authentication token for API calls

### Step 3: Configure Test Environment

```bash
HYPERPAY_ENTITY_ID_VISA=8a8294174b7ecb28014b9699220015ca
HYPERPAY_ENTITY_ID_MASTERCARD=8a8294174b7ecb28014b9699220015cb
HYPERPAY_ENTITY_ID_MADA=8a8294174b7ecb28014b9699220015cc
HYPERPAY_ACCESS_TOKEN=OGE4Mjk0MTc0YjdlY2IyODAxNGI5Njk5MjIwMDE1Y2N8c3k2S0pzVDg=
HYPERPAY_BASE_URL=https://test.oppwa.com
```

⚠️ **These are example credentials - use your actual credentials from HyperPay dashboard**

### Step 4: Test Card Numbers (HyperPay Test Environment)

Use these test cards in HyperPay test environment:

| Card Type | Card Number | CVV | Expiry | Result |
|-----------|-------------|-----|--------|--------|
| Visa | `4200000000000000` | `123` | `05/30` | ✅ Success |
| Mastercard | `5200000000000007` | `123` | `05/30` | ✅ Success |
| Visa | `4000000000000002` | `123` | `05/30` | ❌ Failed |

---

## 5. Free Hosting Options

### ⚠️ Important: You Need a Public URL for Payment Callbacks

HyperPay needs to redirect customers back to your application after payment. `localhost` won't work in production.

### 🆓 Option 1: Render (Recommended)

**Why Render?**
- ✅ 100% FREE forever (not a trial)
- ✅ No credit card required
- ✅ Provides HTTPS domain: `your-app.onrender.com`
- ✅ Direct GitHub integration
- ✅ Auto-deploys on Git push

**Setup Steps:**

1. Go to: https://render.com/
2. Click **"Get Started"** (free account)
3. Connect your GitHub repository
4. Click **"New Web Service"**
5. Configure:
   - **Name**: `ecommerce-app`
   - **Region**: Choose closest to your users
   - **Branch**: `main`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/ecommerce-0.0.1-SNAPSHOT.jar`
   - **Instance Type**: **Free**

6. Add Environment Variables:
   ```
   HYPERPAY_ENTITY_ID_VISA=your_visa_entity_id
   HYPERPAY_ENTITY_ID_MASTERCARD=your_mastercard_entity_id
   HYPERPAY_ENTITY_ID_MADA=your_mada_entity_id
   HYPERPAY_ACCESS_TOKEN=your_access_token
   HYPERPAY_BASE_URL=https://test.oppwa.com
   SENDGRID_API_KEY=your_sendgrid_key
   SENDGRID_FROM_EMAIL=your_email
   SENDGRID_FROM_NAME=E-Commerce Platform
   ```

7. Click **"Create Web Service"**

Your app will be available at: **`https://ecommerce-app.onrender.com`**

**Free Tier Limitations:**
- ⚠️ App "sleeps" after 15 minutes of inactivity (wakes up in 30-60 seconds on first request)
- ✅ 750 hours/month runtime (enough for continuous use)
- ✅ 512MB RAM, 0.1 CPU

### 🆓 Option 2: Railway (Alternative)

**Why Railway?**
- ✅ $5 FREE credit per month (enough for small apps)
- ✅ No credit card for free tier
- ✅ Better performance than Render
- ✅ Provides HTTPS domain: `your-app.railway.app`

**Setup Steps:**

1. Go to: https://railway.app/
2. Sign up with GitHub
3. Click **"New Project"** → **"Deploy from GitHub repo"**
4. Select your repository
5. Railway auto-detects Spring Boot (Java) app
6. Add environment variables (same as Render)
7. Deploy!

**Free Tier:**
- ✅ $5 credit/month (resets monthly)
- ⚠️ Approx. 500 hours runtime/month
- ✅ Better performance than Render

### 🆓 Option 3: Fly.io

**Why Fly.io?**
- ✅ FREE tier: 3 VMs with 256MB RAM each
- ✅ No credit card required for free tier
- ✅ Global edge network

**Setup Steps:**

1. Go to: https://fly.io/
2. Sign up
3. Install Fly CLI: `powershell -c "iwr https://fly.io/install.ps1 -useb | iex"`
4. Run: `flyctl launch` in your project directory
5. Configure environment variables: `flyctl secrets set HYPERPAY_ACCESS_TOKEN=...`

### 🎯 Recommendation: **Use Render**

- Simplest setup (no CLI needed)
- 100% free forever
- Direct GitHub integration
- Perfect for learning/portfolio projects

---

## 6. Testing Payment Flow

### Step 1: Start Application

```powershell
# Set environment variables
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.10"
$env:HYPERPAY_ENTITY_ID_VISA="your_visa_entity_id"
$env:HYPERPAY_ENTITY_ID_MASTERCARD="your_mastercard_entity_id"
$env:HYPERPAY_ENTITY_ID_MADA="your_mada_entity_id"
$env:HYPERPAY_ACCESS_TOKEN="your_access_token"
$env:HYPERPAY_BASE_URL="https://test.oppwa.com"

# Start application
mvn spring-boot:run
```

Check logs for:
```
✅ HyperPayClient initialized (REAL mode)
```

### Step 2: Create an Order

```bash
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": "customer-123",
  "items": [
    {
      "productId": "product-456",
      "quantity": 2,
      "price": 100.00
    }
  ]
}
```

Response:
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "totalAmount": 200.00
}
```

### Step 3: Confirm Order

```bash
POST http://localhost:8080/api/orders/{orderId}/confirm
```

Order status changes: `PENDING` → `CONFIRMED`

### Step 4: Initiate Payment

```bash
POST http://localhost:8080/api/payments/checkout
Content-Type: application/json

{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "paymentMethod": "VISA",
  "customerEmail": "customer@example.com"
}
```

Response:
```json
{
  "success": true,
  "checkoutId": "8a829449501d33d301501d3d60d101ca.uat01-vm-tx01",
  "redirectUrl": "https://test.oppwa.com/v1/paymentWidgets.js?checkoutId=8a829449501d33d301501d3d60d101ca.uat01-vm-tx01",
  "expiresInSeconds": 1800,
  "message": "Checkout session created successfully"
}
```

### Step 5: Complete Payment (Manual Testing)

1. Copy the `redirectUrl` from response
2. Open in browser
3. Enter test card details:
   - **Card Number**: `4200000000000000`
   - **CVV**: `123`
   - **Expiry**: `05/30`
   - **Name**: `Test Customer`
4. Click **"Pay"**
5. HyperPay will redirect back to your callback URL

### Step 6: Verify Payment

```bash
POST http://localhost:8080/api/payments/verify
Content-Type: application/json

{
  "checkoutId": "8a829449501d33d301501d3d60d101ca.uat01-vm-tx01"
}
```

Response:
```json
{
  "success": true,
  "status": "SUCCESS",
  "transactionId": "8a829449501d33d301501d3d60d101ca",
  "message": "Payment verified successfully"
}
```

✅ Order status changes: `CONFIRMED` → `PAID`

### Step 7: Verify in Database

```sql
SELECT id, status, checkout_id, transaction_id, total_amount 
FROM orders 
WHERE id = '550e8400-e29b-41d4-a716-446655440000';
```

Expected result:
```
id: 550e8400-e29b-41d4-a716-446655440000
status: PAID
checkout_id: 8a829449501d33d301501d3d60d101ca.uat01-vm-tx01
transaction_id: 8a829449501d33d301501d3d60d101ca
total_amount: 200.00
```

---

## 7. Frontend Integration (TODO)

> **📝 TODO: Implement when building frontend**
> 
> This section documents the payment flow for **future frontend development**.
> Reference this guide when implementing the payment UI.

### Payment Flow (Customer Perspective)

```
1. Customer adds items to cart
2. Customer proceeds to checkout
3. Frontend calls POST /api/payments/checkout
4. Frontend receives redirectUrl
5. Frontend redirects customer to HyperPay payment page
6. Customer enters card details on HyperPay page
7. HyperPay processes payment
8. HyperPay redirects back to frontend with checkoutId
9. Frontend calls POST /api/payments/verify
10. Frontend shows success/failure message
```

### Frontend Code Example (React/Vue/Angular)

```javascript
// TODO: Implement this in frontend application

// Step 1: Initiate Payment
async function initiatePayment(orderId, paymentMethod, customerEmail) {
  const response = await fetch('http://localhost:8080/api/payments/checkout', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      orderId: orderId,
      paymentMethod: paymentMethod, // 'VISA', 'MASTERCARD', or 'MADA'
      customerEmail: customerEmail
    })
  });
  
  const data = await response.json();
  
  if (data.success) {
    // Step 2: Redirect to HyperPay
    window.location.href = data.redirectUrl;
  } else {
    alert('Payment initiation failed: ' + data.error);
  }
}

// Step 3: Handle Return from HyperPay
// This runs when customer returns from HyperPay payment page
async function handlePaymentReturn() {
  // Get checkoutId from URL query parameter
  const urlParams = new URLSearchParams(window.location.search);
  const checkoutId = urlParams.get('id'); // HyperPay adds ?id=checkoutId
  
  if (!checkoutId) {
    alert('No checkout ID found');
    return;
  }
  
  // Step 4: Verify Payment
  const response = await fetch('http://localhost:8080/api/payments/verify', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ checkoutId: checkoutId })
  });
  
  const result = await response.json();
  
  if (result.success && result.status === 'SUCCESS') {
    // Payment successful!
    alert('Payment successful! Transaction ID: ' + result.transactionId);
    window.location.href = '/order-confirmation';
  } else if (result.status === 'FAILED') {
    alert('Payment failed: ' + result.message);
    window.location.href = '/payment-failed';
  } else if (result.status === 'PENDING') {
    alert('Payment is pending. We will notify you when it completes.');
    window.location.href = '/order-pending';
  }
}

// Example usage in checkout page:
// initiatePayment('order-123', 'VISA', 'customer@example.com');
```

### Frontend Pages to Create

1. **Checkout Page** (`/checkout`)
   - Shows order summary
   - Payment method selector (Visa/Mastercard/Mada)
   - "Pay Now" button → calls `initiatePayment()`

2. **Payment Return Page** (`/payment/return`)
   - Receives customer after HyperPay redirect
   - Shows "Processing payment..." spinner
   - Calls `handlePaymentReturn()` automatically on page load

3. **Success Page** (`/order-confirmation`)
   - Shows "Payment successful" message
   - Displays order details
   - Shows transaction ID

4. **Failure Page** (`/payment-failed`)
   - Shows "Payment failed" message
   - Provides option to retry payment
   - Link back to checkout

### HyperPay Configuration in Dashboard

**TODO: When you have a domain, configure this in HyperPay dashboard:**

1. Login to HyperPay Dashboard
2. Go to: **Settings** → **Checkout**
3. Set **Return URL**: `https://your-domain.com/payment/return`
4. Enable payment methods: Visa, Mastercard, Mada
5. Save settings

**For local development:**
- Use ngrok to expose localhost: `ngrok http 8080`
- Set return URL to: `https://your-ngrok-url.ngrok.io/payment/return`

---

## Troubleshooting

### Issue: "HyperPayClient initialized in MOCK mode"

**Cause**: HyperPay credentials not set

**Solution**: 
1. Check `.env` file has credentials
2. Or set environment variables before starting app
3. Verify credentials are correct (no typos)

### Issue: "Checkout creation failed"

**Possible causes**:
- Invalid entity ID
- Invalid access token
- Wrong base URL (test vs production)
- Network connectivity issues

**Solution**:
1. Check HyperPay dashboard for correct credentials
2. Verify `HYPERPAY_BASE_URL=https://test.oppwa.com` (test environment)
3. Check application logs for detailed error

### Issue: "No order found for checkoutId"

**Cause**: Order's `checkout_id` field is null

**Solution**:
1. Verify database migration ran: `SELECT * FROM flyway_schema_history;`
2. Check order has `checkout_id`: `SELECT checkout_id FROM orders WHERE id='...';`
3. Re-initiate payment for that order

---

## Production Checklist

Before going live:

- [ ] Get production HyperPay credentials
- [ ] Change `HYPERPAY_BASE_URL` to `https://oppwa.com` (production)
- [ ] Configure real domain in HyperPay dashboard
- [ ] Set up GitHub Actions secrets
- [ ] Deploy to Render/Railway/Fly.io
- [ ] Test with real cards (small amounts)
- [ ] Enable HTTPS (automatic on Render/Railway/Fly.io)
- [ ] Configure CORS for frontend domain
- [ ] Set up monitoring/logging
- [ ] Test callback URL is accessible

---

## Summary

✅ **Environment Variables**: Use `.env` file locally, GitHub Secrets for CI/CD
✅ **Database Migration**: Auto-applied by Flyway on startup
✅ **HyperPay Account**: Sign up at https://www.hyperpay.com/ for test credentials
✅ **Free Hosting**: Use Render (recommended), Railway, or Fly.io
✅ **No Credit Card Needed**: All suggested hosting options have truly free tiers
✅ **Testing**: Use provided test card numbers in HyperPay test environment
✅ **Frontend**: Marked as TODO - implement when building UI

**Need Help?**
- HyperPay Docs: https://wordpresshyperpay.docs.oppwa.com/
- Render Support: https://render.com/docs
- GitHub Actions: https://docs.github.com/actions
