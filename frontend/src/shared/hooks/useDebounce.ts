/**
 * useDebounce Hook
 * Custom hook for debouncing values
 */

import { useState, useEffect } from 'react';

/**
 * useDebounce hook - debounces a value
 */
export const useDebounce = <T>(
  value: T,
  delay: number = 300
): T => {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
};

/**
 * useThrottle hook - throttles a value
 */
export const useThrottle = <T>(
  value: T,
  delay: number = 300
): T => {
  const [throttledValue, setThrottledValue] = useState<T>(value);
  const [lastRan, setLastRan] = useState<number>(Date.now());

  useEffect(() => {
    const handler = setTimeout(() => {
      if (Date.now() - lastRan >= delay) {
        setThrottledValue(value);
        setLastRan(Date.now());
      }
    }, delay - (Date.now() - lastRan));

    return () => clearTimeout(handler);
  }, [value, delay, lastRan]);

  return throttledValue;
};

/**
 * useDebouncedCallback - debounces a callback function
 */
export const useDebouncedCallback = <T extends (...args: any[]) => any>(
  callback: T,
  delay: number = 300
): ((...args: Parameters<T>) => void) => {
  const [timeoutId, setTimeoutId] = useState<NodeJS.Timeout | null>(null);

  const debouncedCallback = (...args: Parameters<T>): void => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }

    const newTimeoutId = setTimeout(() => {
      callback(...args);
    }, delay);

    setTimeoutId(newTimeoutId);
  };

  return debouncedCallback;
};

/**
 * useThrottledCallback - throttles a callback function
 */
export const useThrottledCallback = <T extends (...args: any[]) => any>(
  callback: T,
  delay: number = 300
): ((...args: Parameters<T>) => void) => {
  const [lastRan, setLastRan] = useState<number>(Date.now());

  const throttledCallback = (...args: Parameters<T>): void => {
    const now = Date.now();

    if (now - lastRan >= delay) {
      callback(...args);
      setLastRan(now);
    }
  };

  return throttledCallback;
};
