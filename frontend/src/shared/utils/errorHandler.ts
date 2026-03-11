/**
 * Centralized Error Handler
 * Unified error processing, logging, and reporting
 */

// import * as Sentry from '@sentry/react';
// Sentry is optional - commented out for now

/**
 * Error severity levels
 */
export type ErrorSeverity = 'critical' | 'error' | 'warning' | 'info';

/**
 * Error log entry
 */
export interface ErrorLog {
  message: string;
  code?: string;
  statusCode?: number;
  details?: Record<string, any>;
  severity: ErrorSeverity;
  timestamp: number;
  context?: Record<string, any>;
  userAgent?: string;
  url?: string;
}

/**
 * Normalized error representation
 */
export interface NormalizedError {
  message: string;
  code?: string;
  statusCode?: number;
  details?: Record<string, any>;
  timestamp: number;
  severity: ErrorSeverity;
  context?: Record<string, any>;
}

/**
 * Error handler configuration
 */
export interface ErrorHandlerConfig {
  enableSentry?: boolean;
  enableLogging?: boolean;
  enableNotification?: boolean;
  isDevelopment?: boolean;
}

/**
 * Global error handler context
 */
let config: ErrorHandlerConfig = {
  enableSentry: true,
  enableLogging: true,
  enableNotification: true,
  isDevelopment: import.meta.env.DEV,
};

/**
 * Initialize error handler
 */
export const initErrorHandler = (options: Partial<ErrorHandlerConfig>): void => {
  config = { ...config, ...options };
};

/**
 * Normalize error to standard format
 */
export const normalizeError = (error: any): NormalizedError => {
  let message = 'An unexpected error occurred';
  let code: string | undefined;
  let statusCode: number | undefined;
  let details: Record<string, any> | undefined;
  let severity: ErrorSeverity = 'error';

  // Handle Error objects
  if (error instanceof Error) {
    message = error.message;
  }
  // Handle API error responses
  else if (error?.response?.data) {
    const data = error.response.data;
    message = data.message || data.msg || message;
    statusCode = error.response.status;
    code = data.code || `HTTP_${statusCode}`;
    details = data.details || { data };

    // Determine severity by status code
    if (statusCode != null && statusCode >= 500) {
      severity = 'critical';
    } else if (statusCode != null && statusCode >= 400) {
      severity = 'warning';
    }
  }
  // Handle string errors
  else if (typeof error === 'string') {
    message = error;
  }
  // Handle object with message
  else if (error?.message) {
    message = error.message;
    code = error.code;
    statusCode = error.statusCode;
    details = error.details;
    severity = error.severity || severity;
  }

  return {
    message,
    code,
    statusCode,
    details,
    severity,
    timestamp: Date.now(),
  };
};

/**
 * Get user-friendly error message
 */
export const getUserMessage = (error: NormalizedError): string => {
  // Don't expose internal details to users
  const statusCode = error.statusCode;

  if (statusCode === 401) {
    return 'Your session has expired. Please log in again.';
  }

  if (statusCode === 403) {
    return 'You do not have permission to perform this action.';
  }

  if (statusCode === 404) {
    return 'The requested resource was not found.';
  }

  if (statusCode === 409) {
    return 'This action conflicts with existing data.';
  }

  if (statusCode === 422) {
    return 'Please check your input and try again.';
  }

  if (statusCode && statusCode >= 500) {
    return 'A server error occurred. Please try again later.';
  }

  if (statusCode && statusCode >= 400) {
    return 'An error occurred. Please try again.';
  }

  // For client-side errors, use the original message
  if (!statusCode) {
    return error.message;
  }

  return 'Something went wrong. Please try again.';
};

/**
 * Log error locally
 */
const logErrorLocal = (error: NormalizedError, context?: Record<string, any>): void => {
  const logEntry: ErrorLog = {
    message: error.message,
    code: error.code,
    statusCode: error.statusCode,
    details: error.details,
    severity: error.severity,
    timestamp: error.timestamp,
    context,
    userAgent: typeof navigator !== 'undefined' ? navigator.userAgent : undefined,
    url: typeof window !== 'undefined' ? window.location.href : undefined,
  };

  // Log to console in development
  if (config.isDevelopment) {
    const method = error.severity === 'critical' ? 'error' : 'warn';
    console[method as 'error' | 'warn']('[ErrorHandler]', logEntry);
  }

  // Store in localStorage for debugging
  try {
    const logs = JSON.parse(localStorage.getItem('error_logs') || '[]');
    logs.push(logEntry);
    // Keep only last 50 errors
    if (logs.length > 50) {
      logs.shift();
    }
    localStorage.setItem('error_logs', JSON.stringify(logs));
  } catch {
    // Silently fail if localStorage is unavailable
  }
};

/**
 * Report error to Sentry (disabled - optional dependency)
 */
const reportToSentry = (
  _error: NormalizedError,
  _context?: Record<string, any>
): void => {
  if (!config.enableSentry) return;

  // Sentry reporting disabled - install @sentry/react to enable
  // For now, just log to console in development
  if (config.isDevelopment) {
    console.warn('[ErrorHandler] Sentry reporting disabled - consider installing @sentry/react');
  }
};

/**
 * Handle error comprehensively
 */
export const handleError = (
  error: any,
  options: {
    context?: Record<string, any>;
    showNotification?: boolean;
    logLocally?: boolean;
    reportSentry?: boolean;
  } = {}
): NormalizedError => {
  const {
    context,
    showNotification = config.enableNotification,
    logLocally = config.enableLogging,
    reportSentry = config.enableSentry,
  } = options;

  // Normalize error
  const normalized = normalizeError(error);

  // Log locally
  if (logLocally) {
    logErrorLocal(normalized, context);
  }

  // Report to Sentry
  if (reportSentry) {
    reportToSentry(normalized, context);
  }

  // Emit notification event (to be handled by notification service)
  if (showNotification && typeof window !== 'undefined') {
    const event = new CustomEvent('app:error', {
      detail: {
        message: getUserMessage(normalized),
        severity: normalized.severity,
        code: normalized.code,
      },
    });
    window.dispatchEvent(event);
  }

  return normalized;
};

/**
 * Handle API error specifically
 */
export const handleApiError = (
  error: any,
  context?: {
    endpoint?: string;
    method?: string;
    body?: any;
  }
): NormalizedError => {
  return handleError(error, {
    context: {
      type: 'API_ERROR',
      ...context,
    },
  });
};

/**
 * Handle form validation error
 */
export const handleValidationError = (
  error: any,
  context?: {
    formName?: string;
    fields?: string[];
  }
): NormalizedError => {
  return handleError(error, {
    context: {
      type: 'VALIDATION_ERROR',
      ...context,
    },
  });
};

/**
 * Handle async operation error
 */
export const handleAsyncError = async <T>(
  fn: () => Promise<T>,
  errorContext?: Record<string, any>
): Promise<{ data: T | null; error: NormalizedError | null }> => {
  try {
    const data = await fn();
    return { data, error: null };
  } catch (error) {
    const normalized = handleError(error, {
      context: errorContext,
      reportSentry: true,
    });
    return { data: null, error: normalized };
  }
};

/**
 * Create custom error instance
 */
export const createError = (
  message: string,
  options?: {
    code?: string;
    statusCode?: number;
    details?: Record<string, any>;
    severity?: ErrorSeverity;
  }
): NormalizedError => {
  return {
    message,
    code: options?.code,
    statusCode: options?.statusCode,
    details: options?.details,
    severity: options?.severity || 'error',
    timestamp: Date.now(),
  };
};

/**
 * Log error information (for debugging)
 */
export const logErrorInfo = (
  title: string,
  error: any,
  additionalInfo?: Record<string, any>
): void => {
  const normalized = normalizeError(error);
  console.group(`🔴 ${title}`);
  console.log('Message:', normalized.message);
  console.log('Code:', normalized.code);
  console.log('Status Code:', normalized.statusCode);
  console.log('Severity:', normalized.severity);
  if (normalized.details) {
    console.log('Details:', normalized.details);
  }
  if (additionalInfo) {
    console.log('Additional Info:', additionalInfo);
  }
  console.groupEnd();
};

/**
 * Get error logs from localStorage
 */
export const getErrorLogs = (): ErrorLog[] => {
  try {
    return JSON.parse(localStorage.getItem('error_logs') || '[]');
  } catch {
    return [];
  }
};

/**
 * Clear error logs from localStorage
 */
export const clearErrorLogs = (): void => {
  try {
    localStorage.removeItem('error_logs');
  } catch {
    // Silently fail
  }
};

/**
 * Export error logs as JSON
 */
export const exportErrorLogs = (): string => {
  const logs = getErrorLogs();
  return JSON.stringify(logs, null, 2);
};
