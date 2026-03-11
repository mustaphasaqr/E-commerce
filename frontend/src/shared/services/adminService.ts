/**
 * Admin Service
 * API calls for admin user management operations
 * Requires OWNER role
 */

import { apiClient } from './apiClient';
import { API_ENDPOINTS } from '@shared/utils/constants';

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

export interface ChangeUserRoleRequest {
  newRole: string; // CUSTOMER, ADMIN, MODERATOR, SUPPORT, OWNER
}

export interface SearchUsersRequest {
  email?: string;
  username?: string;
  status?: string; // ACTIVE, INACTIVE, BLOCKED
  role?: string; // CUSTOMER, ADMIN, etc
  page: number;
  size: number;
}

export interface PaginatedUsersResponse {
  content: Array<{
    id: string;
    username: string;
    email: string;
    role: string;
    status: string;
    createdAt: string;
  }>;
  number: number;
  size: number;
  totalPages: number;
  totalElements: number;
}

export interface UserResponse {
  id: string;
  username: string;
  email: string;
  role: string;
  status: string;
  emailVerified: boolean;
  marketingConsent: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Admin service class
 */
class AdminService {
  /**
   * Block user (prevents login)
   */
  async blockUser(userId: string, request: BlockUserRequest): Promise<UserResponse> {
    return apiClient.post(API_ENDPOINTS.admin.users.blockUser(userId), request);
  }

  /**
   * Unblock user (allows login again)
   */
  async unblockUser(userId: string, request: UnblockUserRequest): Promise<UserResponse> {
    return apiClient.post(API_ENDPOINTS.admin.users.unblockUser(userId), request);
  }

  /**
   * Activate user account
   */
  async activateUser(userId: string, request: ActivateUserRequest): Promise<UserResponse> {
    return apiClient.post(API_ENDPOINTS.admin.users.activateUser(userId), request);
  }

  /**
   * Deactivate user account
   */
  async deactivateUser(userId: string, request: DeactivateUserRequest): Promise<UserResponse> {
    return apiClient.post(API_ENDPOINTS.admin.users.deactivateUser(userId), request);
  }

  /**
   * Delete user permanently
   */
  async deleteUser(userId: string, request: DeleteUserRequest): Promise<UserResponse> {
    return apiClient.delete(API_ENDPOINTS.admin.users.deleteUser(userId), { data: request });
  }

  /**
   * List all users (paginated)
   */
  async listUsers(page: number = 0, size: number = 20): Promise<PaginatedUsersResponse> {
    return apiClient.get(`${API_ENDPOINTS.admin.users.listUsers}?page=${page}&size=${size}`);
  }

  /**
   * Search users with filter (GET method)
   */
  async searchUsersGet(
    email?: string,
    username?: string,
    status?: string,
    role?: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedUsersResponse> {
    const params = new URLSearchParams();
    if (email) params.append('email', email);
    if (username) params.append('username', username);
    if (status) params.append('status', status);
    if (role) params.append('role', role);
    params.append('page', page.toString());
    params.append('size', size.toString());

    return apiClient.get(`${API_ENDPOINTS.admin.users.searchUsersGet}?${params.toString()}`);
  }

  /**
   * Search users with filter (POST method)
   */
  async searchUsersPost(request: SearchUsersRequest): Promise<PaginatedUsersResponse> {
    return apiClient.post(API_ENDPOINTS.admin.users.searchUsersPost, request);
  }

  /**
   * Change user role
   */
  async changeUserRole(userId: string, request: ChangeUserRoleRequest): Promise<UserResponse> {
    return apiClient.post(API_ENDPOINTS.admin.users.changeUserRole(userId), request);
  }
}

export default new AdminService();
