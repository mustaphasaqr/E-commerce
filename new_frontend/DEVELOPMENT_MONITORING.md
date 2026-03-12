# Development Monitoring & Quality Assurance Guide

## Automated Error Detection (4 Layers)

### 1. 🔴 ESLint - Syntax & Import Errors
**What:** Catches import errors, unused variables, and code quality issues
**When:** Runs as you type (VS Code extension) or with:
```bash
npm run lint
npm run lint:fix        # Auto-fix issues
```
**What to look for:** Red squiggles in VS Code = ESLint warning

---

### 2. 🔵 TypeScript - Type Checking
**What:** Ensures all variables are properly typed, catches undefined references
**When:** Builds automatically, or check anytime with:
```bash
npm run type-check              # One-time check
npm run type-check:watch        # Continuous monitoring
```
**What to look for:** Red squiggles in VS Code = Type error

---

### 3. 🟢 Vite Build Overlay
**What:** Red error banner in browser if build fails
**When:** Appears automatically during `npm run dev`
**What to look for:** Red box in top-right of browser when you save a breaking file

---

### 4. 📊 Console Monitoring (Axios Interceptors)
**What:** Logs all API calls with status, request/response data
**When:** Automatic - every API call is logged in DevTools Console
**What to look for:** 
- `📤 API Request` = outgoing API call (green)
- `✅ API Success` = successful response (green)
- `❌ API Error` = failed request (red)

---

## Development Workflow

### During Development (Each Save)
```
1. Save file (Ctrl+S)
        ↓
2. Look at Vite Terminal for build errors
   - ✅ "compiled successfully" → OK
   - ❌ Red error text → STOP, fix before continuing
        ↓
3. Look at Browser Console (F12 → Console tab)
   - ❌ Red errors? → Read and fix
        ↓
4. Visual check
   - Does your change appear in browser? YES ✅
```

### Testing Features
```
1. Perform action in browser (click button, fill form)
        ↓
2. Open DevTools Console (F12)
        ↓
3. Look for API logs:
   - 📤 Request logged?
   - ✅ Success logged?
   - ❌ Error logged? (if so, check status code)
        ↓
4. Open Network tab (F12 → Network)
        ↓
5. Find the API call and check:
   - Status: 200 (OK) or error?
   - Response: has correct data?
```

---

## Quick Reference

| Layer | Command | When It Runs | What It Catches |
|-------|---------|---|---|
| **ESLint** | `npm run lint` | Manual or auto-save | Syntax, unused imports, code style |
| **TypeScript** | `npm run type-check` | Build time | Type errors, undefined variables |
| **Vite Overlay** | (Built-in) | During `npm run dev` | Build failures |
| **Console Logs** | (Automatic) | API calls | Failed requests, API responses |

---

## Prevention Checklist (After Every Code Save)

- [ ] Terminal shows "compiled successfully"?
- [ ] Browser shows your change visually?
- [ ] Console (F12) has NO red errors?
- [ ] Run API call → Check for 📤📤✅ logs in console?

**If ANY checkbox fails → Stop and fix BEFORE continuing**

---

## Enabling IDE Integration

### VS Code Extensions (Recommended)
Install these extensions for real-time error detection:
1. **ESLint** - See linting errors as you type
2. **TypeScript Vue Plugin** - Type checking in real-time
3. **Vite** - Vite support in VS Code

### In VS Code Settings (`.vscode/settings.json`)
```json
{
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "editor.formatOnSave": true,
  "[typescript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  }
}
```

---

## Common Issues & Fixes

### Issue: Console shows `❌ API Error 404`
**Cause:** Backend endpoint doesn't exist or wrong path
**Fix:** 
1. Check backend is running (localhost:8080)
2. Verify endpoint path in code matches backend
3. Check Vite proxy config in vite.config.ts

### Issue: TypeScript error about undefined variable
**Fix:** 
1. Check all imports at top of file
2. Run `npm run type-check` to see all errors
3. Type each variable properly

### Issue: Browser shows blank page after saving
**Fix:**
1. Check Vite terminal for build error (red text)
2. Press Ctrl+Shift+R (hard refresh browser)
3. Check Console tab (F12) for JavaScript errors

### Issue: API call succeeds but data looks wrong
**Fix:**
1. Check Response in Network tab (F12 → Network)
2. Compare response shape with expected types in code
3. Check backend response format matches frontend expectations

---

## Summary

**You are now protected by 4 layers of error detection:**
1. VS Code shows syntax/type errors (red squiggles)
2. Vite shows build errors (red banner in browser)
3. ESLint ensures code quality (lint command)
4. Console logs show all API activity (open DevTools)

**This prevents the "corrupted web" problem because:**
- You catch errors BEFORE testing
- You monitor API calls in real-time
- You see build failures immediately
- You can identify root cause quickly
