# Frontend Testing Guide

## Overview

This frontend project includes **two levels of testing**:

1. **Unit Tests** (Mocked APIs) - Fast, isolated, no backend needed
2. **Integration Tests** (Real Backend) - Validates against your Railway backend

## Unit Tests (Mocked APIs)

### Run Locally

**All unit tests:**
```bash
cd new_frontend
npm run test:unit
```

**Specific test file:**
```bash
npm run test:unit -- src/features/auth/api/authService.test.ts
```

**Watch mode (auto-rerun on changes):**
```bash
npm run test:watch
```

**UI Dashboard:**
```bash
npm run test:ui
```

### What Gets Tested

- **Auth formulas**: Login, logout, refresh token, register, password reset
- **Error handling**: Invalid credentials, missing auth, rate limiting
- **Type safety**: Return types match backend contracts
- **Mock responses**: Hardcoded expected responses (no network calls)

### Example Output

```
✓ src/features/auth/api/authService.test.ts (17)
  ✓ Auth Service (17)
    ✓ login() (3)
      ✓ should login with valid credentials
      ✓ should handle invalid credentials (401)
      ✓ should handle non-existent user (401)
    ✓ logout() (2)
    ✓ refreshToken() (2)
    ✓ logoutAllDevices() (2)
    ✓ requestPasswordReset() (3)
    ✓ completePasswordReset() (3)
    ✓ register() (2)

Test Files  1 passed (1)
     Tests  17 passed (17)
```

---

## Integration Tests (Real Backend)

### Setup Required

Before running integration tests, you need:

1. **Railway backend running** - your e-commerce API must be deployed
2. **Backend URL** - configured in `.env.test`
3. **Test account** - a user account on your backend (or registration endpoint working)

### Configuration

**File: `.env.test`**
```env
VITE_API_URL=https://e-commerce-production-27b3.up.railway.app
VITE_API_TIMEOUT=30000
```

### Run Locally

**All integration tests:**
```bash
cd new_frontend
npm run test:integration
```

**Watch mode:**
```bash
npm run test:integration -- --watch
```

**With verbose output:**
```bash
npm run test:integration -- --reporter=verbose
```

### What Gets Tested

Integration tests validate your backend is working correctly:

| Test | Endpoint | Purpose |
|------|----------|---------|
| **Register** | `POST /api/v1/users` | Create new user account |
| **Login** | `POST /api/v1/auth/login` | Authenticate user |
| **Refresh Token** | `POST /api/v1/auth/refresh` | Extend session |
| **Logout** | `POST /api/v1/auth/logout` | End session |
| **Password Reset** | `POST /api/v1/auth/password-reset/request` | Request reset email |
| **Health Check** | `POST /api/v1/auth/login` (expect 401) | Confirm backend is responsive |

### Example Output

```
✅ Auth Service Integration Tests (Real Backend)
  ✓ should register a new user on real backend
    ✅ Registration successful - user created on backend
    📧 Email: test-1710307800000@example.com
  
  ✓ should login with valid credentials on real backend
    ✅ Login successful
    👤 User: authtest (CUSTOMER)
    🎫 Session ID: 550e8400-e29b-41d4-a716-446655440000
    ⏱️  Token expires in: 3600000ms
  
  ✓ should refresh token with valid refresh token on real backend
    ✅ Token refreshed successfully
    🔄 New token expires in: 3600000ms
  
  ✓ should logout successfully on real backend
    ✅ Logout successful
    🔒 Session cleared on backend
  
  ✓ should request password reset on real backend
    ✅ Password reset email sent (check email for reset link)
    📧 Email: integration-test@example.com
  
  ✓ should confirm backend is accessible
    ✅ Backend is accessible and responding
```

---

## Run All Tests

```bash
cd new_frontend
npm run test:all
```

This runs **both** unit tests (mocked) and integration tests (real backend).

---

## GitHub Actions CI/CD

### Workflow: `frontend-ci-cd.yml`

Automatically runs when you push to `frontend-rebuild` branch:

```
Push to frontend-rebuild
        ↓
┌─────────────────────────────┐
│ Stage 1: Lint & Type Check  │ ✅ Fast (no tests)
└─────────────────────────────┘
        ↓
┌─────────────────────────────┐
│ Stage 2: Unit Tests         │ ✅ Fast (mocked)
└─────────────────────────────┘
        ↓
┌─────────────────────────────┐
│ Stage 3: Integration Tests  │ ⏱️  Medium (real backend)
└─────────────────────────────┘
        ↓
┌─────────────────────────────┐
│ Stage 4: Build Production   │ ✅ Compiles dist/
└─────────────────────────────┘
        ↓
┌─────────────────────────────┐
│ Stage 5: Results Summary    │ 📊 Reports all results
└─────────────────────────────┘
```

### Access Results

1. Go to **GitHub** → Your Repository
2. Click **Actions** tab
3. Find the **"Frontend CI/CD (New Frontend)"** workflow
4. Click the latest run
5. View:
   - ✅ Pass/Fail status for each stage
   - 📊 Step-by-step output
   - 📦 Artifacts: unit test results, integration test results, production build

### What Gets Checked

**Lint & Type Check:**
- TypeScript compilation errors
- ESLint style violations
- Missing imports or types

**Unit Tests:**
- All 17 auth formula tests
- Validates code matches test expectations
- Fast (~6 seconds)

**Integration Tests:**
- Real API calls to Railway backend
- Validates backend endpoints working
- Slower (~15 seconds) but critical

**Build:**
- Produces `dist/` folder
- Ready for deployment

---

## Troubleshooting

### Integration Tests Failing

**Problem:** `❌ Cannot connect to backend`
- **Solution**: Ensure Railway app is running → Check dashboard at [railway.app](https://railway.app)

**Problem:** `⚠️ Invalid credentials` or `403 Forbidden`
- **Solution**: Test user doesn't exist or not verified
- **Steps**:
  1. Run `npm run test:integration` (registration test will create user)
  2. Check email (if sendgrid configured)
  3. Manually verify user on backend
  4. Update test credentials in `.env.test`

**Problem:** Timeout after 15 seconds
- **Solution**: Railway backend is slow or unresponsive
- **Steps**:
  1. Check Railway logs: [railway.app/projects](https://railway.app/projects)
  2. Increase `VITE_API_TIMEOUT` in `.env.test`
  3. Check network connectivity from GitHub Actions

### Unit Tests Failing

**Problem:** `vi.mock() is not defined`
- **Solution**: Ensure vitest is installed → `npm install --save-dev vitest`

**Problem:** Cannot resolve module `@/shared/api/axios`
- **Solution**: Check tsconfig.json has path mapping
  ```json
  "paths": {
    "@/*": ["./src/*"]
  }
  ```

---

## Development Workflow

### When You Add a New Feature

1. **Write formula test** (mocked)
   ```bash
   src/features/[feature]/api/[feature]Service.test.ts
   ```

2. **Implement service** matching test formula
   ```bash
   src/features/[feature]/api/[feature]Service.ts
   ```

3. **Write integration test** (optional, for new API endpoints)
   ```bash
   src/features/[feature]/api/[feature]Service.integration.test.ts
   ```

4. **Run locally**
   ```bash
   npm run test:unit
   npm run test:integration  # if new endpoints
   ```

5. **Push to frontend-rebuild**
   - GitHub Actions auto-runs all tests
   - Check results in Actions tab
   - Merge to main when all pass ✅

---

## Performance Notes

| Test Type | Time | Frequency | When to Run |
|-----------|------|-----------|------------|
| **Lint** | < 1s | Always | `npm run type-check` before commit |
| **Unit Tests** | ~6s | Always | `npm run test:unit` before push |
| **Integration** | ~15s | Before deploy | `npm run test:integration` optional locally |
| **Build** | ~5s | Before deploy | `npm run build` before production |

---

## Environment Variables

### Development
**File: `.env`**
```env
VITE_API_URL=http://localhost:8080
VITE_API_TIMEOUT=30000
```

### Testing
**File: `.env.test`**
```env
VITE_API_URL=https://e-commerce-production-27b3.up.railway.app
VITE_API_TIMEOUT=30000
```

### Production
**File: `.env.production`** (set during build)
```env
VITE_API_URL=https://e-commerce-production-27b3.up.railway.app
VITE_API_TIMEOUT=30000
```

---

## Key Files

| File | Purpose |
|------|---------|
| `vitest.config.ts` | Vitest configuration (jsdom, env loading) |
| `package.json` | Test scripts (test:unit, test:integration, test:all) |
| `.env.test` | Railway backend URL for testing |
| `.github/workflows/frontend-ci-cd.yml` | GitHub Actions CI/CD pipeline |
| `src/features/auth/api/authService.test.ts` | Unit tests (mocked) |
| `src/features/auth/api/authService.integration.test.ts` | Integration tests (real backend) |

---

## Summary

✅ **Unit Tests** - Mocked, fast, validate code formula
✅ **Integration Tests** - Real backend, validate deployment
✅ **GitHub Actions** - Automated CI/CD on every push to frontend-rebuild
✅ **Artifacts** - Test results and production build saved

Your auth feature is **production-ready**. All tests pass. Deploy with confidence! 🚀
