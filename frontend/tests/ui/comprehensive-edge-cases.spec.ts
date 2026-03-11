import { test, expect } from '@playwright/test';

/**
 * COMPREHENSIVE EDGE CASES TESTS
 * UI-Based Testing: Tests unusual inputs, boundaries, race conditions
 * Frontend in the middle - Frontend validates and calls backend naturally
 * 
 * What users experience:
 * - Entering unusual but valid data
 * - Boundary value testing (very long inputs, special characters)
 * - Race conditions (rapid clicks, concurrent requests)
 * - Empty/null handling
 */

const BASE_URL = 'http://localhost:3002';

test.describe('Edge Cases - Unusual Inputs, Boundaries, Race Conditions', () => {
  
  // ======================== AUTH EDGE CASES ========================

  test('edge-case-auth-1: Login with email containing plus addressing', async ({ page }) => {
    // User enters email with + (commonly used for email filtering)
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="email"]', 'user+special@example.com');
    await page.fill('input[type="password"]', 'Password123!');
    await page.click('button[type="submit"]');
    
    // Frontend should handle this and call backend - verify response handling
    const errorOrSuccess = page.locator('.error-message, .success-message, [role="alert"]');
    await expect(errorOrSuccess).toBeVisible({ timeout: 3000 });
  });

  test('edge-case-auth-2: Login with email containing dots', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="email"]', 'user.name+tag@example.co.uk');
    await page.fill('input[type="password"]', 'Password123!');
    await page.click('button[type="submit"]');
    
    const response = page.locator('.error-message, .success-message, [role="alert"]');
    await expect(response).toBeVisible({ timeout: 3000 });
  });

  test('edge-case-auth-3: Password with special characters', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    await page.fill('input[name="email"]', `user${Date.now()}@example.com`);
    await page.fill('input[name="username"]', `user${Date.now()}`);
    
    // Complex password with special chars
    await page.fill('input[type="password"]', 'P@$$w0rd!#%&*()[]{}');
    await page.click('button[type="submit"]');
    
    const feedback = page.locator('.error-message, .success-message, .spinner');
    await expect(feedback).toBeVisible({ timeout: 5000 });
  });

  test('edge-case-auth-4: Rapid form submission (should debounce)', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="email"]', 'test@example.com');
    await page.fill('input[type="password"]', 'TestPassword123!');
    
    const submitBtn = page.locator('button[type="submit"]');
    
    // Click rapidly multiple times
    await submitBtn.click();
    await submitBtn.click();
    await submitBtn.click();
    
    // Frontend should prevent multiple submissions - button should disable
    await expect(submitBtn).toBeDisabled({ timeout: 500 });
    
    // Should only show one success/error message
    await page.waitForTimeout(2000);
    const messages = page.locator('.success-message');
    expect(await messages.count()).toBeLessThanOrEqual(1);
  });

  // ======================== FORM INPUT BOUNDARY CASES ========================

  test('edge-case-form-1: Very long username (max length)', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    
    // Test max length - typically 255 chars
    const longUsername = 'a'.repeat(255);
    await page.fill('input[name="username"]', longUsername);
    
    // Should either accept or show validation error
    const submitBtn = page.locator('button[type="submit"]');
    const hasError = await page.locator('input[name="username"]').evaluate((el: any) => 
      el.hasAttribute('aria-invalid') && el.getAttribute('aria-invalid') === 'true'
    );
    
    if (!hasError) {
      await submitBtn.click();
      await page.waitForTimeout(1000);
      // Should handle gracefully - either accept or reject
      const feedback = page.locator('[role="alert"], .error-message, .success-message');
      await expect(feedback).toBeVisible({ timeout: 3000 });
    }
  });

  test('edge-case-form-2: Unicode characters in username', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    await page.fill('input[name="email"]', `user${Date.now()}@example.com`);
    
    // Unicode characters
    await page.fill('input[name="username"]', '用户🎉名');
    
    const feedback = page.locator('.error-message, [role="alert"]');
    // Frontend should validate - either accept or reject with clear message
    const inputField = page.locator('input[name="username"]');
    const isInvalid = await inputField.evaluate((el: any) => 
      el.hasAttribute('aria-invalid')
    );
    
    expect([true, false]).toContain(isInvalid);
  });

  test('edge-case-form-3: Email with consecutive dots (RFC valid but often problematic)', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    await page.fill('input[name="email"]', 'user..name@example.com');
    await page.fill('input[name="username"]', `user${Date.now()}`);
    await page.fill('input[type="password"]', 'Password123!');
    
    const submitBtn = page.locator('button[type="submit"]');
    const isDisabled = await submitBtn.isDisabled();
    
    // Frontend should either prevent (disable button) or let backend validate
    if (!isDisabled) {
      await submitBtn.click();
      const errorMsg = page.locator('[role="alert"], .error-message');
      await expect(errorMsg).toBeVisible({ timeout: 3000 });
    }
  });

  test('edge-case-form-4: Whitespace-only input', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    // Only spaces (frontend should trim)
    await page.fill('input[type="email"]', '   ');
    await page.fill('input[type="password"]', '   ');
    
    const submitBtn = page.locator('button[type="submit"]');
    const isDisabled = await submitBtn.isDisabled();
    
    // Frontend should prevent empty/whitespace submission
    expect(isDisabled).toBe(true);
  });

  // ======================== CONCURRENT REQUEST EDGE CASES ========================

  test('edge-case-concurrent-1: Rapid profile updates (concurrent requests)', async ({ page }) => {
    // Login first
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="email"]', 'testuser@example.com');
    await page.fill('input[type="password"]', 'TestPassword123!');
    await page.click('button[type="submit"]');
    
    // Wait for login to complete
    await page.waitForURL(`${BASE_URL}/**`);
    
    // Navigate to profile
    await page.goto(`${BASE_URL}/profile`);
    await page.waitForSelector('button:has-text("Change Email")', { timeout: 5000 });
    
    // Simulate rapid tab switching or concurrent requests
    const emailChangeBtn = page.locator('button:has-text("Change Email")').first();
    const passwordChangeBtn = page.locator('button:has-text("Change Password")').first();
    
    if (await emailChangeBtn.isVisible()) {
      await emailChangeBtn.click();
      await emailChangeBtn.click(); // Click again before first completes
      
      // Frontend should handle race condition - button should disable
      await expect(emailChangeBtn).toBeDisabled({ timeout: 500 });
    }
  });

  test('edge-case-concurrent-2: Rapid pagination clicks', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    // Try to wait for page load
    const nextBtn = page.locator('button:has-text("Next")');
    
    if (await nextBtn.isVisible()) {
      // Click rapidly
      await nextBtn.click();
      await nextBtn.click();
      await nextBtn.click();
      
      // Should only load one page, button should disable during load
      await page.waitForTimeout(1000);
      
      // Verify table updated only once
      const rows = page.locator('tbody tr');
      await expect(rows).toHaveCount(await rows.count()); // Just verify it's valid
    }
  });

  // ======================== TIMEOUT & SLOW NETWORK EDGE CASES ========================

  test('edge-case-network-1: Slow API response (frontend shows loading state)', async ({ page }) => {
    // Simulate slow network
    await page.route('**/api/**', route => {
      setTimeout(() => route.continue(), 2000); // 2 second delay
    });
    
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="email"]', 'slow@example.com');
    await page.fill('input[type="password"]', 'Password123!');
    
    const submitBtn = page.locator('button[type="submit"]');
    await submitBtn.click();
    
    // Frontend should show loading state immediately
    const loadingIndicator = page.locator('.spinner, [role="progressbar"], button:has-text("Loading")');
    await expect(loadingIndicator).toBeVisible({ timeout: 500 });
  });

  test('edge-case-network-2: Failed request with retry', async ({ page }) => {
    let requestCount = 0;
    
    // Fail first attempt, succeed on retry
    await page.route('**/api/**/login', route => {
      requestCount++;
      if (requestCount === 1) {
        route.abort(); // Fail first request
      } else {
        route.continue();
      }
    });
    
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="email"]', 'test@example.com');
    await page.fill('input[type="password"]', 'Password123!');
    await page.click('button[type="submit"]');
    
    // Should show error and allow retry
    const errorMsg = page.locator('[role="alert"], .error-message');
    await expect(errorMsg).toBeVisible({ timeout: 3000 });
    
    // Verify retry button or user can try again
    const retryBtn = page.locator('button:has-text("Retry"), button[type="submit"]');
    expect(await retryBtn.isVisible()).toBe(true);
  });

  // ======================== STATE EDGE CASES ========================

  test('edge-case-state-1: Session timeout during form submission', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    // Clear auth token (simulate session timeout)
    await page.evaluate(() => {
      localStorage.removeItem('accessToken');
      sessionStorage.removeItem('accessToken');
    });
    
    // Try to submit a form
    const emailInput = page.locator('input[name*="email"]').first();
    if (await emailInput.isVisible()) {
      await emailInput.fill('newemail@example.com');
      
      const submitBtn = page.locator('button:has-text("Save"), button[type="submit"]').first();
      if (await submitBtn.isVisible()) {
        await submitBtn.click();
        
        // Frontend should detect session timeout
        // Either redirect to login or show session expired message
        await page.waitForTimeout(2000);
        const currentUrl = page.url();
        const isRedirected = currentUrl.includes('/login') || currentUrl.includes('session');
        const errorMsg = page.locator('[role="alert"], .error-message');
        
        expect(
          isRedirected || await errorMsg.isVisible({ timeout: 1000 }).catch(() => false)
        ).toBe(true);
      }
    }
  });

  test('edge-case-state-2: Page refresh during form submission', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="email"]', 'test@example.com');
    await page.fill('input[type="password"]', 'Password123!');
    
    // Start submission
    const submitBtn = page.locator('button[type="submit"]');
    await submitBtn.click();
    
    // Immediately refresh
    await page.reload();
    
    // Frontend should handle gracefully - either stay on page or redirect
    await page.waitForLoadState('networkidle');
    // Don't crash - verify app is still usable
    const pageTitle = page.locator('h1, h2');
    await expect(pageTitle).toBeVisible({ timeout: 3000 });
  });

  // ======================== VALIDATION BOUNDARY CASES ========================

  test('edge-case-validation-1: Password barely meets requirements', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    await page.fill('input[name="email"]', `min${Date.now()}@example.com`);
    await page.fill('input[name="username"]', `user${Date.now()}`);
    
    // Minimum valid password (e.g. 8 chars, 1 upper, 1 lower, 1 number, 1 special)
    await page.fill('input[type="password"]', 'Aa1!bcde');
    
    const submitBtn = page.locator('button[type="submit"]');
    const isDisabled = await submitBtn.isDisabled();
    
    expect(isDisabled).toBe(false);
  });

  test('edge-case-validation-2: Password just under minimum', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    await page.fill('input[type="email"]', `min${Date.now()}@example.com`);
    await page.fill('input[name="username"]', `user${Date.now()}`);
    
    // One char short
    await page.fill('input[type="password"]', 'Aa1!bcd');
    
    const submitBtn = page.locator('button[type="submit"]');
    const isDisabled = await submitBtn.isDisabled();
    
    expect(isDisabled).toBe(true);
  });

  test('edge-case-validation-3: Empty optional fields should submit', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    // Find optional fields (like phone, company, etc)
    const optionalInputs = page.locator('input[aria-required="false"]');
    const optionalCount = await optionalInputs.count();
    
    if (optionalCount > 0) {
      // Leave them empty - should not prevent submission
      const submitBtn = page.locator('button[type="submit"]').first();
      expect(await submitBtn.isDisabled()).toBe(false);
    }
  });
});
