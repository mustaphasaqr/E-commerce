/**
 * useLocalStorage Hook
 * Custom hook for managing state persisted in localStorage
 */

import { useState, useCallback, useEffect } from 'react';
import { localStorage as storageService } from '@shared/services/storage.service';

/**
 * useLocalStorage options
 */
interface UseLocalStorageOptions<T> {
  fallback?: T;
  serialize?: (value: T) => string;
  deserialize?: (value: string) => T;
  syncData?: boolean;
}

/**
 * useLocalStorage hook
 */
export const useLocalStorage = <T = any>(
  key: string,
  initialValue?: T,
  options?: UseLocalStorageOptions<T>
): [T, (value: T | ((val: T) => T)) => void, () => void] => {
  const { fallback, syncData = true } = options ?? {};

  const [storedValue, setStoredValue] = useState<T>(() => {
    try {
      const value = storageService.getItem<T>(key, fallback ?? initialValue);
      return value ?? fallback ?? initialValue!;
    } catch (error) {
      console.error(`Error reading from localStorage key "${key}":`, error);
      return fallback ?? initialValue!;
    }
  });

  /**
   * Update localStorage and state
   */
  const setValue = useCallback(
    (value: T | ((val: T) => T)) => {
      try {
        const valueToStore =
          value instanceof Function ? value(storedValue) : value;
        setStoredValue(valueToStore);
        storageService.setItem(key, valueToStore);

        // Dispatch custom event for syncing across tabs
        if (syncData) {
          window.dispatchEvent(
            new CustomEvent('local-storage', {
              detail: { key, value: valueToStore },
            })
          );
        }
      } catch (error) {
        console.error(`Error writing to localStorage key "${key}":`, error);
      }
    },
    [key, storedValue, syncData]
  );

  /**
   * Remove item from localStorage
   */
  const clearValue = useCallback(() => {
    try {
      storageService.removeItem(key);
      setStoredValue(fallback ?? initialValue!);

      if (syncData) {
        window.dispatchEvent(
          new CustomEvent('local-storage', {
            detail: { key, value: null },
          })
        );
      }
    } catch (error) {
      console.error(`Error clearing localStorage key "${key}":`, error);
    }
  }, [key, fallback, initialValue, syncData]);

  /**
   * Sync across tabs/windows
   */
  useEffect(() => {
    if (!syncData) return;

    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === key && e.newValue) {
        try {
          const newValue = JSON.parse(e.newValue);
          setStoredValue(newValue);
        } catch {
          setStoredValue(e.newValue as any);
        }
      }
    };

    const handleCustomEvent = (e: Event) => {
      const event = e as CustomEvent;
      if (event.detail?.key === key) {
        setStoredValue(event.detail?.value ?? fallback ?? initialValue!);
      }
    };

    window.addEventListener('storage', handleStorageChange);
    window.addEventListener('local-storage', handleCustomEvent);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('local-storage', handleCustomEvent);
    };
  }, [key, fallback, initialValue, syncData]);

  return [storedValue, setValue, clearValue];
};

/**
 * useSessionStorage hook
 */
export const useSessionStorage = <T = any>(
  key: string,
  initialValue?: T,
  options?: Omit<UseLocalStorageOptions<T>, 'syncData'>
): [T, (value: T | ((val: T) => T)) => void, () => void] => {
  const { fallback } = options ?? {};

  const [storedValue, setStoredValue] = useState<T>(() => {
    try {
      // Check if we're in browser
      if (typeof window === 'undefined') {
        return fallback ?? initialValue!;
      }

      const item = window.sessionStorage?.getItem(key);
      return item ? JSON.parse(item) : (fallback ?? initialValue!);
    } catch (error) {
      console.error(`Error reading from sessionStorage key "${key}":`, error);
      return fallback ?? initialValue!;
    }
  });

  const setValue = useCallback(
    (value: T | ((val: T) => T)) => {
      try {
        const valueToStore =
          value instanceof Function ? value(storedValue) : value;
        setStoredValue(valueToStore);

        if (typeof window !== 'undefined') {
          window.sessionStorage?.setItem(key, JSON.stringify(valueToStore));
        }
      } catch (error) {
        console.error(`Error writing to sessionStorage key "${key}":`, error);
      }
    },
    [key, storedValue]
  );

  const clearValue = useCallback(() => {
    try {
      if (typeof window !== 'undefined') {
        window.sessionStorage?.removeItem(key);
      }
      setStoredValue(fallback ?? initialValue!);
    } catch (error) {
      console.error(`Error clearing sessionStorage key "${key}":`, error);
    }
  }, [key, fallback, initialValue]);

  return [storedValue, setValue, clearValue];
};
