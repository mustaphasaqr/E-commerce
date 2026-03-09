/**
 * Authentication Types
 * Models for auth domain (/api/v1/auth/*)
 */

export interface User {
  id: string;
  username: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  emailVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

export type UserRole = 'CUSTOMER' | 'EMPLOYEE' | 'OWNER' | 'ADMIN';
export type UserStatus = 'PENDING' | 'ACTIVE' | 'INACTIVE' | 'BLOCKED';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  user: User;
  token: string;
  refreshToken: string;
  expiresIn: number;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  termsAccepted: boolean;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface LogoutRequest {
  token: string;
}

export interface PasswordResetRequest {
  email: string;
}

export interface PasswordResetConfirmRequest {
  token: string;
  newPassword: string;
}

export interface MFASetupRequest {
  userId: string;
}

export interface MFAVerifyRequest {
  code: string;
  token: string;
}
