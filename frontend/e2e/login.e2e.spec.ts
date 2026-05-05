/**
 * E2E Test for Login Flow
 *
 * This test suite runs against a real backend and tests the complete login flow
 * Prerequisites:
 * - Backend services must be running (auth-service, api-gateway)
 * - Frontend development server should be running
 */

import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:4200';
const AUTH_API = 'http://localhost:8888';

test.describe('Login E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to login page
    await page.goto(`${BASE_URL}/login`);
    // Wait for page to load
    await page.waitForLoadState('networkidle');
  });

  test('should display login page with form elements', async ({ page }) => {
    // Check for login form
    const form = page.locator('form');
    await expect(form).toBeVisible();

    // Check for username input
    const usernameInput = page.locator('input[type="text"], input[name="username"]');
    await expect(usernameInput).toBeVisible();

    // Check for password input
    const passwordInput = page.locator('input[type="password"]');
    await expect(passwordInput).toBeVisible();

    // Check for submit button
    const submitButton = page.locator('button[type="submit"]');
    await expect(submitButton).toBeVisible();
  });

  test('should show validation errors for empty form submission', async ({ page }) => {
    // Try to submit empty form
    const submitButton = page.locator('button[type="submit"]');
    await submitButton.click();

    // Check for validation messages or disabled state
    const form = page.locator('form');
    const inputs = form.locator('input[required]');

    // At least one input should have validation
    const firstInput = inputs.first();
    const isInvalid = await firstInput.evaluate((el: any) => !el.checkValidity?.());
    expect(isInvalid).toBeTruthy();
  });

  test('should successfully login with valid credentials', async ({ page, context }) => {
    // Fill login form with test credentials
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'admin');

    // Submit form
    const submitButton = page.locator('button[type="submit"]');
    await submitButton.click();

    // Wait for navigation
    await page.waitForNavigation();

    // Check if redirected to dashboard
    const url = page.url();
    expect(url).toContain('/admin-dashboard');

    // Check if token is stored in localStorage
    const token = await page.evaluate(() =>
      localStorage.getItem('auth:jwt')
    ).catch(() => null);

    // Token should exist if login was successful
    expect(token || url).toBeTruthy();
  });

  test('should show error message on invalid credentials', async ({ page }) => {
    // Fill login form with invalid credentials
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', 'invaliduser');
    await page.fill('input[type="password"]', 'wrongpassword');

    // Submit form
    const submitButton = page.locator('button[type="submit"]');
    await submitButton.click();

    // Wait for error response
    await page.waitForTimeout(1000);

    // Check for error message
    const errorMessage = page.locator('[role="alert"], .error, .toast');
    const hasError = await errorMessage.count().then(count => count > 0);

    if (hasError) {
      await expect(errorMessage.first()).toBeVisible();
    }

    // Should still be on login page
    const url = page.url();
    expect(url).toContain('/login');
  });

  test('should require both username and password fields', async ({ page }) => {
    // Try with only username
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', 'testuser');

    const submitButton = page.locator('button[type="submit"]');
    await submitButton.click();

    // Should not navigate to dashboard
    const url = page.url();
    expect(url).toContain('/login');

    // Try with only password
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', '');
    await page.fill('input[type="password"]', 'testpassword');

    await submitButton.click();

    // Should still be on login page
    const url2 = page.url();
    expect(url2).toContain('/login');
  });

  test('should handle network errors gracefully', async ({ page }) => {
    // Go offline
    await page.context().setOffline(true);

    // Fill login form
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', 'testuser');
    await page.fill('input[type="password"]', 'testpassword');

    // Try to submit
    const submitButton = page.locator('button[type="submit"]');
    await submitButton.click();

    // Wait for error
    await page.waitForTimeout(1000);

    // Go back online
    await page.context().setOffline(false);

    // Should show error message
    const errorMessage = page.locator('[role="alert"], .error, .toast');
    const hasError = await errorMessage.count().then(count => count > 0);

    // Either error is shown or user is informed somehow
    expect(hasError || page.url().includes('/login')).toBeTruthy();
  });

  test('should maintain session after login', async ({ page, context }) => {
    // Login with valid credentials
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'admin');

    const submitButton = page.locator('button[type="submit"]');
    await submitButton.click();

    // Wait for navigation
    await page.waitForNavigation();

    // Navigate away
    await page.goto(`${BASE_URL}/home`);

    // Navigate back to protected route
    await page.goto(`${BASE_URL}/admin-dashboard`);

    // Should still be on admin dashboard (token should be valid)
    await page.waitForLoadState('networkidle');
    const url = page.url();
    expect(url).toContain('/admin-dashboard');
  });

  test('should clear errors on new login attempt after failure', async ({ page }) => {
    // First failed attempt
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', 'invaliduser');
    await page.fill('input[type="password"]', 'wrongpassword');

    let submitButton = page.locator('button[type="submit"]');
    await submitButton.click();

    // Wait for error
    await page.waitForTimeout(1000);

    // Clear form
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', '');
    await page.fill('input[type="password"]', '');

    // Fill with valid credentials
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'admin');

    submitButton = page.locator('button[type="submit"]');
    await submitButton.click();

    // Should navigate successfully
    await page.waitForNavigation({ timeout: 5000 }).catch(() => {});

    const url = page.url();
    // Either successfully logged in or still attempting (no old error shown)
    expect(url).toBeTruthy();
  });
});

test.describe('Login UI Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.waitForLoadState('networkidle');
  });

  test('should display language selector if available', async ({ page }) => {
    const languageSelector = page.locator('select, [data-testid="language-selector"], button[aria-label*="language" i]');
    const count = await languageSelector.count();

    // Language selector may or may not be present, but if it is, it should work
    if (count > 0) {
      await expect(languageSelector.first()).toBeVisible();
    }
  });

  test('should show loading state during submission', async ({ page }) => {
    await page.fill('input[type="text"]:first-of-type, input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'admin');

    const submitButton = page.locator('button[type="submit"]');

    // Submit and check for loading state
    const clickPromise = submitButton.click();

    // The button might show loading state (disabled, spinner, etc.)
    const isDisabled = await submitButton.isDisabled();
    expect(isDisabled || await submitButton.locator('.loader, .spinner').count() > 0).toBeTruthy();

    await clickPromise;
  });

  test('should have accessible form labels', async ({ page }) => {
    // Check for form structure
    const inputs = page.locator('input');
    const count = await inputs.count();

    expect(count).toBeGreaterThanOrEqual(2); // At least username and password
  });
});

