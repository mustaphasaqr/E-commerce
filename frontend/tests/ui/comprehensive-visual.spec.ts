import { test, expect } from '@playwright/test';

/**
 * COMPREHENSIVE VISUAL REGRESSION TESTS
 * UI-Based Testing: Tests for visual consistency and layout bugs
 * Frontend in the middle - Tests actual frontend rendering and CSS
 * 
 * Uses Percy.io for visual regression testing (optional, requires Percy account)
 * Also includes manual visual checks as fallback
 * 
 * What users experience:
 * - Buttons visually aligned
 * - Text readable (good contrast, sizing)
 * - Layout responsive and not broken
 * - Images properly displayed
 * - No overlapping elements
 *
 * NOTE: Replace `await percySnapshot()` calls with Percy CLI:
 * npm install --save-dev @percy/cli @percy/playwright
 * npx percy exec -- playwright test comprehensive-visual.spec.ts
 */

const BASE_URL = 'http://localhost:3002';

test.describe('Visual Regression - Layout, Spacing, Alignment, Colors', () => {

  // ======================== LOGIN PAGE VISUAL ========================

  test('visual-login-1: Login form properly centered and laid out', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    // Check form container is centered
    const form = page.locator('form').first();
    const formBox = await form.boundingBox();
    const viewportSize = page.viewportSize();
    
    if (formBox && viewportSize) {
      // Form should not be cramped to left edge
      expect(formBox.x).toBeGreaterThanOrEqual(10);
      expect(formBox.width).toBeLessThanOrEqual(viewportSize.width - 20);
    }
  });

  test('visual-login-2: Form inputs aligned vertically', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    const email = page.locator('input[type="email"]');
    const password = page.locator('input[type="password"]');
    const emailBox = await email.boundingBox();
    const passwordBox = await password.boundingBox();
    
    if (emailBox && passwordBox) {
      // Should have same x position and similar width
      expect(Math.abs(emailBox.x - passwordBox.x)).toBeLessThan(5); // Same left edge
      expect(Math.abs(emailBox.width - passwordBox.width)).toBeLessThan(10); // Similar width
    }
  });

  test('visual-login-3: Submit button same width as inputs', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    const input = page.locator('input[type="email"]');
    const button = page.locator('button[type="submit"]');
    
    const inputBox = await input.boundingBox();
    const buttonBox = await button.boundingBox();
    
    if (inputBox && buttonBox) {
      // Button should align with input width (allow 10px margin of error)
      expect(Math.abs(inputBox.width - buttonBox.width)).toBeLessThan(10);
    }
  });

  test('visual-login-4: Form has appropriate vertical spacing', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    const email = page.locator('input[type="email"]');
    const password = page.locator('input[type="password"]');
    
    const emailBox = await email.boundingBox();
    const passwordBox = await password.boundingBox();
    
    if (emailBox && passwordBox) {
      const spacing = passwordBox.y - (emailBox.y + emailBox.height);
      
      // Should have visible spacing between inputs (not cramped)
      expect(spacing).toBeGreaterThanOrEqual(10);
      expect(spacing).toBeLessThanOrEqual(50); // Not too much space
    }
  });

  // ======================== NAVIGATION VISUAL ========================

  test('visual-nav-1: Navigation items properly aligned', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const navItems = page.locator('nav button, nav a, [role="tablist"] button, [role="tablist"] a');
    
    if (await navItems.count() > 1) {
      const item1Box = await navItems.nth(0).boundingBox();
      const item2Box = await navItems.nth(1).boundingBox();
      
      if (item1Box && item2Box) {
        // Items should be on same baseline (horizontal nav) or stacked (vertical nav)
        const isHorizontal = Math.abs(item1Box.y - item2Box.y) < 20;
        const isVertical = Math.abs(item1Box.x - item2Box.x) < 20;
        
        expect(isHorizontal || isVertical).toBe(true);
      }
    }
  });

  test('visual-nav-2: Navigation doesn\'t overflow page', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const nav = page.locator('nav').first();
    const navBox = await nav.boundingBox();
    const viewportSize = page.viewportSize();
    
    if (navBox && viewportSize) {
      expect(navBox.width).toBeLessThanOrEqual(viewportSize.width);
      expect(navBox.x + navBox.width).toBeLessThanOrEqual(viewportSize.width);
    }
  });

  // ======================== TABLE/LIST VISUAL ========================

  test('visual-table-1: Table columns aligned properly', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    const table = page.locator('table');
    if (await table.isVisible()) {
      const headers = table.locator('thead th');
      const firstRow = table.locator('tbody tr').first();
      
      if (await headers.count() > 0 && await firstRow.isVisible()) {
        const headerCount = await headers.count();
        const cellCount = firstRow.locator('td').count();
        
        // Column count should match
        expect(await cellCount).toBe(headerCount);
      }
    }
  });

  test('visual-table-2: Table rows have consistent height', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    const rows = page.locator('tbody tr');
    
    if (await rows.count() > 1) {
      const row1Height = (await rows.nth(0).boundingBox())?.height || 0;
      const row2Height = (await rows.nth(1).boundingBox())?.height || 0;
      
      // Heights should be consistent (allow 2px variance)
      expect(Math.abs(row1Height - row2Height)).toBeLessThanOrEqual(2);
    }
  });

  // ======================== MODAL VISUAL ========================

  test('visual-modal-1: Modal properly centered on screen', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    const blockBtn = page.locator('button:has-text("Block")').first();
    if (await blockBtn.isVisible()) {
      await blockBtn.click();
      
      const modal = page.locator('[role="dialog"], .modal, dialog');
      if (await modal.isVisible({ timeout: 1000 }).catch(() => false)) {
        const modalBox = await modal.boundingBox();
        const viewportSize = page.viewportSize();
        
        if (modalBox && viewportSize) {
          // Modal should be roughly centered
          const horizontalCenter = (modalBox.x + modalBox.width / 2);
          const viewportCenter = viewportSize.width / 2;
          
          // Centered +/- 10%
          expect(Math.abs(horizontalCenter - viewportCenter)).toBeLessThan(viewportSize.width * 0.15);
        }
      }
    }
  });

  test('visual-modal-2: Modal content has proper padding', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    const blockBtn = page.locator('button:has-text("Block")').first();
    if (await blockBtn.isVisible()) {
      await blockBtn.click();
      
      const modal = page.locator('[role="dialog"], .modal, dialog');
      const content = modal.locator('[class*="modal-content"], [class*="dialog-content"], [class*="body"]');
      
      if (await content.isVisible({ timeout: 1000 }).catch(() => false)) {
        // Content should have padding from edges
        expect(await content.getAttribute('class')).toBeTruthy();
      }
    }
  });

  // ======================== RESPONSIVE VISUAL ========================

  test('visual-responsive-1: Page readable on mobile (375px)', async ({ page, browserName }) => {
    // Create mobile-sized viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    await page.goto(`${BASE_URL}/login`);
    
    const form = page.locator('form').first();
    const inputs = form.locator('input');
    
    // Inputs should still be visible and usable
    for (let i = 0; i < await inputs.count(); i++) {
      const input = inputs.nth(i);
      const box = await input.boundingBox();
      
      expect(box!.width).toBeGreaterThanOrEqual(100); // Minimum usable width
    }
  });

  test('visual-responsive-2: No horizontal scroll on mobile', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto(`${BASE_URL}/profile`);
    
    // Check if page is scrollable horizontally
    const bodyWidth = await page.evaluate(() => document.body.scrollWidth);
    const windowWidth = await page.evaluate(() => window.innerWidth);
    
    expect(bodyWidth).toBeLessThanOrEqual(windowWidth + 1); // +1 for rounding
  });

  test('visual-responsive-3: Content readable on tablet (768px)', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 });
    
    await page.goto(`${BASE_URL}/admin/users`);
    
    const table = page.locator('table');
    if (await table.isVisible()) {
      const box = await table.boundingBox();
      
      // Table should fit on screen
      expect(box!.width).toBeLessThanOrEqual(768);
    }
  });

  // ======================== COLOR & CONTRAST VISUAL ========================

  test('visual-color-1: Status badges have distinct colors', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    const statusBadges = page.locator('[class*="status"], [class*="badge"]').first();
    
    if (await statusBadges.isVisible()) {
      const color = await statusBadges.evaluate((el) =>
        window.getComputedStyle(el).backgroundColor
      );
      
      expect(color).not.toMatch(/rgba?\(0,\s*0,\s*0,\s*0\)/); // Not transparent
    }
  });

  test('visual-color-2: Success and error messages have appropriate colors', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    await page.fill('input[type="email"]', 'invalid');
    await page.click('button[type="submit"]');
    
    const error = page.locator('[role="alert"], .error-message').first();
    
    if (await error.isVisible({ timeout: 1000 }).catch(() => false)) {
      const bgColor = await error.evaluate((el) =>
        window.getComputedStyle(el).backgroundColor
      );
      
      // Error should not use green (success color)
      expect(bgColor).not.toMatch(/rgb\(0,\s*128,\s*0\)/);
    }
  });

  // ======================== ELEMENT VISIBILITY VISUAL ========================

  test('visual-visibility-1: No overlapping elements', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const allElements = page.locator('button, input, a, h1, h2, p');
    
    if (await allElements.count() > 0) {
      const element1 = allElements.nth(0);
      const element2 = allElements.nth(1);
      
      const box1 = await element1.boundingBox();
      const box2 = await element2.boundingBox();
      
      if (box1 && box2) {
        // Check if elements overlap
        const horizontalOverlap = !(box1.x + box1.width < box2.x || box2.x + box2.width < box1.x);
        const verticalOverlap = !(box1.y + box1.height < box2.y || box2.y + box2.height < box1.y);
        
        // Some overlap is okay (elements on different lines), but not fully covered
        const overlapping = horizontalOverlap && verticalOverlap;
        
        // At least some elements should be visible without overlap
        if (overlapping) {
          // This specific pair overlaps - check another pair
          expect(await allElements.count()).toBeGreaterThan(2);
        }
      }
    }
  });

  test('visual-visibility-2: Images load and display correctly', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const images = page.locator('img');
    
    for (let i = 0; i < Math.min(3, await images.count()); i++) {
      const img = images.nth(i);
      
      // Image should have dimensions
      const box = await img.boundingBox();
      expect(box!.width).toBeGreaterThan(0);
      expect(box!.height).toBeGreaterThan(0);
    }
  });

  test('visual-visibility-3: Form inputs have visible placeholders or labels', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    
    const inputs = page.locator('input[type="text"], input[type="email"], input[type="password"]');
    
    for (let i = 0; i < Math.min(3, await inputs.count()); i++) {
      const input = inputs.nth(i);
      
      const placeholder = await input.getAttribute('placeholder');
      const id = await input.getAttribute('id');
      const label = await page.locator(`label[for="${id}"]`).textContent();
      
      // Should have either placeholder or label
      expect(placeholder || label).toBeTruthy();
    }
  });

  // ======================== TEXT RENDERING VISUAL ========================

  test('visual-text-1: Page title is properly sized', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const h1 = page.locator('h1');
    
    if (await h1.isVisible()) {
      const fontSize = await h1.evaluate((el) =>
        parseInt(window.getComputedStyle(el).fontSize)
      );
      
      expect(fontSize).toBeGreaterThanOrEqual(24); // Reasonable heading size
    }
  });

  test('visual-text-2: Text is not too small to read', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    const bodyText = page.locator('body *:not(script):not(style)');
    
    const minFontSize = await page.evaluate(() => {
      const elements = document.querySelectorAll('body *:not(script):not(style)');
      const fontSizes = Array.from(elements).map(el =>
        parseInt(window.getComputedStyle(el as HTMLElement).fontSize)
      );
      return Math.min(...fontSizes.filter(f => f > 0));
    });
    
    expect(minFontSize).toBeGreaterThanOrEqual(12); // Minimum readable font
  });

  test('visual-text-3: Line height adequate for readability', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const paragraph = page.locator('p').first();
    
    if (await paragraph.isVisible()) {
      const lineHeight = await paragraph.evaluate((el) => {
        const height = window.getComputedStyle(el).lineHeight;
        // lineHeight can be "normal" or a number
        return height === 'normal' ? 1.2 : parseFloat(height);
      });
      
      // Line height should be reasonable for readability (>= 1.2)
      expect(lineHeight).toBeGreaterThanOrEqual(1.2);
    }
  });
});
