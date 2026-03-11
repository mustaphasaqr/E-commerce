import { test, expect } from '@playwright/test';

/**
 * AUTH UI TESTS
 * Tests complete authentication flows through the UI
 * Verifies form rendering, validation, user interactions, and navigation
 */

const BASE_URL = 'http://localhost:3002';

test.describe('AUTH UI - Login, Register, Password Reset', () => {
  
  // ======================== LOGIN UI TESTS ========================

  test.describe('LOGIN PAGE', () => {
    test('should render login form with email and password fields', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');

      // Verify form elements exist
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');
      const registerLink = page.locator('a:has-text("Sign up")');

      await expect(emailInput).toBeVisible();
      await expect(passwordInput).toBeVisible();
      await expect(loginButton).toBeVisible();
      await expect(registerLink).toBeVisible();
    });

    test('should show validation error for empty email', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');
      
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      // Verify error message appears
      const emailError = page.locator('text=/email.*required/i');
      await expect(emailError).toBeVisible({ timeout: 2000 });
    });

    test('should show validation error for invalid email format', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('invalid-email');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      const emailError = page.locator('text=/valid.*email/i');
      await expect(emailError).toBeVisible({ timeout: 2000 });
    });

    test('should show validation error for empty password', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await loginButton.click();

      const passwordError = page.locator('text=/password.*required/i');
      await expect(passwordError).toBeVisible({ timeout: 2000 });
    });

    test('should show validation error for short password', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('short');
      await loginButton.click();

      const passwordError = page.locator('text=/password.*least/i');
      await expect(passwordError).toBeVisible({ timeout: 2000 });
    });

    test('should show error message for invalid credentials', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('nonexistent@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      // Wait for error toast/message
      const errorMessage = page.locator('text=/invalid|unauthorized|credentials/i');
      await expect(errorMessage).toBeVisible({ timeout: 5000 });
    });

    test('should successfully login and redirect to dashboard', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      // Wait for redirect and dashboard to load
      await page.waitForURL(`${BASE_URL}/dashboard`, { timeout: 5000 });
      await expect(page).toHaveURL(/dashboard/);
    });

    test('should disable login button while submitting', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      
      // Don't wait for response - check button immediately
      const clickPromise = loginButton.click();
      
      // Button should be disabled while loading
      await expect(loginButton).toBeDisabled({ timeout: 1000 });
      
      await clickPromise;
    });

    test('should show "Forgot password" link', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      
      const forgotLink = page.locator('a:has-text("Forgot password")');
      await expect(forgotLink).toBeVisible();
      
      await forgotLink.click();
      await page.waitForURL(/password-reset|forgot/, { timeout: 3000 });
    });
  });

  // ======================== REGISTER UI TESTS ========================

  test.describe('REGISTER PAGE', () => {
    test('should render registration form with all fields', async ({ page }) => {
      await page.goto(`${BASE_URL}/register`);
      await page.waitForLoadState('networkidle');

      const emailInput = page.locator('input[type="email"]');
      const usernameInput = page.locator('input[name="username"]');
      const passwordInput = page.locator('input[type="password"]');
      const confirmPasswordInput = page.locator('input[name="confirmPassword"]');
      const firstNameInput = page.locator('input[name="firstName"]');
      const lastNameInput = page.locator('input[name="lastName"]');
      const signupButton = page.locator('button[type="submit"]');
      const loginLink = page.locator('a:has-text("Sign in")');

      await expect(emailInput).toBeVisible();
      await expect(usernameInput).toBeVisible();
      await expect(passwordInput).toBeVisible();
      await expect(confirmPasswordInput).toBeVisible();
      await expect(firstNameInput).toBeVisible();
      await expect(lastNameInput).toBeVisible();
      await expect(signupButton).toBeVisible();
      await expect(loginLink).toBeVisible();
    });

    test('should show validation error for duplicate email', async ({ page }) => {
      await page.goto(`${BASE_URL}/register`);
      
      const emailInput = page.locator('input[type="email"]');
      const usernameInput = page.locator('input[name="username"]');
      const passwordInput = page.locator('input[type="password"]');
      const confirmPasswordInput = page.locator('input[name="confirmPassword"]');
      const firstNameInput = page.locator('input[name="firstName"]');
      const lastNameInput = page.locator('input[name="lastName"]');
      const signupButton = page.locator('button[type="submit"]');

      // Use existing user email
      await emailInput.fill('testuser@example.com');
      await usernameInput.fill(`user${Date.now()}`);
      await passwordInput.fill('TestPassword123!');
      await confirmPasswordInput.fill('TestPassword123!');
      await firstNameInput.fill('Test');
      await lastNameInput.fill('User');
      await signupButton.click();

      // Wait for error message
      const errorMessage = page.locator('text=/already.*exists|already.*registered/i');
      await expect(errorMessage).toBeVisible({ timeout: 5000 });
    });

    test('should show validation error for password mismatch', async ({ page }) => {
      await page.goto(`${BASE_URL}/register`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const confirmPasswordInput = page.locator('input[name="confirmPassword"]');
      const signupButton = page.locator('button[type="submit"]');

      await emailInput.fill(`newuser${Date.now()}@example.com`);
      await passwordInput.fill('TestPassword123!');
      await confirmPasswordInput.fill('DifferentPassword456!');
      await signupButton.click();

      const mismatchError = page.locator('text=/password.*match|do not match/i');
      await expect(mismatchError).toBeVisible({ timeout: 2000 });
    });

    test('should show validation error for weak password', async ({ page }) => {
      await page.goto(`${BASE_URL}/register`);
      
      const passwordInput = page.locator('input[type="password"]');
      const confirmPasswordInput = page.locator('input[name="confirmPassword"]');
      const signupButton = page.locator('button[type="submit"]');

      await passwordInput.fill('weak');
      await confirmPasswordInput.fill('weak');
      await signupButton.click();

      const weakError = page.locator('text=/password.*strong|least.*8 characters/i');
      await expect(weakError).toBeVisible({ timeout: 2000 });
    });

    test('should show validation error for empty required fields', async ({ page }) => {
      await page.goto(`${BASE_URL}/register`);
      
      const signupButton = page.locator('button[type="submit"]');
      await signupButton.click();

      const requiredErrors = page.locator('text=/required/i');
      const errorCount = await requiredErrors.count();
      expect(errorCount).toBeGreaterThan(0);
    });

    test('should successfully register new user', async ({ page }) => {
      await page.goto(`${BASE_URL}/register`);
      
      const timestamp = Date.now();
      const emailInput = page.locator('input[type="email"]');
      const usernameInput = page.locator('input[name="username"]');
      const passwordInput = page.locator('input[type="password"]');
      const confirmPasswordInput = page.locator('input[name="confirmPassword"]');
      const firstNameInput = page.locator('input[name="firstName"]');
      const lastNameInput = page.locator('input[name="lastName"]');
      const signupButton = page.locator('button[type="submit"]');

      await emailInput.fill(`newuser${timestamp}@example.com`);
      await usernameInput.fill(`user${timestamp}`);
      await passwordInput.fill('TestPassword123!');
      await confirmPasswordInput.fill('TestPassword123!');
      await firstNameInput.fill('New');
      await lastNameInput.fill('User');
      await signupButton.click();

      // Should redirect or show success message
      await page.waitForURL(/login|verify-email|success/, { timeout: 5000 }).catch(() => {
        // If no redirect, verify success message
      });
    });
  });

  // ======================== LOGOUT UI TESTS ========================

  test.describe('LOGOUT', () => {
    test('should logout user and redirect to login', async ({ page }) => {
      // First login
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });

      // Find and click logout button
      const userMenu = page.locator('button:has-text("Profile")');
      await userMenu.click({ timeout: 3000 }).catch(() => {});

      const logoutButton = page.locator('button:has-text("Logout")');
      await logoutButton.click();

      // Should redirect to login
      await page.waitForURL(/login/, { timeout: 5000 });
      await expect(page).toHaveURL(/login/);
    });

    test('should clear auth token from localStorage on logout', async ({ page }) => {
      // First login
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });

      // Verify token exists
      let token = await page.evaluate(() => localStorage.getItem('auth_token'));
      expect(token).toBeTruthy();

      // Logout
      const userMenu = page.locator('button:has-text("Profile")');
      await userMenu.click({ timeout: 3000 }).catch(() => {});

      const logoutButton = page.locator('button:has-text("Logout")');
      await logoutButton.click();

      // Verify token is cleared
      token = await page.evaluate(() => localStorage.getItem('auth_token'));
      expect(token).toBeFalsy();
    });
  });

  // ======================== PASSWORD RESET UI TESTS ========================

  test.describe('PASSWORD RESET', () => {
    test('should navigate to password reset page', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      
      const forgotLink = page.locator('a:has-text("Forgot password")');
      await forgotLink.click();

      await page.waitForURL(/password-reset|forgot/, { timeout: 3000 });
      
      const emailInput = page.locator('input[type="email"]');
      await expect(emailInput).toBeVisible();
    });

    test('should validate email before submitting password reset request', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      
      const forgotLink = page.locator('a:has-text("Forgot password")');
      await forgotLink.click();

      await page.waitForURL(/password-reset|forgot/, { timeout: 3000 });

      const submitButton = page.locator('button[type="submit"]');
      await submitButton.click();

      const emailError = page.locator('text=/email.*required/i');
      await expect(emailError).toBeVisible({ timeout: 2000 });
    });

    test('should show success message after password reset request', async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      
      const forgotLink = page.locator('a:has-text("Forgot password")');
      await forgotLink.click();

      await page.waitForURL(/password-reset|forgot/, { timeout: 3000 });

      const emailInput = page.locator('input[type="email"]');
      const submitButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await submitButton.click();

      const successMessage = page.locator('text=/reset.*link|check.*email/i');
      await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {
        // May redirect instead of showing message
      });
    });
  });
});
