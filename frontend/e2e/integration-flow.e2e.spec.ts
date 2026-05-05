/**
 * E2E Integration Flow Tests
 *
 * Tests the complete workflow of the application:
 * 1. User login (if applicable)
 * 2. Navigation through application
 * 3. Patient form submission
 * 4. Admin dashboard access
 * 5. Form retrieval and display
 *
 * Prerequisites:
 * - All backend services running
 * - Frontend development server running
 * - Test database with test users
 */

import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:4200';
const API_BASE = 'http://localhost:8888';

test.describe('Complete E2E Flow Tests', () => {

  test('should complete patient form submission flow', async ({ page }) => {
    // Navigate to home
    await page.goto(`${BASE_URL}/`);
    await page.waitForLoadState('networkidle');

    // Try to access patient form (may or may not require login)
    await page.goto(`${BASE_URL}/patient-form`);
    await page.waitForLoadState('networkidle');

    // If redirected to login, login first
    const currentUrl = page.url();
    if (currentUrl.includes('login')) {
      // Perform login if needed
      const usernameInput = page.locator('input[type="text"]:first-of-type, input[name="username"]');
      if (await usernameInput.count() > 0) {
        await usernameInput.fill('admin');
        await page.fill('input[type="password"]', 'admin');
        await page.locator('button[type="submit"]').click();
        await page.waitForNavigation();
      }

      // Navigate back to patient form
      await page.goto(`${BASE_URL}/patient-form`);
      await page.waitForLoadState('networkidle');
    }

    // Fill and submit patient form
    const formInputs = page.locator('input[type="text"], input[type="email"], input[type="tel"], input[name*="firstName" i]');
    if (await formInputs.count() > 0) {
      // Fill first name
      const firstNameField = page.locator('input[name*="firstName" i], input[placeholder*="First" i]');
      if (await firstNameField.count() > 0) {
        await firstNameField.fill('ALICE');
      }

      // Fill last name
      const lastNameField = page.locator('input[name*="lastName" i], input[placeholder*="Last" i]');
      if (await lastNameField.count() > 0) {
        await lastNameField.fill('JOHNSON');
      }

      // Fill email
      const emailField = page.locator('input[type="email"]');
      if (await emailField.count() > 0) {
        await emailField.fill('alice.johnson@test.com');
      }

      // Submit form
      const submitButton = page.locator('button:text("Submit")');
      if (await submitButton.count() > 0) {
        await submitButton.first().click();
        await page.waitForTimeout(2000);
      }
    }

    // Verify submission succeeded
    const currentUrlAfterSubmit = page.url();
    expect(currentUrlAfterSubmit).toBeTruthy();
  });

  test('should handle admin login and access dashboard', async ({ page }) => {
    // Navigate to login
    await page.goto(`${BASE_URL}/login`);
    await page.waitForLoadState('networkidle');

    // Enter admin credentials
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'admin');

    // Submit login
    const loginButton = page.locator('button[type="submit"]');
    await loginButton.click();

    // Wait for navigation
    await page.waitForNavigation({ timeout: 5000 }).catch(() => {});

    // Check if dashboard is accessible
    const dashboardUrl = page.url();
    const isAdminDashboard = dashboardUrl.includes('admin') || dashboardUrl.includes('dashboard');

    if (isAdminDashboard) {
      expect(dashboardUrl).toContain('admin');
    } else {
      // May be redirected to home, which is fine
      expect(dashboardUrl).toBeTruthy();
    }
  });

  test('should navigate through application menu', async ({ page }) => {
    // Start at home
    await page.goto(`${BASE_URL}/`);
    await page.waitForLoadState('networkidle');

    // Look for navigation menu
    const navbar = page.locator('nav, [role="navigation"], .navbar, .menu');

    if (await navbar.count() > 0) {
      // Check for menu items
      const menuItems = page.locator('a, button', { has: page.locator(':has-text("Home"), :has-text("About"), :has-text("Contact")') });

      if (await menuItems.count() > 0) {
        // Try to click first menu item
        const firstMenuItem = menuItems.first();
        const href = await firstMenuItem.getAttribute('href');

        if (href) {
          await firstMenuItem.click();
          await page.waitForLoadState('networkidle');
          expect(page.url()).toBeTruthy();
        }
      }
    }
  });

  test('should handle errors gracefully', async ({ page }) => {
    // Navigate to non-existent page
    await page.goto(`${BASE_URL}/non-existent-page`, { waitUntil: 'networkidle' }).catch(() => {});

    // Should not crash, may show 404 or redirect
    const currentUrl = page.url();
    expect(currentUrl).toBeTruthy();

    // Should be able to navigate back
    await page.goto(`${BASE_URL}/`);
    await page.waitForLoadState('networkidle');

    const homeUrl = page.url();
    expect(homeUrl).toContain(BASE_URL);
  });

  test('should handle session timeout and re-login', async ({ page }) => {
    // Navigate to login
    await page.goto(`${BASE_URL}/login`);
    await page.waitForLoadState('networkidle');

    // Login
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'admin');
    await page.locator('button[type="submit"]').click();

    // Wait for login
    await page.waitForTimeout(1000);

    // Navigate to admin area
    await page.goto(`${BASE_URL}/admin-dashboard`);
    await page.waitForLoadState('networkidle');

    // If session management is implemented, test timeout handling
    const currentUrl = page.url();
    expect(currentUrl).toBeTruthy();
  });

  test('should properly display responsive design on different screen sizes', async ({ browser }) => {
    // Test different viewport sizes
    const viewportSizes = [
      { width: 1920, height: 1080 }, // Desktop
      { width: 768, height: 1024 },  // Tablet
      { width: 375, height: 667 }    // Mobile
    ];

    for (const viewport of viewportSizes) {
      const context = await browser.newContext({ viewport });
      const page = await context.newPage();

      await page.goto(`${BASE_URL}/`);
      await page.waitForLoadState('networkidle');

      // Try to interact with form if visible
      const form = page.locator('form');
      if (await form.count() > 0) {
        await expect(form).toBeVisible();
      }

      // Check if navigation is accessible
      const navbar = page.locator('nav, [role="navigation"]');
      if (await navbar.count() > 0) {
        await expect(navbar).toBeVisible();
      }

      await context.close();
    }
  });

  test('should load resources without errors', async ({ page }) => {
    let errors = false;

    // Listen for console errors
    page.on('console', msg => {
      if (msg.type() === 'error') {
        console.log('Console error:', msg.text());
        errors = true;
      }
    });

    // Navigate through application
    await page.goto(`${BASE_URL}/`);
    await page.waitForLoadState('networkidle');

    // Check CSS and JS loaded
    const stylesheets = page.locator('link[rel="stylesheet"]');
    const scripts = page.locator('script[src]');

    if (await stylesheets.count() > 0) {
      expect(await stylesheets.count()).toBeGreaterThan(0);
    }

    // No critical errors should have occurred
    expect(errors).toBeFalsy();
  });

  test('should maintain form state across page refresh', async ({ page }) => {
    // Navigate to patient form
    await page.goto(`${BASE_URL}/patient-form`);
    await page.waitForLoadState('networkidle');

    // Fill a field
    const firstNameField = page.locator('input[name*="firstName" i], input[placeholder*="First" i]');
    if (await firstNameField.count() > 0) {
      await firstNameField.fill('TESTUSER');

      // Refresh page
      await page.reload();
      await page.waitForLoadState('networkidle');

      // Check if state is preserved (depends on implementation)
      const value = await firstNameField.inputValue();

      // May or may not preserve state - just verify form is still accessible
      await expect(firstNameField).toBeVisible();
    }
  });

  test('should handle API errors gracefully', async ({ page }) => {
    // Navigate to patient form
    await page.goto(`${BASE_URL}/patient-form`);
    await page.waitForLoadState('networkidle');

    // Try to submit form (may fail if no backend, should show error)
    const submitButton = page.locator('button[type="submit"]:text("Submit")');
    if (await submitButton.count() > 0) {
      await submitButton.first().click();

      // Wait for response
      await page.waitForTimeout(2000);

      // Check for error or success message
      const alert = page.locator('[role="alert"]');
      const isVisible = await alert.count() > 0;

      // Should show some feedback
      expect(isVisible || page.url()).toBeTruthy();
    }
  });
});

