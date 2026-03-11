/**
 * User Service
 * API calls for user profile and account management
 */

import { apiClient } from './apiClient';
import { API_ENDPOINTS } from '@shared/utils/constants';

export interface ChangeEmailRequest {
  newEmail: string;
  password: string; // Verify password before changing email
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface BlockUserRequest {
  reason: string;
}

export interface UnblockUserRequest {
  reason: string;
}

export interface ActivateUserRequest {
  activationNote?: string;
}

export interface DeactivateUserRequest {
  reason: string;
}

export interface DeleteUserRequest {
  reason: string;
}

export interface UserResponse {
  id: string;
  username: string;
  email: string;
  emailVerified: boolean;
  role: string;
  status: string;
  marketingConsent: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * User service class
 */
class UserService {
  /**
   * Get current authenticated user profile
   */
  async getCurrentUser(): Promise<UserResponse> {
    return apiClient.get(API_ENDPOINTS.users.getCurrentUser);
  }

  /**
   * Get user by ID
   */
  async getUserById(id: string): Promise<UserResponse> {
    return apiClient.get(API_ENDPOINTS.users.getById(id));
  }

  /**
   * Get user by email
   */
  async getUserByEmail(email: string): Promise<UserResponse> {
    return apiClient.get(API_ENDPOINTS.users.getByEmail(email));
  }

  /**
   * Get user by username
   */
  async getUserByUsername(username: string): Promise<UserResponse> {
    return apiClient.get(API_ENDPOINTS.users.getByUsername(username));
  }

  /**
   * Change user email
   */
  async changeEmail(request: ChangeEmailRequest): Promise<UserResponse> {
    return apiClient.put(API_ENDPOINTS.users.changeEmail, request);
  }

  /**
   * Change user password
   */
  async changePassword(request: ChangePasswordRequest): Promise<UserResponse> {
    return apiClient.put(API_ENDPOINTS.users.changePassword, request);
  }

  /**
   * Verify user email
   */
  async verifyEmail(): Promise<UserResponse> {
    return apiClient.post(API_ENDPOINTS.users.verifyEmail, {});
  }

  /**
   * Grant marketing consent
   */
  async grantMarketingConsent(): Promise<UserResponse> {
    return apiClient.post(API_ENDPOINTS.users.grantMarketingConsent, {});
  }

  /**
   * Revoke marketing consent
   */
  async revokeMarketingConsent(): Promise<UserResponse> {
    return apiClient.delete(API_ENDPOINTS.users.revokeMarketingConsent);
  }

  /**
   * Activate user (requires OWNER role)
   */
  async activateUser(id: string, request: ActivateUserRequest): Promise<UserResponse> {
    return apiClient.post(API_ENDPOINTS.users.activateUser(id), request);
  }

  /**
   * Deactivate user (requires OWNER role)
   */
  async deactivateUser(id: string, request: DeactivateUserRequest): Promise<UserResponse> {
    return apiClient.post(API_ENDPOINTS.users.deactivateUser(id), request);
  }

  /**
   * Block user (requires OWNER role)
   */
  async blockUser(id: string, request: BlockUserRequest): Promise<UserResponse> {
    return apiClient.post(API_ENDPOINTS.users.blockUser(id), request);
  }

  /**
   * Unblock user (requires OWNER role)
   */
  async unblockUser(id: string, request: UnblockUserRequest): Promise<UserResponse> {
    return apiClient.post(API_ENDPOINTS.users.unblockUser(id), request);
  }

  /**
   * Delete user (requires OWNER role)
   */
  async deleteUser(id: string, request: DeleteUserRequest): Promise<UserResponse> {
    return apiClient.delete(API_ENDPOINTS.users.deleteUser(id), { data: request });
  }
}

export default new UserService();
