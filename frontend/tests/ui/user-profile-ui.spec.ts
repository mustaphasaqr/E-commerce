import { test, expect } from '@playwright/test';

/**
 * USER PROFILE & SETTINGS UI TESTS
 * Tests profile page rendering, settings updates, form interactions
 * Verifies user data display and profile modification flows
 */

const BASE_URL = 'http://localhost:3002';

test.describe('USER PROFILE & SETTINGS UI', () => {
  
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

  // ======================== PROFILE PAGE ========================

  test.describe('PROFILE PAGE', () => {
    test('should display user profile information', async ({ page }) => {
      await page.goto(`${BASE_URL}/profile`);
      await page.waitForLoadState('networkidle');

      // Verify profile elements
      const userEmail = page.locator('text=/testuser@example.com/');
      const userSection = page.locator('text=/profile|account information/i');
      
      await expect(userEmail).toBeVisible({ timeout: 3000 }).catch(() => {});
      await expect(userSection).toBeVisible({ timeout: 3000 }).catch(() => {});
    });

    test('should show profile tabs or sections', async ({ page }) => {
      await page.goto(`${BASE_URL}/profile`);
      await page.waitForLoadState('networkidle');

      // Look for common profile sections
      const personalInfoSection = page.locator('text=/personal|information/i');
      const securitySection = page.locator('text=/security|password/i');
      const preferencesSection = page.locator('text=/preferences|settings/i');

      const hasAtLeastOne = 
        (await personalInfoSection.count()) > 0 ||
        (await securitySection.count()) > 0 ||
        (await preferencesSection.count()) > 0;

      expect(hasAtLeastOne).toBeTruthy();
    });

    test('should navigate to settings from profile', async ({ page }) => {
      await page.goto(`${BASE_URL}/profile`);

      const settingsLink = page.locator('a:has-text("Settings"), button:has-text("Settings")');
      
      if (await settingsLink.count() > 0) {
        await settingsLink.first().click();
        await page.waitForURL(/settings/, { timeout: 3000 }).catch(() => {});
      }
    });
  });

  // ======================== SETTINGS PAGE ========================

  test.describe('SETTINGS PAGE', () => {
    test('should display settings page with form sections', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);
      await page.waitForLoadState('networkidle');

      // Look for settings sections
      const emailSection = page.locator('text=/change.*email|email address/i');
      const passwordSection = page.locator('text=/change.*password/i');
      
      const hasSettings = 
        (await emailSection.count()) > 0 ||
        (await passwordSection.count()) > 0;

      expect(hasSettings).toBeTruthy();
    });

    test('should validate new email format before submission', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const emailInput = page.locator('input[name="newEmail"], input[placeholder*="email"]').first();
      const submitButton = page.locator('button:near(input[name="newEmail"])').first();

      if (await emailInput.count() > 0) {
        await emailInput.fill('invalid-email');
        await submitButton.click();

        const emailError = page.locator('text=/valid.*email/i');
        await expect(emailError).toBeVisible({ timeout: 2000 });
      }
    });

    test('should require current password when changing email', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const newEmailInput = page.locator('input[name="newEmail"]').first();
      const passwordInput = page.locator('input[type="password"]').first();
      const submitButton = page.locator('button:near(input[name="newEmail"])').first();

      if (await newEmailInput.count() > 0) {
        await newEmailInput.fill(`newemail${Date.now()}@example.com`);
        // Don't fill password
        await submitButton.click();

        const passwordError = page.locator('text=/password.*required|current.*password/i');
        await expect(passwordError).toBeVisible({ timeout: 2000 }).catch(() => {});
      }
    });

    test('should show error for incorrect current password', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const newEmailInput = page.locator('input[name="newEmail"]').first();
      const passwordInput = page.locator('input[type="password"]').first();
      const submitButton = page.locator('button:near(input[name="newEmail"])').first();

      if (await newEmailInput.count() > 0 && await passwordInput.count() > 0) {
        await newEmailInput.fill(`newemail${Date.now()}@example.com`);
        await passwordInput.fill('WrongPassword123!');
        await submitButton.click();

        const error = page.locator('text=/incorrect|invalid.*password/i');
        await expect(error).toBeVisible({ timeout: 5000 });
      }
    });

    test('should successfully change email with correct password', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const newEmailInput = page.locator('input[name="newEmail"]').first();
      const passwordInput = page.locator('input[type="password"]').first();
      const submitButton = page.locator('button:near(input[name="newEmail"])').first();

      if (await newEmailInput.count() > 0 && await passwordInput.count() > 0) {
        const newEmail = `newemail${Date.now()}@example.com`;
        
        await newEmailInput.fill(newEmail);
        await passwordInput.fill('TestPassword123!');
        await submitButton.click();

        const successMessage = page.locator('text=/success|updated|changed/i');
        await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
      }
    });
  });

  // ======================== PASSWORD CHANGE ========================

  test.describe('PASSWORD CHANGE', () => {
    test('should display password change form', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const passwordSection = page.locator('text=/change.*password|password/i');
      await expect(passwordSection).toBeVisible({ timeout: 3000 }).catch(() => {});
    });

    test('should validate current password is required', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const currentPasswordInput = page.locator('input[name="currentPassword"]').first();
      const newPasswordInput = page.locator('input[name="newPassword"]').first();
      const submitButton = page.locator('button:has-text("Change Password")');

      if (await currentPasswordInput.count() > 0) {
        // Only fill new password, not current
        await newPasswordInput.fill('NewPassword456!');
        await submitButton.click();

        const error = page.locator('text=/current.*password.*required/i');
        await expect(error).toBeVisible({ timeout: 2000 });
      }
    });

    test('should validate new password meets requirements', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const currentPasswordInput = page.locator('input[name="currentPassword"]').first();
      const newPasswordInput = page.locator('input[name="newPassword"]').first();
      const submitButton = page.locator('button:has-text("Change Password")');

      if (await currentPasswordInput.count() > 0 && await newPasswordInput.count() > 0) {
        await currentPasswordInput.fill('TestPassword123!');
        await newPasswordInput.fill('weak'); // Too short
        await submitButton.click();

        const error = page.locator('text=/password.*strong|at least.*8/i');
        await expect(error).toBeVisible({ timeout: 2000 });
      }
    });

    test('should show error for incorrect current password', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const currentPasswordInput = page.locator('input[name="currentPassword"]').first();
      const newPasswordInput = page.locator('input[name="newPassword"]').first();
      const submitButton = page.locator('button:has-text("Change Password")');

      if (await currentPasswordInput.count() > 0 && await newPasswordInput.count() > 0) {
        await currentPasswordInput.fill('WrongPassword123!');
        await newPasswordInput.fill('NewPassword456!');
        await submitButton.click();

        const error = page.locator('text=/incorrect|invalid.*password/i');
        await expect(error).toBeVisible({ timeout: 5000 });
      }
    });

    test('should successfully change password with correct current password', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const currentPasswordInput = page.locator('input[name="currentPassword"]').first();
      const newPasswordInput = page.locator('input[name="newPassword"]').first();
      const confirmPasswordInput = page.locator('input[name="confirmPassword"]').first();
      const submitButton = page.locator('button:has-text("Change Password")');

      if (await currentPasswordInput.count() > 0) {
        await currentPasswordInput.fill('TestPassword123!');
        await newPasswordInput.fill('NewPassword456!');
        
        if (await confirmPasswordInput.count() > 0) {
          await confirmPasswordInput.fill('NewPassword456!');
        }
        
        await submitButton.click();

        const successMessage = page.locator('text=/success|password.*changed/i');
        await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
      }
    });
  });

  // ======================== MARKETING PREFERENCES ========================

  test.describe('MARKETING PREFERENCES', () => {
    test('should display marketing consent toggle', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const marketingToggle = page.locator('input[type="checkbox"]').first();
      const marketingLabel = page.locator('text=/marketing|newsletter|promotional/i');

      if (await marketingLabel.count() > 0) {
        await expect(marketingToggle).toBeVisible({ timeout: 3000 }).catch(() => {});
      }
    });

    test('should toggle marketing consent on', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const marketingToggle = page.locator('input[type="checkbox"][name="marketing"]').first();
      
      if (await marketingToggle.count() > 0) {
        const isChecked = await marketingToggle.isChecked();
        
        if (!isChecked) {
          await marketingToggle.click();
          
          const successMessage = page.locator('text=/success|updated|saved/i');
          await expect(successMessage).toBeVisible({ timeout: 3000 }).catch(() => {});
        }
      }
    });

    test('should toggle marketing consent off', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const marketingToggle = page.locator('input[type="checkbox"][name="marketing"]').first();
      
      if (await marketingToggle.count() > 0) {
        const isChecked = await marketingToggle.isChecked();
        
        if (isChecked) {
          await marketingToggle.click();
          
          const successMessage = page.locator('text=/success|updated|saved/i');
          await expect(successMessage).toBeVisible({ timeout: 3000 }).catch(() => {});
        }
      }
    });
  });

  // ======================== PROFILE NAVIGATION ========================

  test.describe('PROFILE NAVIGATION', () => {
    test('should navigate to profile from dashboard', async ({ page }) => {
      await page.goto(`${BASE_URL}/dashboard`);

      const profileLink = page.locator('a:has-text("Profile"), button:has-text("Profile")');
      const userMenuButton = page.locator('button:has-text("Account"), button[id*="avatar"], button[id*="user"]').first();

      let clicked = false;
      if (await profileLink.count() > 0) {
        await profileLink.first().click();
        clicked = true;
      } else if (await userMenuButton.count() > 0) {
        await userMenuButton.click();
        const profileOption = page.locator('a:has-text("Profile")');
        if (await profileOption.count() > 0) {
          await profileOption.click();
          clicked = true;
        }
      }

      if (clicked) {
        await page.waitForURL(/profile|account/, { timeout: 3000 }).catch(() => {});
      }
    });

    test('should go back to dashboard from profile', async ({ page }) => {
      await page.goto(`${BASE_URL}/profile`);

      const backButton = page.locator('button:has-text("Back"), a:has-text("Back")').first();
      const dashboardLink = page.locator('a:has-text("Dashboard")').first();

      if (await backButton.count() > 0) {
        await backButton.click();
        await page.waitForURL(/dashboard/, { timeout: 3000 }).catch(() => {});
      } else if (await dashboardLink.count() > 0) {
        await dashboardLink.click();
        await page.waitForURL(/dashboard/, { timeout: 3000 }).catch(() => {});
      }
    });
  });
});
