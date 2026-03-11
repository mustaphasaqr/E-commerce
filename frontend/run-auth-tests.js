#!/usr/bin/env node

/**
 * 🔐 E-Commerce Authentication Test Suite
 * Professional Playwright testing for Login & Sign Up
 * 
 * This script runs comprehensive tests for:
 * - Login page functionality
 * - Sign up page functionality
 * - Form validation
 * - Cross-browser compatibility ✓
 * - Responsive design (Desktop, Mobile, Tablet) ✓
 * 
 * Test Coverage:
 * ✓ Chrome / Chromium (Desktop)
 * ✓ Mobile Chrome (Preview)
 * ✓ iPad / Tablet Preview
 * ✓ All authentication flows
 * ✓ All form validations
 * 
 * Usage:
 *   npm run test:auth           # Run all authentication tests
 *   npm run test:auth:ui        # Run with visual UI
 *   npm run test:auth:debug     # Debug individual tests
 *   npm run test:auth:report    # Generate and open HTML report
 */

const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

const projectRoot = path.resolve(__dirname, '../..');
const args = process.argv.slice(2);

const commands = {
  default: {
    name: 'Run all authentication tests',
    cmd: 'npx playwright test auth-professional.spec.ts --reporter=html',
  },
  ui: {
    name: 'Run tests in UI mode (interactive)',
    cmd: 'npx playwright test auth-professional.spec.ts --ui',
  },
  debug: {
    name: 'Run tests in debug mode',
    cmd: 'npx playwright test auth-professional.spec.ts --debug',
  },
  report: {
    name: 'Generate and open HTML report',
    cmd: 'npx playwright test auth-professional.spec.ts --reporter=html && npx playwright show-report',
  },
  quick: {
    name: 'Quick test run (Chrome only)',
    cmd: 'npx playwright test auth-professional.spec.ts --project=chromium',
  },
  mobile: {
    name: 'Test on mobile and tablet',
    cmd: 'npx playwright test auth-professional.spec.ts --project=chromium --project="Mobile Chrome" --project=iPad',
  },
};

function runCommand(cmd) {
  try {
    console.log('\n' + '='.repeat(70));
    console.log('🔐 Running Authentication Test Suite');
    console.log('='.repeat(70) + '\n');
    
    execSync(cmd, {
      cwd: projectRoot,
      stdio: 'inherit',
      shell: true,
    });
    
    console.log('\n' + '='.repeat(70));
    console.log('✅ Tests completed successfully!');
    console.log('='.repeat(70) + '\n');
  } catch (error) {
    console.error('\n❌ Test execution failed');
    process.exit(1);
  }
}

function showHelp() {
  console.log('\n🔐 E-Commerce Authentication Test Suite\n');
  console.log('Available commands:\n');
  
  Object.entries(commands).forEach(([key, { name, cmd }]) => {
    console.log(`  npm run test:auth:${key === 'default' ? '' : key}`);
    console.log(`    → ${name}\n`);
  });
}

// Parse arguments
const command = args[0] || 'default';

if (command === '--help' || command === '-h') {
  showHelp();
} else if (commands[command]) {
  runCommand(commands[command].cmd);
} else {
  console.error(`❌ Unknown command: ${command}\n`);
  showHelp();
  process.exit(1);
}
