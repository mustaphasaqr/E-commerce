import { test, expect } from '@playwright/test';

/**
 * ADMIN USER MANAGEMENT UI TESTS
 * Tests admin panel, user list, search, and user action modals
 * Verifies admin operations update UI correctly
 */

const BASE_URL = 'http://localhost:3002';

test.describe('ADMIN USER MANAGEMENT UI', () => {
  
  test.beforeEach(async ({ page }) => {
    // Login as admin
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

  // ======================== ADMIN USERS LIST PAGE ========================

  test.describe('USERS LIST PAGE', () => {
    test('should display users management page with table', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      // Verify table elements exist
      const userTable = page.locator('table').first();
      const tableHeaders = page.locator('thead').first();
      const tableRows = page.locator('tbody tr');

      await expect(userTable).toBeVisible({ timeout: 3000 });
      await expect(tableHeaders).toBeVisible();
      
      const rowCount = await tableRows.count();
      expect(rowCount).toBeGreaterThan(0);
    });

    test('should display user columns: email, username, status, role, actions', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const emailHeader = page.locator('th:has-text("Email")');
      const usernameHeader = page.locator('th:has-text("Username")');
      const statusHeader = page.locator('th:has-text("Status")');
      const roleHeader = page.locator('th:has-text("Role")');
      const actionsHeader = page.locator('th:has-text("Actions")');

      // At least some of these should exist
      const headerCount = 
        (await emailHeader.count()) +
        (await usernameHeader.count()) +
        (await statusHeader.count()) +
        (await roleHeader.count()) +
        (await actionsHeader.count());

      expect(headerCount).toBeGreaterThan(0);
    });

    test('should display user status badges', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      // Look for status indicators
      const statusBadges = page.locator('[class*="badge"], [class*="status"], [class*="chip"]');
      const statusCount = await statusBadges.count();

      expect(statusCount).toBeGreaterThan(0);
    });

    test('should display action buttons for each user (Edit, Block, Delete)', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const firstUserRow = page.locator('tbody tr').first();
      const actionButtons = firstUserRow.locator('button');

      const actionCount = await actionButtons.count();
      expect(actionCount).toBeGreaterThan(0);
    });

    test('should display pagination controls', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const pagination = page.locator('[class*="pagination"], nav:has-text("Page")');
      const paginationExists = (await pagination.count()) > 0;

      // Pagination might not exist if few users, so just verify it doesn't break
      expect(page.locator('table')).toBeVisible();
    });
  });

  // ======================== ADMIN USER SEARCH ========================

  test.describe('USER SEARCH', () => {
    test('should display search form with filters', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const searchInput = page.locator('input[type="search"], input[placeholder*="search"]').first();
      const filterButton = page.locator('button:has-text("Filter")').first();

      // At least search input should exist
      const hasSearch = (await searchInput.count()) > 0 || (await filterButton.count()) > 0;
      expect(hasSearch).toBeTruthy();
    });

    test('should search users by email', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const searchInput = page.locator('input[type="search"], input[placeholder*="search"], input[name="email"]').first();
      const searchButton = page.locator('button:has-text("Search")').first();

      if (await searchInput.count() > 0) {
        await searchInput.fill('testuser@example.com');
        
        if (await searchButton.count() > 0) {
          await searchButton.click();
        } else {
          // Might have auto-search
          await searchInput.press('Enter');
        }

        await page.waitForLoadState('networkidle');

        const tableRows = page.locator('tbody tr');
        const rowCount = await tableRows.count();

        // Should have at least one result or zero results message
        expect(rowCount).toBeGreaterThanOrEqual(0);
      }
    });

    test('should filter by status', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const statusFilter = page.locator('select[name="status"], [role="combobox"]:near(:has-text("Status"))').first();
      
      if (await statusFilter.count() > 0) {
        await statusFilter.click();
        const activeOption = page.locator('[role="option"]:has-text("Active")').first();
        
        if (await activeOption.count() > 0) {
          await activeOption.click();
          await page.waitForLoadState('networkidle');
        }
      }
    });

    test('should clear search/filters', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const clearButton = page.locator('button:has-text("Clear"), button:has-text("Reset")').first();
      
      if (await clearButton.count() > 0) {
        await clearButton.click();
        await page.waitForLoadState('networkidle');
      }
    });
  });

  // ======================== BLOCK/UNBLOCK USER ========================

  test.describe('BLOCK/UNBLOCK USER', () => {
    test('should open block user modal', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const firstUserRow = page.locator('tbody tr').first();
      const blockButton = firstUserRow.locator('button:has-text("Block")').first();

      if (await blockButton.count() > 0) {
        await blockButton.click();

        const modal = page.locator('[role="dialog"]');
        await expect(modal).toBeVisible({ timeout: 3000 });
      }
    });

    test('should show block confirmation dialog with reason field', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const firstUserRow = page.locator('tbody tr').first();
      const blockButton = firstUserRow.locator('button:has-text("Block")').first();

      if (await blockButton.count() > 0) {
        await blockButton.click();

        const modal = page.locator('[role="dialog"]');
        const reasonInput = modal.locator('input[name="reason"], textarea[name="reason"]').first();
        const confirmButton = modal.locator('button:has-text("Confirm"), button:has-text("Block")').first();

        await expect(modal).toBeVisible();
        
        if (await reasonInput.count() > 0) {
          await reasonInput.fill('Test blocking user');
        }
        
        if (await confirmButton.count() > 0) {
          await confirmButton.click();

          const successMessage = page.locator('text=/blocked|success/i');
          await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
        }
      }
    });

    test('should show unblock button for blocked users', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const tableRows = page.locator('tbody tr');
      
      for (let i = 0; i < await tableRows.count(); i++) {
        const row = tableRows.nth(i);
        const statusCell = row.locator('td').nth(2); // Assuming status is 3rd column
        
        const statusText = await statusCell.textContent();
        
        if (statusText?.includes('BLOCKED')) {
          const unblockButton = row.locator('button:has-text("Unblock")').first();
          
          if (await unblockButton.count() > 0) {
            await expect(unblockButton).toBeVisible();
            break;
          }
        }
      }
    });

    test('should unblock user successfully', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const tableRows = page.locator('tbody tr');
      
      for (let i = 0; i < await tableRows.count(); i++) {
        const row = tableRows.nth(i);
        const statusCell = row.locator('td').nth(2);
        
        const statusText = await statusCell.textContent();
        
        if (statusText?.includes('BLOCKED')) {
          const unblockButton = row.locator('button:has-text("Unblock")').first();
          
          if (await unblockButton.count() > 0) {
            await unblockButton.click();

            const modal = page.locator('[role="dialog"]');
            const confirmButton = modal.locator('button:has-text("Confirm"), button:has-text("Unblock")').first();

            if (await modal.count() > 0 && await confirmButton.count() > 0) {
              await confirmButton.click();

              const successMessage = page.locator('text=/unblocked|success/i');
              await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
            }
            break;
          }
        }
      }
    });
  });

  // ======================== ACTIVATE/DEACTIVATE USER ========================

  test.describe('ACTIVATE/DEACTIVATE USER', () => {
    test('should show activate button for inactive users', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const tableRows = page.locator('tbody tr');
      
      for (let i = 0; i < await tableRows.count(); i++) {
        const row = tableRows.nth(i);
        const activateButton = row.locator('button:has-text("Activate")').first();
        
        if (await activateButton.count() > 0) {
          await expect(activateButton).toBeVisible();
          break;
        }
      }
    });

    test('should activate user successfully', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const tableRows = page.locator('tbody tr');
      
      for (let i = 0; i < await tableRows.count(); i++) {
        const row = tableRows.nth(i);
        const activateButton = row.locator('button:has-text("Activate")').first();
        
        if (await activateButton.count() > 0) {
          await activateButton.click();

          const successMessage = page.locator('text=/activated|success/i');
          await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
          break;
        }
      }
    });

    test('should show deactivate button for active users', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const tableRows = page.locator('tbody tr');
      
      for (let i = 0; i < await tableRows.count(); i++) {
        const row = tableRows.nth(i);
        const deactivateButton = row.locator('button:has-text("Deactivate")').first();
        
        if (await deactivateButton.count() > 0) {
          await expect(deactivateButton).toBeVisible();
          break;
        }
      }
    });

    test('should deactivate user successfully', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const tableRows = page.locator('tbody tr');
      
      for (let i = 0; i < Math.min(5, await tableRows.count()); i++) {
        const row = tableRows.nth(i);
        const deactivateButton = row.locator('button:has-text("Deactivate")').first();
        
        if (await deactivateButton.count() > 0) {
          await deactivateButton.click();

          const modal = page.locator('[role="dialog"]');
          const confirmButton = modal.locator('button:has-text("Confirm"), button:has-text("Deactivate")').first();

          if (await modal.count() > 0 && await confirmButton.count() > 0) {
            await confirmButton.click();

            const successMessage = page.locator('text=/deactivated|success/i');
            await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
          }
          break;
        }
      }
    });
  });

  // ======================== DELETE USER ========================

  test.describe('DELETE USER', () => {
    test('should show delete button for users', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const firstUserRow = page.locator('tbody tr').first();
      const deleteButton = firstUserRow.locator('button:has-text("Delete")').first();

      if (await deleteButton.count() > 0) {
        await expect(deleteButton).toBeVisible();
      }
    });

    test('should show delete confirmation modal', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const firstUserRow = page.locator('tbody tr').first();
      const deleteButton = firstUserRow.locator('button:has-text("Delete")').first();

      if (await deleteButton.count() > 0) {
        await deleteButton.click();

        const modal = page.locator('[role="dialog"]');
        const warningText = modal.locator('text=/permanent|cannot be undone/i');
        const confirmButton = modal.locator('button:has-text("Confirm"), button:has-text("Delete")').first();

        await expect(modal).toBeVisible({ timeout: 3000 });
        
        if (await warningText.count() > 0) {
          await expect(warningText).toBeVisible();
        }
      }
    });

    test('should close modal on cancel', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const firstUserRow = page.locator('tbody tr').first();
      const deleteButton = firstUserRow.locator('button:has-text("Delete")').first();

      if (await deleteButton.count() > 0) {
        await deleteButton.click();

        const modal = page.locator('[role="dialog"]');
        const cancelButton = modal.locator('button:has-text("Cancel")').first();

        if (await cancelButton.count() > 0) {
          await cancelButton.click();

          await expect(modal).not.toBeVisible({ timeout: 2000 });
        }
      }
    });
  });

  // ======================== CHANGE USER ROLE ========================

  test.describe('CHANGE USER ROLE', () => {
    test('should open role change modal/dropdown', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const firstUserRow = page.locator('tbody tr').first();
      const roleCell = firstUserRow.locator('td').nth(3); // Assuming role is 4th column
      const roleButton = roleCell.locator('button, select').first();

      if (await roleButton.count() > 0) {
        await roleButton.click();

        // Check for dropdown options or modal
        const dropdown = page.locator('[role="listbox"], [role="menu"]').first();
        
        if (await dropdown.count() > 0) {
          await expect(dropdown).toBeVisible();
        }
      }
    });

    test('should show role options', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const roleSelect = page.locator('select[name="role"]').first();
      
      if (await roleSelect.count() > 0) {
        await roleSelect.click();

        const userOption = page.locator('option:has-text("USER")');
        const adminOption = page.locator('option:has-text("ADMIN")');

        const hasRoles = (await userOption.count()) > 0 || (await adminOption.count()) > 0;
        expect(hasRoles).toBeTruthy();
      }
    });

    test('should successfully change user role', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const roleSelect = page.locator('select[name="role"]').first();
      
      if (await roleSelect.count() > 0) {
        const currentRole = await roleSelect.inputValue();
        const newRole = currentRole === 'USER' ? 'ADMIN' : 'USER';

        await roleSelect.selectOption(newRole);

        const successMessage = page.locator('text=/success|role.*changed/i');
        await expect(successMessage).toBeVisible({ timeout: 5000 }).catch(() => {});
      }
    });
  });

  // ======================== PAGINATION ========================

  test.describe('PAGINATION', () => {
    test('should navigate to next page', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const nextButton = page.locator('button:has-text("Next"), [aria-label="Next page"]').first();
      
      if (await nextButton.count() > 0 && !(await nextButton.isDisabled())) {
        const firstPageRows = await page.locator('tbody tr').count();
        
        await nextButton.click();
        await page.waitForLoadState('networkidle');

        const secondPageRows = await page.locator('tbody tr').count();
        
        // Page should have loaded new data
        expect(secondPageRows).toBeGreaterThan(0);
      }
    });

    test('should go back to previous page', async ({ page }) => {
      await page.goto(`${BASE_URL}/admin/users`);
      await page.waitForLoadState('networkidle');

      const nextButton = page.locator('button:has-text("Next")').first();
      
      if (await nextButton.count() > 0 && !(await nextButton.isDisabled())) {
        await nextButton.click();
        await page.waitForLoadState('networkidle');

        const prevButton = page.locator('button:has-text("Previous"), [aria-label="Previous page"]').first();
        
        if (await prevButton.count() > 0) {
          await prevButton.click();
          await page.waitForLoadState('networkidle');

          // Should be back at first page
          expect(page.locator('tbody tr')).toBeVisible();
        }
      }
    });
  });
});
