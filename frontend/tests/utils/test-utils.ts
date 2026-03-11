/**
 * Test Utilities - Common operations and helpers
 * Used across all test suites for frontend testing
 */

import { Page, expect } from '@playwright/test';

const API_BASE_URL = 'http://localhost:8080/api/v1';
const FRONTEND_URL = 'http://localhost:3000';

/**
 * Auth Helper - Login and get tokens
 */
export async function loginUser(
  page: Page,
  email: string = 'testuser@example.com',
  password: string = 'TestPassword123!'
) {
  const response = await page.evaluate(async ({ email, password }) => {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    return {
      status: res.status,
      data: await res.json()
    };
  }, { email, password });

  if (response.status !== 200) {
    throw new Error(`Login failed with status ${response.status}`);
  }

  return {
    accessToken: response.data.data.accessToken,
    refreshToken: response.data.data.refreshToken,
    user: response.data.data.user
  };
}

/**
 * Admin Login Helper
 */
export async function loginAdmin(
  page: Page,
  email: string = 'admin@example.com',
  password: string = 'AdminPassword123!'
) {
  return loginUser(page, email, password);
}

/**
 * Register new user
 */
export async function registerUser(
  page: Page,
  userData?: {
    email?: string;
    username?: string;
    password?: string;
    firstName?: string;
    lastName?: string;
  }
) {
  const email = userData?.email || `user-${Date.now()}@example.com`;
  const username = userData?.username || `user${Date.now()}`;
  const password = userData?.password || 'TestPassword123!';
  const firstName = userData?.firstName || 'Test';
  const lastName = userData?.lastName || 'User';

  const response = await page.evaluate(
    async ({ email, username, password, firstName, lastName }) => {
      const res = await fetch(`${API_BASE_URL}/users/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email,
          username,
          password,
          firstName,
          lastName
        })
      });
      return {
        status: res.status,
        data: await res.json()
      };
    },
    { email, username, password, firstName, lastName }
  );

  if (response.status !== 201) {
    throw new Error(`Registration failed with status ${response.status}`);
  }

  return {
    user: response.data.data,
    email,
    password
  };
}

/**
 * Make authenticated API request
 */
export async function makeAuthenticatedRequest(
  page: Page,
  method: string,
  endpoint: string,
  token: string,
  body?: any
) {
  const response = await page.evaluate(
    async ({ method, endpoint, token, body }) => {
      const res = await fetch(`${API_BASE_URL}${endpoint}`, {
        method,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: body ? JSON.stringify(body) : undefined
      });

      let data;
      try {
        data = await res.json();
      } catch {
        data = null;
      }

      return {
        status: res.status,
        data,
        headers: Array.from(res.headers.entries()).reduce(
          (acc, [key, value]) => ({ ...acc, [key]: value }),
          {}
        )
      };
    },
    { method, endpoint, token, body }
  );

  return response;
}

/**
 * Make unauthenticated API request
 */
export async function makePublicRequest(
  page: Page,
  method: string,
  endpoint: string,
  body?: any
) {
  const response = await page.evaluate(
    async ({ method, endpoint, body }) => {
      const res = await fetch(`${API_BASE_URL}${endpoint}`, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: body ? JSON.stringify(body) : undefined
      });

      let data;
      try {
        data = await res.json();
      } catch {
        data = null;
      }

      return {
        status: res.status,
        data
      };
    },
    { method, endpoint, body }
  );

  return response;
}

/**
 * Get current user profile
 */
export async function getCurrentUser(page: Page, token: string) {
  const response = await makeAuthenticatedRequest(page, 'GET', '/users/me', token);
  
  if (response.status !== 200) {
    throw new Error(`Failed to get current user: ${response.status}`);
  }

  return response.data.data;
}

/**
 * Change user email
 */
export async function changeUserEmail(
  page: Page,
  token: string,
  newEmail: string,
  password: string
) {
  const response = await makeAuthenticatedRequest(
    page,
    'PUT',
    '/users/me/email',
    token,
    { newEmail, password }
  );

  return response;
}

/**
 * Change user password
 */
export async function changeUserPassword(
  page: Page,
  token: string,
  currentPassword: string,
  newPassword: string
) {
  const response = await makeAuthenticatedRequest(
    page,
    'PUT',
    '/users/me/password',
    token,
    { currentPassword, newPassword }
  );

  return response;
}

/**
 * Grant marketing consent
 */
export async function grantMarketingConsent(page: Page, token: string) {
  const response = await makeAuthenticatedRequest(
    page,
    'POST',
    '/users/me/marketing/grant',
    token
  );

  return response;
}

/**
 * Revoke marketing consent
 */
export async function revokeMarketingConsent(page: Page, token: string) {
  const response = await makeAuthenticatedRequest(
    page,
    'DELETE',
    '/users/me/marketing',
    token
  );

  return response;
}

/**
 * List users with pagination
 */
export async function listUsers(
  page: Page,
  token: string,
  page_num: number = 0,
  size: number = 20
) {
  const response = await makeAuthenticatedRequest(
    page,
    'GET',
    `/admin/users?page=${page_num}&size=${size}`,
    token
  );

  return response;
}

/**
 * Search users
 */
export async function searchUsers(
  page: Page,
  token: string,
  filters: {
    email?: string;
    username?: string;
    status?: string;
    role?: string;
  } = {},
  page_num: number = 0,
  size: number = 20
) {
  const response = await makeAuthenticatedRequest(
    page,
    'POST',
    '/admin/users/search',
    token,
    { ...filters, page: page_num, size }
  );

  return response;
}

/**
 * Block user
 */
export async function blockUser(
  page: Page,
  token: string,
  userId: string,
  reason: string = 'Test block'
) {
  const response = await makeAuthenticatedRequest(
    page,
    'POST',
    `/admin/users/${userId}/block`,
    token,
    { reason }
  );

  return response;
}

/**
 * Unblock user
 */
export async function unblockUser(
  page: Page,
  token: string,
  userId: string,
  reason: string = 'Test unblock'
) {
  const response = await makeAuthenticatedRequest(
    page,
    'POST',
    `/admin/users/${userId}/unblock`,
    token,
    { reason }
  );

  return response;
}

/**
 * Activate user
 */
export async function activateUser(
  page: Page,
  token: string,
  userId: string,
  note: string = 'Test activation'
) {
  const response = await makeAuthenticatedRequest(
    page,
    'POST',
    `/users/${userId}/activate`,
    token,
    { activationNote: note }
  );

  return response;
}

/**
 * Deactivate user
 */
export async function deactivateUser(
  page: Page,
  token: string,
  userId: string,
  reason: string = 'Test deactivation'
) {
  const response = await makeAuthenticatedRequest(
    page,
    'POST',
    `/users/${userId}/deactivate`,
    token,
    { reason }
  );

  return response;
}

/**
 * Delete user
 */
export async function deleteUser(
  page: Page,
  token: string,
  userId: string,
  reason: string = 'Test deletion'
) {
  const response = await makeAuthenticatedRequest(
    page,
    'DELETE',
    `/admin/users/${userId}`,
    token,
    { reason }
  );

  return response;
}

/**
 * Change user role
 */
export async function changeUserRole(
  page: Page,
  token: string,
  userId: string,
  newRole: string
) {
  const response = await makeAuthenticatedRequest(
    page,
    'POST',
    `/admin/users/${userId}/role`,
    token,
    { newRole }
  );

  return response;
}

/**
 * Logout user
 */
export async function logout(page: Page, token: string) {
  const response = await makeAuthenticatedRequest(
    page,
    'POST',
    '/auth/logout',
    token
  );

  return response;
}

/**
 * Logout from all devices
 */
export async function logoutAll(page: Page, token: string) {
  const response = await makeAuthenticatedRequest(
    page,
    'POST',
    '/auth/logout-all',
    token
  );

  return response;
}

/**
 * Refresh token
 */
export async function refreshToken(page: Page, refreshToken: string) {
  const response = await makePublicRequest(
    page,
    'POST',
    '/auth/refresh',
    { refreshToken }
  );

  return response;
}

/**
 * Request password reset
 */
export async function requestPasswordReset(page: Page, email: string) {
  const response = await makePublicRequest(
    page,
    'POST',
    '/auth/password-reset/request',
    { email }
  );

  return response;
}

/**
 * Complete password reset
 */
export async function completePasswordReset(
  page: Page,
  resetToken: string,
  newPassword: string
) {
  const response = await makePublicRequest(
    page,
    'POST',
    '/auth/password-reset/complete',
    { resetToken, newPassword }
  );

  return response;
}

/**
 * Get user by ID
 */
export async function getUserById(page: Page, token: string, userId: string) {
  const response = await makeAuthenticatedRequest(
    page,
    'GET',
    `/users/${userId}`,
    token
  );

  return response;
}

/**
 * Get user by email
 */
export async function getUserByEmail(page: Page, token: string, email: string) {
  const response = await makeAuthenticatedRequest(
    page,
    'GET',
    `/users/email/${encodeURIComponent(email)}`,
    token
  );

  return response;
}

/**
 * Get user by username
 */
export async function getUserByUsername(page: Page, token: string, username: string) {
  const response = await makeAuthenticatedRequest(
    page,
    'GET',
    `/users/username/${username}`,
    token
  );

  return response;
}

/**
 * Verify email
 */
export async function verifyEmail(page: Page, token: string) {
  const response = await makeAuthenticatedRequest(
    page,
    'POST',
    '/users/me/email/verify',
    token
  );

  return response;
}

/**
 * Measure API endpoint response time
 */
export async function measureEndpointPerformance(
  page: Page,
  method: string,
  endpoint: string,
  token?: string,
  body?: any
): Promise<number> {
  const startTime = Date.now();
  
  if (token) {
    await makeAuthenticatedRequest(page, method, endpoint, token, body);
  } else {
    await makePublicRequest(page, method, endpoint, body);
  }
  
  return Date.now() - startTime;
}

/**
 * Measure multiple endpoint calls
 */
export async function measureMultipleEndpoints(
  page: Page,
  endpoints: Array<{
    method: string;
    endpoint: string;
    token?: string;
    body?: any;
  }>,
  iterations: number = 1
): Promise<Map<string, number[]>> {
  const results = new Map<string, number[]>();

  for (let i = 0; i < iterations; i++) {
    for (const endpoint of endpoints) {
      const time = await measureEndpointPerformance(
        page,
        endpoint.method,
        endpoint.endpoint,
        endpoint.token,
        endpoint.body
      );

      const key = `${endpoint.method} ${endpoint.endpoint}`;
      if (!results.has(key)) {
        results.set(key, []);
      }
      results.get(key)!.push(time);
    }
  }

  return results;
}

/**
 * Calculate performance statistics
 */
export function calculateStats(times: number[]) {
  if (times.length === 0) return { avg: 0, min: 0, max: 0, median: 0 };

  const sorted = [...times].sort((a, b) => a - b);
  const avg = times.reduce((a, b) => a + b, 0) / times.length;
  const median = sorted[Math.floor(sorted.length / 2)];

  return {
    avg: Math.round(avg),
    min: Math.min(...times),
    max: Math.max(...times),
    median,
    count: times.length
  };
}
