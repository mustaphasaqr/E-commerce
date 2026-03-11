/// <reference types="node" />
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests/integration',  // Only integration tests
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  timeout: 30 * 1000,  // 30 seconds (most tests 3-5 sec)
  expect: { timeout: 5000 },  // 5 seconds for expect() calls
  retries: process.env.CI ? 1 : 0,  // Only 1 retry (fast feedback)
  workers: process.env.CI ? 10 : undefined,  // 10 workers (fast, stable)
  reporter: [
    ['list'],
    ['html', { open: 'never' }],
  ],
  use: {
    baseURL: 'http://localhost:3002',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    // Removed Mobile Chrome - same backend, same logic, wastes time
  ],

  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3002',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },
});
