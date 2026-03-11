/**
 * Auth Service
 * API calls for authentication and session management
 */

import { apiClient } from '@shared/services/apiClient';
import { API_ENDPOINTS } from '@shared/utils/constants';
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from '../types/auth.types';

/**
 * Auth service class
 */
class AuthService {
  /**
   * Login user with email/username and password
   */
  async login(data: LoginRequest): Promise<LoginResponse> {
    return apiClient.post(API_ENDPOINTS.auth.login, data);
  }

  /**
   * Register new user
   */
  async register(data: RegisterRequest): Promise<RegisterResponse> {
    return apiClient.post(API_ENDPOINTS.users.register, data);
  }

  /**
   * Logout current session
   */
  async logout(): Promise<void> {
    return apiClient.post(API_ENDPOINTS.auth.logout, {});
  }

  /**
   * Logout all devices (all sessions)
   */
  async logoutAll(): Promise<void> {
    return apiClient.post(API_ENDPOINTS.auth.logoutAll, {});
  }

  /**
   * Refresh authentication token
   */
  async refreshToken(refreshToken: string): Promise<LoginResponse> {
    return apiClient.post(API_ENDPOINTS.auth.refresh, { refreshToken });
  }

  /**
   * Request password reset
   */
  async requestPasswordReset(email: string): Promise<void> {
    return apiClient.post(API_ENDPOINTS.auth.passwordResetRequest, { email });
  }

  /**
   * Complete password reset with token
   */
  async completePasswordReset(token: string, newPassword: string): Promise<void> {
    return apiClient.post(API_ENDPOINTS.auth.passwordResetComplete, { token, newPassword });
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    try {
      const token = localStorage.getItem('auth_token');
      return !!token;
    } catch {
      return false;
    }
  }

  /**
   * Get stored auth token
   */
  getAuthToken(): string | null {
    try {
      return localStorage.getItem('auth_token');
    } catch {
      return null;
    }
  }

  /**
   * Get stored refresh token
   */
  getRefreshToken(): string | null {
    try {
      return localStorage.getItem('refresh_token');
    } catch {
      return null;
    }
  }

  /**
   * Store auth tokens in localStorage
   */
  setTokens(accessToken: string, refreshToken: string): void {
    try {
      localStorage.setItem('auth_token', accessToken);
      localStorage.setItem('refresh_token', refreshToken);
    } catch (error) {
      console.error('Failed to store tokens:', error);
    }
  }

  /**
   * Clear auth tokens from localStorage
   */
  clearTokens(): void {
    try {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('refresh_token');
    } catch (error) {
      console.error('Failed to clear tokens:', error);
    }
  }
}

// Export singleton instance
export const authService = new AuthService();

// Export default for backward compatibility
export default authService;
