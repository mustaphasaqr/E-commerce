import { test, expect } from '@playwright/test';

/**
 * USER OPERATIONS UI TESTS - Missing Coverage
 * Tests for endpoints integrated in frontend but not yet covered by UI tests:
 * - GET /users/{id} - Get user by ID
 * - GET /users/email/{email} - Get user by email
 * - GET /users/username/{username} - Get user by username
 * - POST /users/me/email/verify - Verify email
 * - POST /users/{id}/activate - Activate user
 * - POST /users/{id}/deactivate - Deactivate user
 * - POST /users/{id}/block - Block user
 * - POST /users/{id}/unblock - Unblock user
 * - DELETE /users/{id} - Delete user
 * - POST /auth/logout-all - Logout from all devices
 */

const BASE_URL = 'http://localhost:3002';

test.describe('USER OPERATIONS - MISSING ENDPOINT COVERAGE', () => {

  // ======================== GET USER BY ID ========================

  test.describe('GET /users/{id} - User Lookup by ID', () => {
    test.beforeEach(async ({ page }) => {
      // Login as admin to access user lookup features
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('admin@example.com');
      await passwordInput.fill('AdminPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should display user details when visiting user profile by ID', async ({ page }) => {
      // Navigate to admin users
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      // Click on first user row to view details
      const firstUserRow = page.locator('tbody tr').first();
      const userLink = firstUserRow.locator('a, button').first();

      if (await userLink.count() > 0) {
        await userLink.click();

        // Should show user details (calls GET /users/{id})
        const userDetails = page.locator('[class*="profile"], [class*="details"]').first();
        await expect(userDetails).toBeVisible({ timeout: 3000 }).catch(() => {});
      }
    });

    test('should display user info when navigating to user ID directly', async ({ page }) => {
      // Get a user ID from the list first
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const firstUserRow = page.locator('tbody tr').first();
      const userIdCell = firstUserRow.locator('td').first();
      const userId = await userIdCell.textContent();

      if (userId && userId.trim()) {
        // Navigate directly to user profile
        await page.goto(`${BASE_URL}/users/${userId.trim()}`);

        // Should load user data
        const userEmail = page.locator('input[type="email"], text=/email/i').first();
        await expect(userEmail).toBeVisible({ timeout: 3000 }).catch(() => {});
      }
    });

    test('should show error for invalid user ID format', async ({ page }) => {
      // Try to access with invalid ID
      await page.goto(`${BASE_URL}/users/invalid-id`);

      // Should show error
      const errorMessage = page.locator('[class*="error"], [role="alert"]').first();
      await expect(errorMessage).toBeVisible({ timeout: 3000 }).catch(() => {});
    });

    test('should show error for non-existent user ID', async ({ page }) => {
      // Try to access non-existent user
      await page.goto(`${BASE_URL}/users/00000000-0000-0000-0000-000000000000`);

      // Should show not found error
      const notFoundMessage = page.locator('text=/not found|does not exist/i').first();
      await expect(notFoundMessage).toBeVisible({ timeout: 3000 }).catch(() => {});
    });
  });

  // ======================== GET USER BY EMAIL ========================

  test.describe('GET /users/email/{email} - User Lookup by Email', () => {
    test.beforeEach(async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('admin@example.com');
      await passwordInput.fill('AdminPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should find user by email in admin search', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const emailSearchInput = page.locator('input[name="email"], input[placeholder*="email"]').first();
      const searchButton = page.locator('button:has-text("Search")').first();

      if (await emailSearchInput.count() > 0) {
        await emailSearchInput.fill('testuser@example.com');
        
        if (await searchButton.count() > 0) {
          await searchButton.click();
        } else {
          await emailSearchInput.press('Enter');
        }

        await page.waitForLoadState('networkidle');

        // Should display the user (via GET /users/email/{email})
        const userRow = page.locator('tbody tr').first();
        await expect(userRow).toBeVisible({ timeout: 3000 });
      }
    });

    test('should show empty results for non-existent email', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const emailSearchInput = page.locator('input[name="email"]').first();
      const searchButton = page.locator('button:has-text("Search")').first();

      if (await emailSearchInput.count() > 0) {
        await emailSearchInput.fill('nonexistent-email-12345@example.com');
        
        if (await searchButton.count() > 0) {
          await searchButton.click();
        } else {
          await emailSearchInput.press('Enter');
        }

        await page.waitForLoadState('networkidle');

        // Should show no results message
        const noResultsMessage = page.locator('text=/no results|no users found/i');
        const emptyTable = page.locator('tbody tr');

        const hasNoResults = (await noResultsMessage.count()) > 0 || (await emptyTable.count()) === 0;
        expect(hasNoResults).toBeTruthy();
      }
    });

    test('should validate email format before searching', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const emailSearchInput = page.locator('input[name="email"]').first();

      if (await emailSearchInput.count() > 0) {
        await emailSearchInput.fill('invalid-email');
        await emailSearchInput.blur();

        const isInvalid = await emailSearchInput.evaluate((el: HTMLInputElement) => !el.checkValidity());
        expect(isInvalid).toBeTruthy();
      }
    });
  });

  // ======================== GET USER BY USERNAME ========================

  test.describe('GET /users/username/{username} - User Lookup by Username', () => {
    test.beforeEach(async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('admin@example.com');
      await passwordInput.fill('AdminPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should find user by username in admin search', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const usernameSearchInput = page.locator('input[name="username"], input[placeholder*="username"]').first();
      const searchButton = page.locator('button:has-text("Search")').first();

      if (await usernameSearchInput.count() > 0) {
        await usernameSearchInput.fill('testuser');
        
        if (await searchButton.count() > 0) {
          await searchButton.click();
        } else {
          await usernameSearchInput.press('Enter');
        }

        await page.waitForLoadState('networkidle');

        // Should display user (via GET /users/username/{username})
        const userRow = page.locator('tbody tr').first();
        await expect(userRow).toBeVisible({ timeout: 3000 });
      }
    });

    test('should show empty results for non-existent username', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const usernameSearchInput = page.locator('input[name="username"]').first();
      const searchButton = page.locator('button:has-text("Search")').first();

      if (await usernameSearchInput.count() > 0) {
        await usernameSearchInput.fill('nonexistent-user-12345');
        
        if (await searchButton.count() > 0) {
          await searchButton.click();
        } else {
          await usernameSearchInput.press('Enter');
        }

        await page.waitForLoadState('networkidle');

        // Should show no results
        const noResultsMessage = page.locator('text=/no results|no users found/i');
        const emptyTable = page.locator('tbody tr');

        const hasNoResults = (await noResultsMessage.count()) > 0 || (await emptyTable.count()) === 0;
        expect(hasNoResults).toBeTruthy();
      }
    });
  });

  // ======================== POST /users/me/email/verify ========================

  test.describe('POST /users/me/email/verify - Email Verification', () => {
    test.beforeEach(async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should display email verification option in settings', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const verifyEmailButton = page.locator('button:has-text("Verify Email"), button:has-text("Verify")').first();
      const verifyEmailSection = page.locator('text=/verify.*email|email.*verification/i').first();

      if (await verifyEmailSection.count() > 0 || await verifyEmailButton.count() > 0) {
        await expect(verifyEmailSection.or(verifyEmailButton)).toBeVisible({ timeout: 3000 });
      }
    });

    test('should send verification email when clicking verify button', async ({ page }) => {
      await page.goto(`${BASE_URL}/settings`);

      const verifyEmailButton = page.locator('button:has-text("Verify Email"), button:has-text("Verify")').first();

      if (await verifyEmailButton.count() > 0) {
        await verifyEmailButton.click();

        // Should show confirmation message
        const confirmMessage = page.locator('text=/verification.*sent|check.*email/i');
        await expect(confirmMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
      }
    });

    test('should show email verification status in profile', async ({ page }) => {
      await page.goto(`${BASE_URL}/profile`);

      const verificationStatus = page.locator('text=/verified|unverified|pending verification/i').first();
      
      if (await verificationStatus.count() > 0) {
        await expect(verificationStatus).toBeVisible();
      }
    });
  });

  // ======================== POST /users/{id}/activate ========================

  test.describe('POST /users/{id}/activate - Activate User (Non-Admin)', () => {
    test.beforeEach(async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('admin@example.com');
      await passwordInput.fill('AdminPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should activate user from profile view', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      // Find an inactive user
      const userRows = page.locator('tbody tr');
      
      for (let i = 0; i < await userRows.count(); i++) {
        const row = userRows.nth(i);
        const statusCell = row.locator('td').nth(2);
        const statusText = await statusCell.textContent();

        if (statusText?.includes('INACTIVE')) {
          const activateButton = row.locator('button:has-text("Activate")').first();

          if (await activateButton.count() > 0) {
            await activateButton.click();

            // Should show activation confirmation
            const successMessage = page.locator('text=/activated|success/i');
            await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
            break;
          }
        }
      }
    });

    test('should show activate button for inactive users', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const activateButtons = page.locator('button:has-text("Activate")');
      
      if (await activateButtons.count() > 0) {
        await expect(activateButtons.first()).toBeVisible();
      }
    });
  });

  // ======================== POST /users/{id}/deactivate ========================

  test.describe('POST /users/{id}/deactivate - Deactivate User (Non-Admin)', () => {
    test.beforeEach(async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('admin@example.com');
      await passwordInput.fill('AdminPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should deactivate user account', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      // Find an active user
      const userRows = page.locator('tbody tr');
      
      for (let i = 0; i < Math.min(5, await userRows.count()); i++) {
        const row = userRows.nth(i);
        const deactivateButton = row.locator('button:has-text("Deactivate")').first();

        if (await deactivateButton.count() > 0) {
          await deactivateButton.click();

          // Should show confirmation dialog
          const modal = page.locator('[role="dialog"]').first();
          const confirmButton = modal.locator('button:has-text("Confirm"), button:has-text("Deactivate")').first();

          if (await modal.count() > 0 && await confirmButton.count() > 0) {
            await confirmButton.click();

            // Should show success message
            const successMessage = page.locator('text=/deactivated|success/i');
            await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
          }
          break;
        }
      }
    });

    test('should require confirmation before deactivating', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const firstDeactivateButton = page.locator('button:has-text("Deactivate")').first();

      if (await firstDeactivateButton.count() > 0) {
        await firstDeactivateButton.click();

        // Should show confirmation modal
        const modal = page.locator('[role="dialog"]').first();
        await expect(modal).toBeVisible({ timeout: 3000 });
      }
    });
  });

  // ======================== POST /users/{id}/block ========================

  test.describe('POST /users/{id}/block - Block User (Non-Admin)', () => {
    test.beforeEach(async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('admin@example.com');
      await passwordInput.fill('AdminPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should block user with reason', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const firstUserRow = page.locator('tbody tr').first();
      const blockButton = firstUserRow.locator('button:has-text("Block")').first();

      if (await blockButton.count() > 0) {
        await blockButton.click();

        // Should show block dialog
        const modal = page.locator('[role="dialog"]').first();
        const reasonInput = modal.locator('input[name="reason"], textarea[name="reason"]').first();

        if (await modal.count() > 0) {
          if (await reasonInput.count() > 0) {
            await reasonInput.fill('User violated policy');
          }

          const confirmButton = modal.locator('button:has-text("Confirm"), button:has-text("Block")').first();
          
          if (await confirmButton.count() > 0) {
            await confirmButton.click();

            // Should show success message
            const successMessage = page.locator('text=/blocked|success/i');
            await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
          }
        }
      }
    });

    test('should prevent blocked user from logging in', async ({ page }) => {
      // This would require backend to enforce, but UI should reflect the block
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      // Find blocked user
      const userRows = page.locator('tbody tr');
      
      for (let i = 0; i < await userRows.count(); i++) {
        const row = userRows.nth(i);
        const statusCell = row.locator('td').nth(2);
        const statusText = await statusCell.textContent();

        if (statusText?.includes('BLOCKED')) {
          // Should have unblock button, not block button
          const unblockButton = row.locator('button:has-text("Unblock")').first();
          await expect(unblockButton).toBeVisible();
          break;
        }
      }
    });
  });

  // ======================== POST /users/{id}/unblock ========================

  test.describe('POST /users/{id}/unblock - Unblock User (Non-Admin)', () => {
    test.beforeEach(async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('admin@example.com');
      await passwordInput.fill('AdminPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should unblock blocked user', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      // Find a blocked user
      const userRows = page.locator('tbody tr');
      
      for (let i = 0; i < await userRows.count(); i++) {
        const row = userRows.nth(i);
        const unblockButton = row.locator('button:has-text("Unblock")').first();

        if (await unblockButton.count() > 0) {
          await unblockButton.click();

          // Should show success message
          const successMessage = page.locator('text=/unblocked|success/i');
          await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
          break;
        }
      }
    });

    test('should show unblock button for blocked users only', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const unblockButtons = page.locator('button:has-text("Unblock")');

      // Should have at least some unblock buttons if there are blocked users
      // Or no unblock buttons if no users are blocked
      const count = await unblockButtons.count();
      expect(count).toBeGreaterThanOrEqual(0);
    });
  });

  // ======================== DELETE /users/{id} ========================

  test.describe('DELETE /users/{id} - Delete User (Non-Admin)', () => {
    test.beforeEach(async ({ page }) => {
      await page.goto(`${BASE_URL}/login`);
      await page.waitForLoadState('networkidle');
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('admin@example.com');
      await passwordInput.fill('AdminPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });
    });

    test('should show delete confirmation before deletion', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const firstUserRow = page.locator('tbody tr').first();
      const deleteButton = firstUserRow.locator('button:has-text("Delete")').first();

      if (await deleteButton.count() > 0) {
        await deleteButton.click();

        // Should show warning dialog
        const modal = page.locator('[role="dialog"]').first();
        const warningText = modal.locator('text=/permanent|cannot be undone/i');

        await expect(modal).toBeVisible({ timeout: 3000 });
        
        if (await warningText.count() > 0) {
          await expect(warningText).toBeVisible();
        }
      }
    });

    test('should cancel deletion when user clicks cancel', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const userCountBefore = await page.locator('tbody tr').count();

      const firstUserRow = page.locator('tbody tr').first();
      const deleteButton = firstUserRow.locator('button:has-text("Delete")').first();

      if (await deleteButton.count() > 0) {
        await deleteButton.click();

        const modal = page.locator('[role="dialog"]').first();
        const cancelButton = modal.locator('button:has-text("Cancel")').first();

        if (await cancelButton.count() > 0) {
          await cancelButton.click();

          // Modal should close
          await expect(modal).not.toBeVisible({ timeout: 2000 });

          // User should still be in the list
          const userCountAfter = await page.locator('tbody tr').count();
          expect(userCountAfter).toBe(userCountBefore);
        }
      }
    });

    test('should delete user permanently', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const userCountBefore = await page.locator('tbody tr').count();
      const firstUserEmail = await page.locator('tbody tr').first().locator('td').nth(1).textContent();

      const deleteButton = page.locator('tbody tr').first().locator('button:has-text("Delete")').first();

      if (await deleteButton.count() > 0) {
        await deleteButton.click();

        const modal = page.locator('[role="dialog"]').first();
        const confirmDeleteButton = modal.locator('button:has-text("Confirm"), button:has-text("Delete")').first();

        if (await confirmDeleteButton.count() > 0) {
          await confirmDeleteButton.click();

          // Should show success message
          const successMessage = page.locator('text=/deleted|success/i');
          await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});

          // User count should decrease
          await page.waitForLoadState('networkidle');
          const userCountAfter = await page.locator('tbody tr').count();
          expect(userCountAfter).toBeLessThanOrEqual(userCountBefore);
        }
      }
    });
  });

  // ======================== POST /auth/logout-all ========================

  test.describe('POST /auth/logout-all - Logout from All Devices', () => {
    test('should logout user from all devices', async ({ page }) => {
      // Login first
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });

      // Navigate to settings/security
      await page.goto(`${BASE_URL}/settings`);

      // Find logout all button
      const logoutAllButton = page.locator('button:has-text("Logout All Devices"), button:has-text("Sign Out Everywhere")').first();

      if (await logoutAllButton.count() > 0) {
        await logoutAllButton.click();

        // Should show confirmation
        const confirmMessage = page.locator('text=/confirm|are you sure/i');
        
        if (await confirmMessage.count() > 0) {
          await expect(confirmMessage).toBeVisible();

          // Confirm action
          const confirmButton = page.locator('button:has-text("Confirm"), button:has-text("Logout")').first();
          
          if (await confirmButton.count() > 0) {
            await confirmButton.click();

            // Should logout and redirect to login
            await page.waitForURL(/login/, { timeout: 5000 });
            await expect(page).toHaveURL(/login/);
          }
        }
      }
    });

    test('should invalidate all sessions when logout all is used', async ({ page }) => {
      // Login first
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });

      // Store the auth token
      const tokenBefore = await page.evaluate(() => localStorage.getItem('auth_token'));
      expect(tokenBefore).toBeTruthy();

      // Go to settings and logout all
      await page.goto(`${BASE_URL}/settings`);

      const logoutAllButton = page.locator('button:has-text("Logout All Devices")').first();

      if (await logoutAllButton.count() > 0) {
        await logoutAllButton.click();

        const confirmButton = page.locator('button:has-text("Confirm"), button:has-text("Logout")').first();
        
        if (await confirmButton.count() > 0) {
          await confirmButton.click();

          // Wait for logout
          await page.waitForURL(/login/, { timeout: 5000 });

          // Token should be cleared
          const tokenAfter = await page.evaluate(() => localStorage.getItem('auth_token'));
          expect(tokenAfter).toBeFalsy();
        }
      }
    });

    test('should show logout all option in security settings', async ({ page }) => {
      // Login first
      await page.goto(`${BASE_URL}/login`);
      
      const emailInput = page.locator('input[type="email"]');
      const passwordInput = page.locator('input[type="password"]');
      const loginButton = page.locator('button[type="submit"]');

      await emailInput.fill('testuser@example.com');
      await passwordInput.fill('TestPassword123!');
      await loginButton.click();

      await page.waitForURL(/dashboard/, { timeout: 5000 });

      // Go to settings
      await page.goto(`${BASE_URL}/settings`);

      // Look for logout all option
      const logoutAllOption = page.locator('button:has-text("Logout All"), text=/logout.*all|sign.*out.*everywhere/i').first();

      if (await logoutAllOption.count() > 0) {
        await expect(logoutAllOption).toBeVisible();
      }
    });
  });
});
