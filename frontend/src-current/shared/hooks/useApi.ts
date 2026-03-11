/**
 * useApi Hook
 * Generic hook for making API calls with loading, error, and data states
 */

import { useState, useCallback, useRef, useEffect } from 'react';
import { apiClient } from '@shared/services/apiClient';
import { NormalizedError, handleApiError } from '@shared/utils/errorHandler';

/**
 * Request status
 */
export type RequestStatus = 'idle' | 'loading' | 'success' | 'error';

/**
 * useApi options
 */
export interface UseApiOptions<T> {
  autoExecute?: boolean;
  onSuccess?: (data: T) => void;
  onError?: (error: NormalizedError) => void;
  onFinally?: () => void;
  timeout?: number;
}

/**
 * useApi return type
 */
export interface UseApiResult<T> {
  data: T | null;
  error: NormalizedError | null;
  status: RequestStatus;
  isLoading: boolean;
  isSuccess: boolean;
  isError: boolean;
  execute: (...args: any[]) => Promise<T | null>;
  reset: () => void;
  refetch: () => Promise<T | null>;
}

/**
 * Generic API fetching hook
 */
export const useApi = <T = any>(
  apiMethod: (...args: any[]) => Promise<T>,
  options: UseApiOptions<T> = {}
): UseApiResult<T> => {
  const {
    autoExecute = false,
    onSuccess,
    onError,
    onFinally,
  } = options;

  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<NormalizedError | null>(null);
  const [status, setStatus] = useState<RequestStatus>('idle');
  const argsRef = useRef<any[]>([]);
  const isMountedRef = useRef(true);

  /**
   * Execute API call
   */
  const execute = useCallback(
    async (...args: any[]): Promise<T | null> => {
      argsRef.current = args;
      setStatus('loading');
      setError(null);

      try {
        const result = await apiMethod(...args);

        if (isMountedRef.current) {
          setData(result);
          setStatus('success');
          setError(null);
          onSuccess?.(result);
        }

        return result;
      } catch (err) {
        const normalized = handleApiError(err, {
          endpoint: apiMethod.name,
        });

        if (isMountedRef.current) {
          setData(null);
          setStatus('error');
          setError(normalized);
          onError?.(normalized);
        }

        return null;
      } finally {
        if (isMountedRef.current) {
          onFinally?.();
        }
      }
    },
    [apiMethod, onSuccess, onError, onFinally]
  );

  /**
   * Refetch with same arguments
   */
  const refetch = useCallback(() => {
    return execute(...argsRef.current);
  }, [execute]);

  /**
   * Reset state
   */
  const reset = useCallback(() => {
    setData(null);
    setError(null);
    setStatus('idle');
    argsRef.current = [];
  }, []);

  /**
   * Auto-execute on mount
   */
  useEffect(() => {
    if (autoExecute) {
      execute();
    }

    return () => {
      isMountedRef.current = false;
    };
  }, [autoExecute, execute]);

  return {
    data,
    error,
    status,
    isLoading: status === 'loading',
    isSuccess: status === 'success',
    isError: status === 'error',
    execute,
    reset,
    refetch,
  };
};

/**
 * useGet hook
 */
export const useGet = <T = any>(
  url: string,
  options?: UseApiOptions<T>
): UseApiResult<T> => {
  return useApi(
    () => apiClient.get<T>(url),
    { ...options, autoExecute: options?.autoExecute ?? true }
  );
};

/**
 * usePost hook
 */
export const usePost = <T = any>(
  url: string,
  options?: UseApiOptions<T>
): UseApiResult<T> & {
  mutate: (data: any) => Promise<T | null>;
} => {
  const apiResult = useApi(
    (data: any) => apiClient.post<T>(url, data),
    options
  );

  return {
    ...apiResult,
    mutate: apiResult.execute,
  };
};

/**
 * usePut hook
 */
export const usePut = <T = any>(
  url: string,
  options?: UseApiOptions<T>
): UseApiResult<T> & {
  mutate: (data: any) => Promise<T | null>;
} => {
  const apiResult = useApi(
    (data: any) => apiClient.put<T>(url, data),
    options
  );

  return {
    ...apiResult,
    mutate: apiResult.execute,
  };
};

/**
 * usePatch hook
 */
export const usePatch = <T = any>(
  url: string,
  options?: UseApiOptions<T>
): UseApiResult<T> & {
  mutate: (data: any) => Promise<T | null>;
} => {
  const apiResult = useApi(
    (data: any) => apiClient.patch<T>(url, data),
    options
  );

  return {
    ...apiResult,
    mutate: apiResult.execute,
  };
};

/**
 * useDelete hook
 */
export const useDelete = <T = any>(
  url: string,
  options?: UseApiOptions<T>
): UseApiResult<T> & {
  mutate: () => Promise<T | null>;
} => {
  const apiResult = useApi(
    () => apiClient.delete<T>(url),
    options
  );

  return {
    ...apiResult,
    mutate: apiResult.execute,
  };
};
