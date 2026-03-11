/**
 * Shared Services - Main Export
 * Central point for importing all service classes
 */

export { apiClient, ApiClient } from './apiClient';
export { localStorage, sessionStorage, createStorageService } from './storage.service';
export type { IStorage } from './storage.service';
export { notificationService, notify } from './notification.service';
export type { Notification, NotificationEvent, NotificationType } from './notification.service';

// Auth services (from auth module)
export { default as authService } from '@auth/services/authService';

// User services (profile management)
export { default as userService } from './userService';
export type { ChangeEmailRequest, ChangePasswordRequest, UserResponse } from './userService';

// Admin services (user management)
export { default as adminService } from './adminService';
export type {
  BlockUserRequest,
  UnblockUserRequest,
  ActivateUserRequest,
  DeactivateUserRequest,
  DeleteUserRequest,
  ChangeUserRoleRequest,
  SearchUsersRequest,
  PaginatedUsersResponse,
} from './adminService';
