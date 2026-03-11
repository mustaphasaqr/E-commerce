/**
 * API Client Service
 * Axios wrapper with interceptors, error handling, and request/response transformation
 */

import axios, { AxiosInstance, AxiosRequestConfig, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL, API_TIMEOUT, API_RETRY_ATTEMPTS, API_RETRY_DELAY } from '@shared/utils/constants';
import { handleApiError } from '@shared/utils/errorHandler';

/**
 * API Client instance
 */
class ApiClient {
  private client: AxiosInstance;
  private retryCount: Map<string, number> = new Map();

  constructor(baseURL: string = API_BASE_URL) {
    this.client = axios.create({
      baseURL,
      timeout: API_TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
    });

    // Request interceptor
    this.client.interceptors.request.use(
      (config) => this.handleRequest(config),
      (error) => Promise.reject(error)
    );

    // Response interceptor
    this.client.interceptors.response.use(
      (response) => this.handleResponse(response),
      (error) => this.handleError(error)
    );
  }

  /**
   * Handle request - Add auth token, headers, etc.
   */
  private handleRequest(config: InternalAxiosRequestConfig<any>): InternalAxiosRequestConfig<any> {
    // Add auth token if available
    const token = this.getAuthToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Add request ID for tracing
    if (!config.headers) {
      config.headers = {} as any;
    }
    config.headers['X-Request-ID'] = this.generateRequestId();

    return config;
  }

  /**
   * Handle response - Extract data, handle pagination, etc.
   */
  private handleResponse(response: any): any {
    // Handle paginated responses
    if (response.data?.data && response.data?.pagination) {
      return {
        data: response.data.data,
        pagination: response.data.pagination,
        meta: response.data.meta,
      };
    }

    // Handle standard API response
    if (response.data?.data !== undefined) {
      return response.data.data;
    }

    // Return raw data
    return response.data;
  }

  /**
   * Handle error - Retry logic, token refresh, etc.
   */
  private async handleError(error: AxiosError): Promise<never> {
    const config = error.config;

    // Don't retry if no config or if retry disabled
    if (!config) {
      return Promise.reject(error);
    }

    // Get retry count for this request
    const url = config.url || '';
    const currentRetry = this.retryCount.get(url) || 0;

    // Retry logic
    if (
      currentRetry < API_RETRY_ATTEMPTS &&
      this.isRetryable(error)
    ) {
      this.retryCount.set(url, currentRetry + 1);

      // Wait before retrying with exponential backoff
      await this.delay(API_RETRY_DELAY * Math.pow(2, currentRetry));

      // Retry the request
      return this.client.request(config);
    }

    // Clear retry count
    this.retryCount.delete(url);

    // Handle 401 - token refresh
    if (error.response?.status === 401) {
      await this.handleUnauthorized();
    }

    // Normalize and throw error
    const normalized = handleApiError(error, {
      endpoint: config.url,
      method: config.method,
    });

    return Promise.reject(normalized);
  }

  /**
   * Handle unauthorized - refresh token or logout
   */
  private async handleUnauthorized(): Promise<void> {
    try {
      const refreshToken = this.getRefreshToken();
      if (!refreshToken) {
        this.logout();
        return;
      }

      // Try to refresh token
      const response = await axios.post(
        `${API_BASE_URL}/auth/refresh-token`,
        { refreshToken },
        { timeout: API_TIMEOUT }
      );

      const newToken = response.data?.token;
      if (newToken) {
        this.setAuthToken(newToken);
      } else {
        this.logout();
      }
    } catch (error) {
      this.logout();
    }
  }

  /**
   * Check if error is retryable
   */
  private isRetryable(error: AxiosError): boolean {
    const status = error.response?.status;

    // Retry on network errors
    if (!error.response) {
      return true;
    }

    // Retry on 5xx errors
    if (status && status >= 500) {
      return true;
    }

    // Retry on specific 4xx errors
    if (status === 408 || status === 429) {
      return true;
    }

    return false;
  }

  /**
   * Delay utility
   */
  private delay(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  /**
   * Generate unique request ID
   */
  private generateRequestId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Get stored auth token
   */
  private getAuthToken(): string | null {
    try {
      return localStorage.getItem('auth_token');
    } catch {
      return null;
    }
  }

  /**
   * Get stored refresh token
   */
  private getRefreshToken(): string | null {
    try {
      return localStorage.getItem('refresh_token');
    } catch {
      return null;
    }
  }

  /**
   * Set auth token
   */
  private setAuthToken(token: string): void {
    try {
      localStorage.setItem('auth_token', token);
    } catch {
      // Silently fail
    }
  }

  /**
   * Logout - clear tokens
   */
  private logout(): void {
    try {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('user_id');

      // Emit logout event
      window.dispatchEvent(new CustomEvent('auth:logout'));
    } catch {
      // Silently fail
    }
  }

  // ============ PUBLIC API METHODS ============

  /**
   * GET request
   */
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.get<T>(url, config);
    return response.data;
  }

  /**
   * POST request
   */
  async post<T>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.client.post<T>(url, data, config);
    return response.data;
  }

  /**
   * PUT request
   */
  async put<T>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.client.put<T>(url, data, config);
    return response.data;
  }

  /**
   * PATCH request
   */
  async patch<T>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.client.patch<T>(url, data, config);
    return response.data;
  }

  /**
   * DELETE request
   */
  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.delete<T>(url, config);
    return response.data;
  }

  /**
   * Upload file
   */
  async upload<T>(
    url: string,
    file: File,
    additionalData?: Record<string, any>
  ): Promise<T> {
    const formData = new FormData();
    formData.append('file', file);

    if (additionalData) {
      Object.entries(additionalData).forEach(([key, value]) => {
        formData.append(key, String(value));
      });
    }

    const response = await this.client.post<T>(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return response.data;
  }

  /**
   * Download file
   */
  async download(url: string, filename: string): Promise<void> {
    const response = await this.client.get(url, {
      responseType: 'blob',
    });

    const blob = response.data;
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  }

  /**
   * Set custom header
   */
  setHeader(key: string, value: string): void {
    this.client.defaults.headers.common[key] = value;
  }

  /**
   * Remove custom header
   */
  removeHeader(key: string): void {
    delete this.client.defaults.headers.common[key];
  }

  /**
   * Get axios instance for advanced usage
   */
  getInstance(): AxiosInstance {
    return this.client;
  }
}

// Export singleton instance
export const apiClient = new ApiClient();

// Export class for custom instances if needed
export { ApiClient };
