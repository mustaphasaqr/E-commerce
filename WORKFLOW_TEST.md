# Manual Workflow Trigger Test

This file was created to test the manual workflow_dispatch trigger.

**Date**: March 7, 2026
**Branch**: feat/test-manual-workflow-trigger
**Purpose**: Verify that the "Run workflow" button works correctly

When you click "Run workflow" on the GitHub Actions page and select this branch, the CI/CD pipeline should run automatically with all 4 stages:
1. Build & Test
2. Security & Quality Checks
3. Manual Approval
4. Deploy to Production
