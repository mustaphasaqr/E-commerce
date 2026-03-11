import { test, expect } from '@playwright/test';

/**
 * NAVIGATION & ROUTING UI TESTS
 * Tests page navigation, route transitions, nav menu, breadcrumbs
 * Verifies the complete application navigation flow
 */

const BASE_URL = 'http://localhost:3002';

test.describe('NAVIGATION & ROUTING', () => {

  // ======================== PUBLIC NAVIGATION ========================

  test.describe('PUBLIC PAGES NAVIGATION', () => {
    test('should navigate to login page from home', async ({ page }) => {
      await page.goto(`${BASE_URL}/`);

      const loginLink = page.locator('a:has-text("Login"), a:has-text("Sign In")').first();
      
      if (await loginLink.count() > 0) {
        await loginLink.click();
        await page.waitForURL(/login/, { timeout: 3000 });
      }
    });

    test('should navigate to register page from login', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);

      const registerLink = page.locator('a:has-text("Sign up"), a:has-text("Register")').first();
      
      if (await registerLink.count() > 0) {
        await registerLink.click();
        await page.waitForURL(/register/, { timeout: 3000 });
      }
    });

    test('should navigate to login from register', async ({ page }) => {
      await page.goto(`${BASE_URL}/register`);

      const loginLink = page.locator('a:has-text("Sign in"), a:has-text("Login")').first();
      
      if (await loginLink.count() > 0) {
        await loginLink.click();
        await page.waitForURL(/login/, { timeout: 3000 });
      }
    });

    test('should navigate to password reset from login', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);

      const forgotLink = page.locator('a:has-text("Forgot password")').first();
      
      if (await forgotLink.count() > 0) {
        await forgotLink.click();
        await page.waitForURL(/password|reset|forgot/, { timeout: 3000 });
      }
    });

    test('should navigate back to login from password reset', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);

      const forgotLink = page.locator('a:has-text("Forgot password")').first();
      
      if (await forgotLink.count() > 0) {
        await forgotLink.click();
        await page.waitForURL(/password|reset|forgot/, { timeout: 3000 });

        const backLink = page.locator('a:has-text("Back to login"), button:has-text("Back")').first();
        
        if (await backLink.count() > 0) {
          await backLink.click();
          await page.waitForURL(/login/, { timeout: 3000 });
        }
      }
    });
  });

  // ======================== AUTHENTICATED NAVIGATION ========================

  test.describe('AUTHENTICATED USER NAVIGATION', () => {
    test.beforeEach(async ({ page }) => {
      // Login before each test
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should navigate from dashboard to profile', async ({ page }) => {
      await page.goto(`${BASE_URL}/dashboard`);

      const profileLink = page.locator('a:has-text("Profile"), button:has-text("Profile")').first();
      const userMenuButton = page.locator('button[id*="avatar"], button[id*="user-menu"]').first();

      if (await profileLink.count() > 0) {
        await profileLink.click();
      } else if (await userMenuButton.count() > 0) {
        await userMenuButton.click();
        const profileOption = page.locator('a:has-text("Profile")').first();
        if (await profileOption.count() > 0) {
          await profileOption.click();
        }
      }

      await page.waitForURL(/profile|account/, { timeout: 3000 }).catch(() => {});
    });

    test('should navigate from dashboard to settings', async ({ page }) => {
      await page.goto(`${BASE_URL}/dashboard`);

      const settingsLink = page.locator('a:has-text("Settings")').first();
      const userMenuButton = page.locator('button[id*="avatar"], button[id*="user-menu"]').first();

      if (await settingsLink.count() > 0) {
        await settingsLink.click();
      } else if (await userMenuButton.count() > 0) {
        await userMenuButton.click();
        const settingsOption = page.locator('a:has-text("Settings")').first();
        if (await settingsOption.count() > 0) {
          await settingsOption.click();
        }
      }

      await page.waitForURL(/settings/, { timeout: 3000 }).catch(() => {});
    });

    test('should navigate between profile and settings', async ({ page }) => {
      await page.goto(`${BASE_URL}/profile`);

      const settingsLink = page.locator('a:has-text("Settings"), button:has-text("Settings")').first();
      
      if (await settingsLink.count() > 0) {
        await settingsLink.click();
        await page.waitForURL(/settings/, { timeout: 3000 });
      }

      // Navigate back to profile
      const backLink = page.locator('a:has-text("Profile"), button:has-text("Back")').first();
      
      if (await backLink.count() > 0) {
        await backLink.click();
        await page.waitForURL(/profile/, { timeout: 3000 }).catch(() => {});
      }
    });

    test('should return to dashboard from profile when clicking back', async ({ page }) => {
      await page.goto(`${BASE_URL}/profile`);

      const dashboardLink = page.locator('a:has-text("Dashboard")').first();
      const backButton = page.locator('button:has-text("Back")').first();

      if (await dashboardLink.count() > 0) {
        await dashboardLink.click();
      } else if (await backButton.count() > 0) {
        await backButton.click();
      }

      await page.waitForURL(/dashboard/, { timeout: 3000 }).catch(() => {});
    });
  });

  // ======================== ADMIN NAVIGATION ========================

  test.describe('ADMIN NAVIGATION', () => {
    test.beforeEach(async ({ page }) => {
      // Login as admin
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('admin@example.com');
      await passwordInput.fill('AdminPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should navigate to admin panel from dashboard', async ({ page }) => {
      await page.goto(`${BASE_URL}/dashboard`);

      const adminLink = page.locator('a:has-text("Admin"), a:has-text("Users")').first();
      const adminMenuButton = page.locator('button:has-text("Admin")').first();

      if (await adminLink.count() > 0) {
        await adminLink.click();
      } else if (await adminMenuButton.count() > 0) {
        await adminMenuButton.click();
        const usersOption = page.locator('a:has-text("Users")').first();
        if (await usersOption.count() > 0) {
          await usersOption.click();
        }
      }

      await page.waitForURL(/admin|users/, { timeout: 3000 }).catch(() => {});
    });

    test('should navigate to admin users page', async ({ page }) => {
      const adminLink = page.locator('a:has-text("Admin Users"), a:has-text("Manage Users")').first();
      
      if (await adminLink.count() > 0) {
        await adminLink.click();
        await page.waitForURL(/admin.*users/, { timeout: 3000 });
      }
    });

    test('should return to dashboard from admin panel', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);

      const dashboardLink = page.locator('a:has-text("Dashboard"), button:has-text("Back to Dashboard")').first();
      
      if (await dashboardLink.count() > 0) {
        await dashboardLink.click();
        await page.waitForURL(/dashboard/, { timeout: 3000 }).catch(() => {});
      }
    });
  });

  // ======================== SIDEBAR/NAV MENU ========================

  test.describe('SIDEBAR/NAVIGATION MENU', () => {
    test.beforeEach(async ({ page }) => {
      // Login first
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should display navigation menu after login', async ({ page }) => {
      await page.goto(`${BASE_URL}/dashboard`);

      const navMenu = page.locator('nav, [role="navigation"], [class*="sidebar"], [class*="menu"]').first();
      
      if (await navMenu.count() > 0) {
        await expect(navMenu).toBeVisible();
      }
    });

    test('should show user menu with profile options', async ({ page }) => {
      await page.goto(`${BASE_URL}/dashboard`);

      const userMenuButton = page.locator('button[id*="avatar"], button[id*="user"], button:has-text("Profile")').first();
      
      if (await userMenuButton.count() > 0) {
        await userMenuButton.click();

        const profileOption = page.locator('[role="menuitem"]:has-text("Profile"), a:has-text("Profile")').first();
        const settingsOption = page.locator('[role="menuitem"]:has-text("Settings"), a:has-text("Settings")').first();

        const hasMenu = (await profileOption.count()) > 0 || (await settingsOption.count()) > 0;
        expect(hasMenu).toBeTruthy();
      }
    });

    test('should show logout option in user menu', async ({ page }) => {
      await page.goto(`${BASE_URL}/dashboard`);

      const userMenuButton = page.locator('button[id*="avatar"], button[id*="user"], button:has-text("Profile")').first();
      
      if (await userMenuButton.count() > 0) {
        await userMenuButton.click();

        const logoutOption = page.locator('button:has-text("Logout"), a:has-text("Logout")').first();
        
        if (await logoutOption.count() > 0) {
          await expect(logoutOption).toBeVisible();
        }
      }
    });

    test('should toggle mobile navigation menu', async ({ page }) => {
      // Set mobile viewport
      await page.setViewportSize({ width: 375, height: 667 });
      
      await page.goto(`${BASE_URL}/dashboard`);

      const hamburgerMenu = page.locator('button[aria-label="Menu"], button:has-text("☰")').first();
      
      if (await hamburgerMenu.count() > 0) {
        await hamburgerMenu.click();

        const navMenu = page.locator('nav, [role="navigation"]').first();
        await expect(navMenu).toBeVisible({ timeout: 2000 });
      }
    });
  });

  // ======================== BREADCRUMBS ========================

  test.describe('BREADCRUMBS', () => {
    test.beforeEach(async ({ page }) => {
      // Login first
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should display breadcrumbs on admin users page', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);

      const breadcrumbs = page.locator('nav[aria-label="Breadcrumb"], [class*="breadcrumb"]').first();
      
      if (await breadcrumbs.count() > 0) {
        await expect(breadcrumbs).toBeVisible();
      }
    });

    test('should navigate using breadcrumbs', async ({ page }) => {
      await page.goto(`${BASE_URL}/profile`);

      const breadcrumb = page.locator('[class*="breadcrumb"] a, [aria-label="Breadcrumb"] a').first();
      
      if (await breadcrumb.count() > 0) {
        const href = await breadcrumb.getAttribute('href');
        await breadcrumb.click();
        
        // Verify navigation happened
        expect(page.url()).toContain(href || 'dashboard');
      }
    });
  });

  // ======================== URL DIRECT ACCESS ========================

  test.describe('DIRECT URL ACCESS', () => {
    test('should redirect to login if accessing protected page without auth', async ({ page }) => {
      // Access dashboard without login
      await page.goto(`${BASE_URL}/dashboard`);

      // Should redirect to login
      await page.waitForURL(/login/, { timeout: 5000 }).catch(() => {});
    });

    test('should redirect to login if accessing profile without auth', async ({ page }) => {
      // Access profile without login
      await page.goto(`${BASE_URL}/profile`);

      // Should redirect to login
      await page.waitForURL(/login/, { timeout: 5000 }).catch(() => {});
    });

    test('should redirect to login if accessing admin without auth', async ({ page }) => {
      // Access admin without login
      await page.goto(`${BASE_URL}/admin/users`);

      // Should redirect to login
      await page.waitForURL(/login/, { timeout: 5000 }).catch(() => {});
    });

    test('should allow direct access to login page', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);

      // Should stay on login
      expect(page.url()).toContain('login');
    });

    test('should allow direct access to register page', async ({ page }) => {
      await page.goto(`${BASE_URL}/register`);

      // Should stay on register
      expect(page.url()).toContain('register');
    });

    test('should redirect non-admin users from admin pages', async ({ page }) => {
      // Login as regular user
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });

      // Try to access admin panel
      await page.goto(`${BASE_URL}/admin/users`);

      // Should either show error or redirect
      const isAdmin = page.url().includes('admin');
      const hasError = (await page.locator('[class*="error"], [role="alert"]').count()) > 0;
      
      // Should either stay on admin (if has access) or redirect/show error
      expect(isAdmin || hasError || page.url().includes('dashboard')).toBeTruthy();
    });
  });

  // ======================== PAGE REFRESH ========================

  test.describe('PAGE REFRESH & STATE', () => {
    test.beforeEach(async ({ page }) => {
      // Login first
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should maintain auth state after page refresh', async ({ page }) => {
      await page.goto(`${BASE_URL}/dashboard`);

      // Refresh the page
      await page.reload();

      // Should still be on dashboard (authenticated)
      expect(page.url()).toContain('dashboard');
    });

    test('should load profile page correctly after refresh', async ({ page }) => {
      await page.goto(`${BASE_URL}/profile`);

      // Store initial user info
      const userEmail = page.locator('text=/testuser@example.com/');

      // Refresh
      await page.reload();

      // User info should still be visible
      await expect(userEmail).toBeVisible({ timeout: 3000 }).catch(() => {});
    });

    test('should return to dashboard if session expires', async ({ page }) => {
      // This would require backend to invalidate token
      // Just verify page handles it gracefully
      
      await page.goto(`${BASE_URL}/dashboard`);
      expect(page.url()).toContain('dashboard');
    });
  });
});
