/**
 * Auth Module Types - Re-exported from auth.types
 */

export type * from './auth.types';
export type {
  User,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  RefreshTokenRequest,
  LogoutRequest,
  PasswordResetRequest,
  PasswordResetConfirmRequest,
  EmailVerificationRequest,
  EmailVerificationVerifyRequest,
} from './auth.types';

/**
 * Authentication tokens (extracted from response)
 */
export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

/**
 * Auth state
 */
export interface AuthState {
  user: import('./auth.types').User | null;
  tokens: AuthTokens | null;
  isLoading: boolean;
  error: string | null;
  isAuthenticated: boolean;
}
