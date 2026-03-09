/**
 * Error Types
 * Centralized error structures and error handling types
 */

import { HttpStatusCode } from './common.types';

export interface FieldError {
  field: string;
  message: string;
  code: string;
}

export interface ApiErrorResponse {
  code: string;
  message: string;
  details?: {
    [key: string]: string | string[];
  };
  timestamp: string;
  path: string;
  validationErrors?: FieldError[];
  hint?: string;
}

export class ApiError extends Error {
  public readonly statusCode: HttpStatusCode;
  public readonly code: string;
  public readonly details?: ApiErrorResponse['details'];
  public readonly validationErrors?: FieldError[];
  public readonly hint?: string;
  public readonly originalError?: any;

  constructor(
    message: string,
    statusCode: HttpStatusCode = 500,
    code: string = 'INTERNAL_ERROR',
    options?: {
      details?: ApiErrorResponse['details'];
      validationErrors?: FieldError[];
      hint?: string;
      originalError?: any;
    }
  ) {
    super(message);
    this.name = 'ApiError';
    this.statusCode = statusCode;
    this.code = code;
    this.details = options?.details;
    this.validationErrors = options?.validationErrors;
    this.hint = options?.hint;
    this.originalError = options?.originalError;
  }

  /**
   * Check if error is validation error
   */
  isValidationError(): boolean {
    return this.statusCode === 422 || (this.validationErrors !== undefined && this.validationErrors.length > 0);
  }

  /**
   * Check if error is authentication error
   */
  isAuthError(): boolean {
    return this.statusCode === 401;
  }

  /**
   * Check if error is authorization error
   */
  isAuthorizationError(): boolean {
    return this.statusCode === 403;
  }

  /**
   * Check if error is not found error
   */
  isNotFoundError(): boolean {
    return this.statusCode === 404;
  }

  /**
   * Check if error is server error
   */
  isServerError(): boolean {
    return this.statusCode >= 500;
  }

  /**
   * Check if error is network/connectivity error
   */
  isNetworkError(): boolean {
    return this.code === 'NETWORK_ERROR' || this.code === 'TIMEOUT';
  }

  /**
   * Get user-friendly error message
   */
  getUserMessage(): string {
    const userMessages: { [key: string]: string } = {
      VALIDATION_ERROR: 'Please check your input and try again',
      UNAUTHORIZED: 'Your session has expired. Please log in again',
      FORBIDDEN: 'You do not have permission to perform this action',
      NOT_FOUND: 'The requested resource was not found',
      CONFLICT: 'This item already exists',
      NETWORK_ERROR: 'Network connection error. Please check your internet',
      TIMEOUT: 'Request took too long. Please try again',
      INTERNAL_ERROR: 'Something went wrong. Please try again later',
    };

    return userMessages[this.code] || this.message;
  }
}

export type ErrorHandler = (error: ApiError) => void;

export interface ErrorContextValue {
  error: ApiError | null;
  setError: (error: ApiError | null) => void;
  clearError: () => void;
  hasError: boolean;
}

export enum ErrorSeverity {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
  CRITICAL = 'critical',
}

export interface ErrorLog {
  id: string;
  timestamp: string;
  severity: ErrorSeverity;
  message: string;
  code: string;
  stackTrace?: string;
  context?: Record<string, any>;
  userId?: string;
  sessionId?: string;
}
