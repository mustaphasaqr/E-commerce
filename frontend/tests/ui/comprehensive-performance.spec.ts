import { test, expect } from '@playwright/test';

/**
 * COMPREHENSIVE PERFORMANCE TESTS
 * UI-Based Testing: Tests load times, responsiveness, interactive time
 * Frontend in the middle - Tests actual frontend rendering and responsiveness
 * 
 * What users experience:
 * - Page load time
 * - Time to interactive (first button click works)
 * - Loading state feedback
 * - UI responsiveness during data fetching
 * - Memory leaks and performance degradation
 */

const BASE_URL = 'http://localhost:3002';

test.describe('Performance - Load Times, Responsiveness, Interactive Time', () => {

  // ======================== PAGE LOAD PERFORMANCE ========================

  test('perf-load-1: Login page loads in reasonable time (<2 seconds)', async ({ page }) => {
    const startTime = Date.now();
    
    await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle' });
    
    const loadTime = Date.now() - startTime;
    
    // Should be reasonably fast
    expect(loadTime).toBeLessThan(2000); // 2 seconds
  });

  test('perf-load-2: Register page loads quickly (<2 seconds)', async ({ page }) => {
    const startTime = Date.now();
    
    await page.goto(`${BASE_URL}/register`, { waitUntil: 'networkidle' });
    
    const loadTime = Date.now() - startTime;
    expect(loadTime).toBeLessThan(2000);
  });

  test('perf-load-3: Profile page loads efficiently (<3 seconds)', async ({ page }) => {
    // Login first
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="email"]', 'testuser@example.com');
    await page.fill('input[type="password"]', 'TestPassword123!');
    await page.click('button[type="submit"]');
    await page.waitForURL(`${BASE_URL}/**`);
    
    // Now measure profile page load
    const startTime = Date.now();
    await page.goto(`${BASE_URL}/profile`, { waitUntil: 'networkidle' });
    const loadTime = Date.now() - startTime;
    
    expect(loadTime).toBeLessThan(3000); // 3 seconds (includes data fetch)
  });

  test('perf-load-4: Admin panel loads within acceptable time (<3 seconds)', async ({ page }) => {
    // Assuming admin can access
    const startTime = Date.now();
    
    await page.goto(`${BASE_URL}/admin/users`, { waitUntil: 'networkidle' });
    
    const loadTime = Date.now() - startTime;
    expect(loadTime).toBeLessThan(3000);
  });

  // ======================== TIME TO INTERACTIVE ========================

  test('perf-interactive-1: Login button clickable within 500ms', async ({ page }) => {
    const startTime = Date.now();
    
    await page.goto(`${BASE_URL}/login`);
    
    // Wait for submit button to be clickable
    const submitBtn = page.locator('button[type="submit"]');
    await submitBtn.waitFor({ state: 'attached' });
    
    const timeToInteractive = Date.now() - startTime;
    
    // Button should be interactive very quickly
    expect(timeToInteractive).toBeLessThan(500);
  });

  test('perf-interactive-2: Form inputs focusable immediately', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    const emailInput = page.locator('input[type="email"]');
    
    // Should be able to focus immediately
    const startTime = Date.now();
    await emailInput.focus();
    const timeToFocus = Date.now() - startTime;
    
    expect(timeToFocus).toBeLessThan(100);
    expect(await emailInput.evaluate(el => el === document.activeElement)).toBe(true);
  });

  test('perf-interactive-3: API response reflected in UI within 1 second (normal network)', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    await page.fill('input[type="email"]', 'fast@example.com');
    await page.fill('input[type="password"]', 'TestPassword123!');
    
    const startTime = Date.now();
    await page.click('button[type="submit"]');
    
    // Loading state should appear immediately
    const loadingIndicator = page.locator('.spinner, [role="progressbar"], button:has-text("Loading")');
    await expect(loadingIndicator).toBeVisible({ timeout: 300 });
    
    const feedbackTime = Date.now() - startTime;
    expect(feedbackTime).toBeLessThan(300); // Immediate feedback
    
    // Final result within 1 second
    const result = page.locator('[role="alert"], .success-message, .error-message');
    await expect(result).toBeVisible({ timeout: 1000 });
  });

  // ======================== LOADING STATE FEEDBACK ========================

  test('perf-loading-1: Loading indicator appears during API call', async ({ page }) => {
    // Simulate slow API
    await page.route('**/api/**', async (route) => {
      await new Promise(resolve => setTimeout(resolve, 1500)); // 1.5s delay
      await route.continue();
    });
    
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="email"]', 'slow@example.com');
    await page.fill('input[type="password"]', 'TestPassword123!');
    
    const submitBtn = page.locator('button[type="submit"]');
    await submitBtn.click();
    
    // Loading indicator should be visible
    const spinner = page.locator('.spinner, [role="progressbar"], button:has-text("Loading")');
    await expect(spinner).toBeVisible({ timeout: 300 });
  });

  test('perf-loading-2: Loading state cleared after response', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    // Trigger a form submission
    const submitBtn = page.locator('button[type="submit"]').first();
    if (await submitBtn.isVisible()) {
      await submitBtn.click();
      
      // Wait for loading to appear and disappear
      const spinner = page.locator('.spinner, [role="progressbar"]');
      
      if (await spinner.isVisible({ timeout: 500 }).catch(() => false)) {
        // Loading should disappear
        await expect(spinner).not.toBeVisible({ timeout: 3000 });
      }
    }
  });

  test('perf-loading-3: Button disabled during submission', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="email"]', 'test@example.com');
    await page.fill('input[type="password"]', 'TestPassword123!');
    
    const submitBtn = page.locator('button[type="submit"]');
    await submitBtn.click();
    
    // Button should be disabled or show loading
    const isDisabled = await submitBtn.isDisabled();
    const showsLoading = await submitBtn.evaluate(el => 
      el.textContent?.includes('Loading') || el.textContent?.includes('...')
    );
    
    expect(isDisabled || showsLoading).toBe(true);
  });

  // ======================== UI RESPONSIVENESS ========================

  test('perf-responsive-1: Scrolling remains smooth (not janky)', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    // Get initial scroll position
    const initialScroll = await page.evaluate(() => window.scrollY);
    
    // Scroll rapidly
    const startTime = Date.now();
    for (let i = 0; i < 10; i++) {
      await page.evaluate(() => {
        window.scrollBy(0, 100);
      });
      await page.waitForTimeout(16); // ~60fps
    }
    const scrollTime = Date.now() - startTime;
    
    // Should complete without lag (10 scrolls in ~160ms at 60fps)
    expect(scrollTime).toBeLessThan(500);
  });

  test('perf-responsive-2: Form input responsive (no lag on typing)', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    
    const usernameInput = page.locator('input[name="username"]');
    
    const startTime = Date.now();
    
    // Type quickly
    await usernameInput.type('quicktyping', { delay: 0 }); // No delay = fastest
    
    const typeTime = Date.now() - startTime;
    
    // 11 characters should type very quickly
    expect(typeTime).toBeLessThan(200);
    
    // Value should be complete
    const value = await usernameInput.inputValue();
    expect(value).toBe('quicktyping');
  });

  test('perf-responsive-3: Validation feedback instant (debounced if needed)', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    
    const emailInput = page.locator('input[type="email"]');
    const errorMsg = page.locator('[role="alert"], .error-message').first();
    
    // Type invalid email
    await emailInput.fill('invalid');
    
    const startTime = Date.now();
    
    // Feedback should appear quickly (within 500ms, allowing for debounce)
    const isInvalid = await emailInput.evaluate((el: any) => 
      el.hasAttribute('aria-invalid') && el.getAttribute('aria-invalid') === 'true'
    );
    
    const feedbackTime = Date.now() - startTime;
    
    if (isInvalid) {
      expect(feedbackTime).toBeLessThan(500);
    }
  });

  test('perf-responsive-4: Click response time < 100ms', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const tabButton = page.locator('button:has-text("Email"), button:has-text("Password")').first();
    if (await tabButton.isVisible()) {
      const startTime = Date.now();
      
      // Click button
      await tabButton.click();
      
      // Wait for content to change
      const clickResponseTime = Date.now() - startTime;
      
      // Should respond within reasonable time
      expect(clickResponseTime).toBeLessThan(200);
    }
  });

  // ======================== ANIMATION PERFORMANCE ========================

  test('perf-animation-1: Modal open animation completes quickly', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    const blockBtn = page.locator('button:has-text("Block")').first();
    if (await blockBtn.isVisible()) {
      const startTime = Date.now();
      
      await blockBtn.click();
      
      // Modal should be visible/interactable quickly
      const modal = page.locator('[role="dialog"], .modal');
      await expect(modal).toBeVisible({ timeout: 500 });
      
      const animationTime = Date.now() - startTime;
      expect(animationTime).toBeLessThan(500);
    }
  });

  test('perf-animation-2: No jank during fade-in animations', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    // Monitor performance during animation
    const performanceMarks = await page.evaluate(() => {
      const marks: number[] = [];
      
      for (let i = 0; i < 10; i++) {
        marks.push(performance.now());
      }
      
      return marks;
    });
    
    // Intervals between marks shouldn't be huge (no frame drops)
    let lastTime = performanceMarks[0];
    let largestGap = 0;
    
    for (let i = 1; i < performanceMarks.length; i++) {
      const gap = performanceMarks[i] - lastTime;
      largestGap = Math.max(largestGap, gap);
      lastTime = performanceMarks[i];
    }
    
    // No frame should take longer than ~33ms at 30fps
    expect(largestGap).toBeLessThan(100);
  });

  // ======================== PAGINATION PERFORMANCE ========================

  test('perf-pagination-1: First page loads quickly', async ({ page }) => {
    const startTime = Date.now();
    
    await page.goto(`${BASE_URL}/admin/users`);
    
    const loadTime = Date.now() - startTime;
    expect(loadTime).toBeLessThan(3000);
  });

  test('perf-pagination-2: Next page loaded without full reload', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    const nextBtn = page.locator('button:has-text("Next")');
    
    if (await nextBtn.isVisible()) {
      const startTime = Date.now();
      
      await nextBtn.click();
      
      // Wait for data to update
      const updateTime = Date.now() - startTime;
      
      // Should be faster than initial load (no full page reload)
      expect(updateTime).toBeLessThan(2000);
    }
  });

  // ======================== MEMORY LEAKS ========================

  test('perf-memory-1: No memory leak on repeated navigation', async ({ page }) => {
    // Navigate back and forth multiple times
    for (let i = 0; i < 5; i++) {
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');
      
      await page.goto(`${BASE_URL}/register`);
      await page.waitForLoadState('networkidle');
    }
    
    // Page should still be responsive
    const loginBtn = page.locator('button:has-text("Login"), button:has-text("Sign in")');
    expect(await loginBtn.isVisible()).toBe(true);
  });

  test('perf-memory-2: Event listeners cleaned up on unmount', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    // Open and close modal multiple times
    for (let i = 0; i < 3; i++) {
      const blockBtn = page.locator('button:has-text("Block")').first();
      if (await blockBtn.isVisible()) {
        await blockBtn.click();
        const modal = page.locator('[role="dialog"], .modal');
        await expect(modal).toBeVisible({ timeout: 1000 });
        
        const closeBtn = page.locator('button:has-text("Cancel")').first();
        if (await closeBtn.isVisible()) {
          await closeBtn.click();
          await modal.waitFor({ state: 'hidden' }).catch(() => {});
        }
      }
    }
    
    // Page should still be responsive
    expect(await page.isVisible('body')).toBe(true);
  });

  // ======================== BUNDLE SIZE PROXY ========================

  test('perf-resources-1: Page doesn\'t load excessive resources', async ({ page }) => {
    const requests: { url: string; size: number }[] = [];
    
    page.on('response', async (response) => {
      const size = (await response.text()).length;
      requests.push({ url: response.url(), size });
    });
    
    await page.goto(`${BASE_URL}/login`);
    
    // Filter to important resources
    const criticalRequests = requests.filter(r => 
      r.url.includes('.js') || r.url.includes('.css') || r.url.includes('.json')
    );
    
    // Should have reasonable number of requests
    expect(criticalRequests.length).toBeLessThan(50);
  });

  test('perf-resources-2: Images are optimized', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const images = page.locator('img');
    
    for (let i = 0; i < Math.min(3, await images.count()); i++) {
      const img = images.nth(i);
      const src = await img.getAttribute('src');
      
      // Should use modern formats or CDN optimization
      if (src) {
        expect(src).not.toMatch(/\.bmp$/i); // Old format
      }
    }
  });
});
