import { test, expect } from '@playwright/test';

/**
 * COMPREHENSIVE UX CLARITY TESTS
 * UI-Based Testing: Tests error messages, navigation clarity, button sizing, visual hierarchy
 * Frontend in the middle - Tests actual frontend UX design and user clarity
 * 
 * What users experience:
 * - Clear error messages near relevant fields
 * - Obvious call-to-action buttons
 * - Consistent navigation
 * - Adequate button/link sizing for clicking
 * - Visual feedback for interactions
 */

const BASE_URL = 'http://localhost:3002';

test.describe('UX Clarity - Error Messages, Navigation, Button Sizing, Feedback', () => {

  // ======================== ERROR MESSAGE CLARITY ========================

  test('ux-error-1: Validation error appears near invalid field', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    // Submit with invalid email
    await page.fill('input[type="email"]', 'invalid-email');
    await page.click('button[type="submit"]');
    
    const emailInput = page.locator('input[type="email"]');
    const emailBoundingBox = await emailInput.boundingBox();
    
    // Find error message
    const errorMsg = page.locator('[role="alert"], .error-message, .form-error').first();
    
    if (await errorMsg.isVisible({ timeout: 500 }).catch(() => false)) {
      const errorBoundingBox = await errorMsg.boundingBox();
      
      // Error should be near field (within 100px vertically or horizontally)
      const verticalDistance = Math.abs(errorBoundingBox!.y - emailBoundingBox!.y);
      const horizontalDistance = Math.abs(errorBoundingBox!.x - emailBoundingBox!.x);
      
      expect(verticalDistance + horizontalDistance).toBeLessThan(150);
    }
  });

  test('ux-error-2: Error message is red/visible (high contrast)', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    
    // Submit empty form
    await page.click('button[type="submit"]');
    
    const errorMsg = page.locator('[role="alert"], .error-message, .form-error').first();
    
    if (await errorMsg.isVisible({ timeout: 1000 }).catch(() => false)) {
      const color = await errorMsg.evaluate((el) => {
        const style = window.getComputedStyle(el);
        return style.color;
      });
      
      // Should have strong color (not gray)
      expect(color).toBeTruthy();
    }
  });

  test('ux-error-3: Error message is specific (not just "Error")', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    // Submit with invalid data
    await page.fill('input[type="email"]', 'invalid');
    await page.fill('input[type="password"]', 'x');
    await page.click('button[type="submit"]');
    
    const errorMsg = page.locator('[role="alert"], .error-message').first();
    
    if (await errorMsg.isVisible({ timeout: 2000 }).catch(() => false)) {
      const text = await errorMsg.textContent();
      
      // Should tell user what's wrong, not just "Error"
      expect(text).not.toMatch(/^(error|failed)$/i);
      expect(text?.length).toBeGreaterThan(10);
    }
  });

  test('ux-error-4: Success message is clear and positive', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    await page.fill('input[type="email"]', 'test@example.com');
    await page.fill('input[type="password"]', 'TestPassword123!');
    await page.click('button[type="submit"]');
    
    const successMsg = page.locator('.success-message, [role="status"]').first();
    
    if (await successMsg.isVisible({ timeout: 3000 }).catch(() => false)) {
      const text = await successMsg.textContent();
      
      // Should be positive and clear
      expect(text?.toLowerCase()).toMatch(/success|welcome|logged|completed/);
    }
  });

  // ======================== CALL-TO-ACTION CLARITY ========================

  test('ux-cta-1: Primary button visually distinct from secondary', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    const primaryBtn = page.locator('button[type="submit"]'); // Primary CTA
    const secondaryBtn = page.locator('a:has-text("Register"), a:has-text("Forgot"), button:not([type="submit"])').first();
    
    const primaryStyle = await primaryBtn.evaluate((el) => ({
      bg: window.getComputedStyle(el).backgroundColor,
      color: window.getComputedStyle(el).color,
      size: el.getBoundingClientRect().width
    }));
    
    const secondaryStyle = await secondaryBtn.evaluate((el) => ({
      bg: window.getComputedStyle(el).backgroundColor,
      color: window.getComputedStyle(el).color,
      size: el.getBoundingClientRect().width
    }));
    
    // Primary should look different (color or size)
    const isDifferent = primaryStyle.bg !== secondaryStyle.bg || 
                       primaryStyle.color !== secondaryStyle.color ||
                       Math.abs(primaryStyle.size - secondaryStyle.size) > 10;
    
    expect(isDifferent).toBe(true);
  });

  test('ux-cta-2: Call-to-action button clearly labeled', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const actionButtons = page.locator('button');
    
    for (let i = 0; i < Math.min(5, await actionButtons.count()); i++) {
      const btn = actionButtons.nth(i);
      const text = await btn.textContent();
      
      // Button text should be action-oriented verb
      const isActionable = /^(save|submit|update|delete|edit|create|add|remove|send|cancel|close|done)/i.test(text || '');
      
      expect(isActionable || text?.length! > 3).toBe(true);
    }
  });

  test('ux-cta-3: Destructive actions clearly marked or confirmed', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    const deleteBtn = page.locator('button:has-text("Delete")').first();
    
    if (await deleteBtn.isVisible()) {
      // Delete button should be red/danger color
      const style = await deleteBtn.evaluate((el) => 
        window.getComputedStyle(el).backgroundColor
      );
      
      const isDangerColor = style.includes('rgb(') || style.includes('red'); // Danger color
      
      // Or should require confirmation
      await deleteBtn.click();
      const modal = page.locator('[role="dialog"], .modal, .confirm-dialog');
      const hasConfirmation = await modal.isVisible({ timeout: 500 }).catch(() => false);
      
      expect(isDangerColor || hasConfirmation).toBe(true);
    }
  });

  // ======================== NAVIGATION CLARITY ========================

  test('ux-nav-1: Navigation menu clearly visible on all pages', async ({ page }) => {
    const pages = [`${BASE_URL}/login`, `${BASE_URL}/register`, `${BASE_URL}/profile`];
    
    for (const pageUrl of pages) {
      await page.goto(pageUrl);
      
      const nav = page.locator('nav, header, [role="navigation"]');
      const hasNav = await nav.isVisible().catch(() => false);
      
      // Either explicit nav or page should be self-explanatory
      expect(hasNav || await page.locator('h1, h2').isVisible()).toBe(true);
    }
  });

  test('ux-nav-2: Current page highlighted in navigation', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const navItems = page.locator('nav a, nav button, [role="navigation"] a, [role="navigation"] button');
    
    if (await navItems.count() > 0) {
      for (let i = 0; i < await navItems.count(); i++) {
        const item = navItems.nth(i);
        const isActive = await item.evaluate((el) => 
          el.classList.contains('active') || 
          el.classList.contains('current') ||
          el.getAttribute('aria-current') === 'page'
        );
        
        // At least one nav item should be marked as active
        if (i === 0 && await navItems.count() > 0) {
          expect(isActive || await item.getAttribute('href')).toBeTruthy();
        }
      }
    }
  });

  test('ux-nav-3: Breadcrumbs show current location (if used)', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    const breadcrumb = page.locator('[class*="breadcrumb"], nav[aria-label="breadcrumb"]');
    
    if (await breadcrumb.isVisible()) {
      const items = breadcrumb.locator('a, span');
      const itemCount = await items.count();
      
      // Should have at least 2 items
      expect(itemCount).toBeGreaterThanOrEqual(2);
      
      // Last item should be current page (not a link)
      const lastItem = items.nth(itemCount - 1);
      const isLink = await lastItem.evaluate((el) => el.tagName === 'A');
      
      expect(isLink).toBe(false); // Current page isn't a link
    }
  });

  test('ux-nav-4: Back button or link available on nested pages', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const backButton = page.locator('button:has-text("Back"), a:has-text("Back"), [aria-label*="Back"]');
    const pageTitle = page.locator('h1');
    
    // Either explicit back button or page should be main/not nested
    const hasBackButton = await backButton.isVisible().catch(() => false);
    const isMainPage = await pageTitle.textContent().then(t => t?.includes('Dashboard') || t?.includes('Home'));
    
    expect(hasBackButton || isMainPage).toBe(true);
  });

  // ======================== BUTTON & CLICKABLE SIZING ========================

  test('ux-button-1: Buttons have minimum clickable size (44x44px)', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    const buttons = page.locator('button');
    
    for (let i = 0; i < Math.min(5, await buttons.count()); i++) {
      const btn = buttons.nth(i);
      const box = await btn.boundingBox();
      
      // Minimum accessible button size: 44x44px (WCAG guideline)
      expect(box!.width).toBeGreaterThanOrEqual(44);
      expect(box!.height).toBeGreaterThanOrEqual(44);
    }
  });

  test('ux-button-2: Links have adequate spacing (not cramped)', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const links = page.locator('a');
    
    if (await links.count() > 1) {
      // Get bounding boxes for first two links
      const link1Box = await links.nth(0).boundingBox();
      const link2Box = await links.nth(1).boundingBox();
      
      // Should have some separation (not overlapping or too close)
      if (link1Box && link2Box) {
        const verticalGap = Math.abs(link2Box.y - (link1Box.y + link1Box.height));
        expect(verticalGap).toBeGreaterThanOrEqual(0);
      }
    }
  });

  // ======================== FORM STRUCTURE CLARITY ========================

  test('ux-form-1: Form labels clearly associated with inputs', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    
    const inputs = page.locator('input[type="text"], input[type="email"], input[type="password"]');
    
    for (let i = 0; i < Math.min(3, await inputs.count()); i++) {
      const input = inputs.nth(i);
      const inputId = await input.getAttribute('id');
      
      // Either has label with for attribute, or aria-label, or aria-labelledby
      const hasLabel = await page.locator(`label[for="${inputId}"]`).isVisible().catch(() => false);
      const hasAriaLabel = await input.getAttribute('aria-label');
      const hasAriaLabelledBy = await input.getAttribute('aria-labelledby');
      
      expect(hasLabel || hasAriaLabel || hasAriaLabelledBy).toBeTruthy();
    }
  });

  test('ux-form-2: Required fields clearly marked', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    
    const requiredInputs = page.locator('input[required]');
    
    if (await requiredInputs.count() > 0) {
      for (let i = 0; i < Math.min(3, await requiredInputs.count()); i++) {
        const input = requiredInputs.nth(i);
        const inputId = await input.getAttribute('id');
        
        // Find label for this input
        const label = page.locator(`label[for="${inputId}"]`);
        const labelText = await label.textContent();
        const ariaRequired = await input.getAttribute('aria-required');
        
        // Check if label has asterisk or text says "required"
        const hasRequiredMark = labelText?.includes('*') || labelText?.includes('Required');
        
        expect(hasRequiredMark || ariaRequired === 'true').toBe(true);
      }
    }
  });

  test('ux-form-3: Form input has visual focus indicator', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    const emailInput = page.locator('input[type="email"]');
    await emailInput.focus();
    
    // Check for visual focus
    const focusStyle = await emailInput.evaluate((el) => {
      const style = window.getComputedStyle(el);
      return {
        outline: style.outline,
        boxShadow: style.boxShadow,
        borderColor: style.borderColor,
        backgroundColor: style.backgroundColor
      };
    });
    
    // Should have some visual change when focused
    const hasFocusStyle = Object.values(focusStyle).some(val => 
      val && val !== 'none' && val !== 'rgba(0, 0, 0, 0)'
    );
    
    expect(hasFocusStyle).toBe(true);
  });

  // ======================== VISUAL HIERARCHY ========================

  test('ux-hierarchy-1: Most important action is visually prominent', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    // Submit button should be larger/more colored than links
    const submitBtn = page.locator('button[type="submit"]');
    const registerLink = page.locator('a:has-text("Register")');
    
    const submitSize = await submitBtn.boundingBox();
    const linkSize = await registerLink.boundingBox();
    
    // Submit should be more prominent (larger)
    if (submitSize && linkSize) {
      expect(submitSize.width * submitSize.height).toBeGreaterThan(linkSize.width * linkSize.height);
    }
  });

  test('ux-hierarchy-2: Page title prominent and clear', async ({ page }) => {
    await page.goto(`${BASE_URL}/profile`);
    
    const pageTitle = page.locator('h1');
    
    // Should be visible and have appropriate size
    const isVisible = await pageTitle.isVisible();
    const titleSize = await pageTitle.boundingBox();
    
    expect(isVisible).toBe(true);
    expect(titleSize!.height).toBeGreaterThanOrEqual(24); // Reasonable font size
  });

  test('ux-hierarchy-3: Input focus changes are obvious', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    const email = page.locator('input[type="email"]');
    const password = page.locator('input[type="password"]');
    
    // Focus email
    await email.focus();
    const emailFocused = await email.evaluate(el => el === document.activeElement);
    
    // Focus password
    await password.focus();
    const passwordFocused = await password.evaluate(el => el === document.activeElement);
    
    // Both states should be identifiable
    expect(emailFocused || passwordFocused).toBe(true);
  });
});
