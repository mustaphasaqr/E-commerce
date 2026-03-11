/**
 * Auth Service
 * API calls for authentication
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
   * Login user
   */
  async login(data: LoginRequest): Promise<LoginResponse> {
    return apiClient.post(API_ENDPOINTS.auth.login, data);
  }

  /**
   * Register user
   */
  async register(data: RegisterRequest): Promise<RegisterResponse> {
    return apiClient.post(API_ENDPOINTS.auth.register, data);
  }

  /**
   * Logout user
   */
  async logout(): Promise<void> {
    return apiClient.post(API_ENDPOINTS.auth.logout, {});
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
   * Request email verification
   */
  async requestEmailVerification(email: string): Promise<void> {
    return apiClient.post(API_ENDPOINTS.auth.emailVerificationRequest, { email });
  }

  /**
   * Verify email with token
   */
  async verifyEmail(token: string): Promise<void> {
    return apiClient.post(API_ENDPOINTS.auth.emailVerificationVerify, { token });
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
   * Clear all tokens
   */
  clearTokens(): void {
    try {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('user_id');
      localStorage.removeItem('auth_user');
    } catch {
      // Silently fail
    }
  }
}

// Export singleton instance
export const authService = new AuthService();

// Export class for custom instances if needed
export { AuthService };
