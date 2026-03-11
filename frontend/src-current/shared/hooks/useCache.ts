/**
 * useCache Hook
 * Custom hook for caching API responses with expiry
 */

import { useState, useCallback, useRef, useEffect } from 'react';

/**
 * Cache entry
 */
interface CacheEntry<T> {
  data: T;
  timestamp: number;
  expiresAt: number;
}

/**
 * Default cache TTL (1 minute)
 */
const DEFAULT_TTL = 60000;

/**
 * Global cache store
 */
const globalCache = new Map<string, CacheEntry<any>>();

/**
 * useCache hook - caches data with expiry
 */
export const useCache = <T>(
  key: string,
  fn: () => Promise<T>,
  options?: {
    ttl?: number;
    skipCache?: boolean;
  }
): {
  data: T | null;
  isLoading: boolean;
  error: Error | null;
  refetch: () => Promise<void>;
  clear: () => void;
} => {
  const { ttl = DEFAULT_TTL, skipCache = false } = options || {};

  const [data, setData] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const isMountedRef = useRef(true);

  /**
   * Check if cache is valid
   */
  const isCacheValid = useCallback((): boolean => {
    if (skipCache) return false;

    const cached = globalCache.get(key);
    if (!cached) return false;

    const isExpired = Date.now() > cached.expiresAt;
    if (isExpired) {
      globalCache.delete(key);
      return false;
    }

    return true;
  }, [key, skipCache]);

  /**
   * Get from cache
   */
  const getFromCache = useCallback((): T | null => {
    const cached = globalCache.get(key);
    return cached?.data ?? null;
  }, [key]);

  /**
   * Set in cache
   */
  const setInCache = useCallback(
    (value: T): void => {
      globalCache.set(key, {
        data: value,
        timestamp: Date.now(),
        expiresAt: Date.now() + ttl,
      });
    },
    [key, ttl]
  );

  /**
   * Clear cache
   */
  const clearCache = useCallback((): void => {
    globalCache.delete(key);
    setData(null);
    setError(null);
  }, [key]);

  /**
   * Fetch data
   */
  const fetch = useCallback(async (): Promise<void> => {
    // Check cache first
    if (isCacheValid()) {
      const cached = getFromCache();
      if (isMountedRef.current) {
        setData(cached);
        setError(null);
      }
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const result = await fn();

      if (isMountedRef.current) {
        setData(result);
        setInCache(result);
        setError(null);
      }
    } catch (err) {
      if (isMountedRef.current) {
        setData(null);
        setError(err instanceof Error ? err : new Error(String(err)));
      }
    } finally {
      if (isMountedRef.current) {
        setIsLoading(false);
      }
    }
  }, [fn, isCacheValid, getFromCache, setInCache]);

  /**
   * Initial fetch on mount
   */
  useEffect(() => {
    fetch();

    return () => {
      isMountedRef.current = false;
    };
  }, [fetch]);

  return {
    data,
    isLoading,
    error,
    refetch: fetch,
    clear: clearCache,
  };
};

/**
 * useCacheKey hook - creates/manages a cache key
 */
export const useCacheKey = (
  ...parts: (string | number | undefined | null)[]
): string => {
  return parts.filter((part) => part !== undefined && part !== null).join(':');
};

/**
 * Clear all cache
 */
export const clearAllCache = (): void => {
  globalCache.clear();
};

/**
 * Clear cache by pattern
 */
export const clearCacheByPattern = (pattern: RegExp): void => {
  Array.from(globalCache.keys())
    .filter((key) => pattern.test(key))
    .forEach((key) => globalCache.delete(key));
};

/**
 * Get cache statistics
 */
export const getCacheStats = (): {
  size: number;
  keys: string[];
} => {
  return {
    size: globalCache.size,
    keys: Array.from(globalCache.keys()),
  };
};
