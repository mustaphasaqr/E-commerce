/**
 * Storage Service
 * Wrapper for LocalStorage and SessionStorage with type safety and error handling
 */

/**
 * Storage interface
 */
export interface IStorage {
  getItem<T>(key: string, fallback?: T): T | null;
  setItem<T>(key: string, value: T): boolean;
  removeItem(key: string): boolean;
  clear(): boolean;
  getAllKeys(): string[];
  getAllItems(): Record<string, any>;
  exists(key: string): boolean;
  size(): number;
}

/**
 * Generic storage implementation
 */
class StorageService implements IStorage {
  private storageType: 'localStorage' | 'sessionStorage';

  constructor(type: 'localStorage' | 'sessionStorage' = 'localStorage') {
    this.storageType = type;
  }

  /**
   * Get item from storage with type safety
   */
  getItem<T>(key: string, fallback?: T): T | null {
    try {
      const storage = this.getStorage();
      const item = storage?.getItem(key);

      if (item == null) {
        return fallback ?? null;
      }

      // Try to parse as JSON
      try {
        return JSON.parse(item) as T;
      } catch {
        // Return as string if not JSON
        return item as unknown as T;
      }
    } catch (error) {
      console.warn(`Failed to get item from ${this.storageType}:`, key, error);
      return fallback ?? null;
    }
  }

  /**
   * Set item in storage
   */
  setItem<T>(key: string, value: T): boolean {
    try {
      const storage = this.getStorage();
      if (!storage) return false;

      // Convert to JSON if object/array
      const serialized =
        typeof value === 'string'
          ? value
          : JSON.stringify(value);

      storage.setItem(key, serialized);
      return true;
    } catch (error) {
      console.warn(`Failed to set item in ${this.storageType}:`, key, error);
      return false;
    }
  }

  /**
   * Remove item from storage
   */
  removeItem(key: string): boolean {
    try {
      const storage = this.getStorage();
      if (!storage) return false;

      storage.removeItem(key);
      return true;
    } catch (error) {
      console.warn(`Failed to remove item from ${this.storageType}:`, key, error);
      return false;
    }
  }

  /**
   * Clear all items from storage
   */
  clear(): boolean {
    try {
      const storage = this.getStorage();
      if (!storage) return false;

      storage.clear();
      return true;
    } catch (error) {
      console.warn(`Failed to clear ${this.storageType}:`, error);
      return false;
    }
  }

  /**
   * Get all keys in storage
   */
  getAllKeys(): string[] {
    try {
      const storage = this.getStorage();
      if (!storage) return [];

      return Object.keys(storage);
    } catch (error) {
      console.warn(`Failed to get keys from ${this.storageType}:`, error);
      return [];
    }
  }

  /**
   * Get all items in storage
   */
  getAllItems(): Record<string, any> {
    try {
      const storage = this.getStorage();
      if (!storage) return {};

      const items: Record<string, any> = {};

      for (let i = 0; i < storage.length; i++) {
        const key = storage.key(i);
        if (key) {
          items[key] = this.getItem(key);
        }
      }

      return items;
    } catch (error) {
      console.warn(`Failed to get items from ${this.storageType}:`, error);
      return {};
    }
  }

  /**
   * Check if key exists
   */
  exists(key: string): boolean {
    try {
      const storage = this.getStorage();
      return storage?.getItem(key) !== null;
    } catch {
      return false;
    }
  }

  /**
   * Get storage size (approximate)
   */
  size(): number {
    try {
      let size = 0;
      const items = this.getAllItems();

      for (const value of Object.values(items)) {
        const str = typeof value === 'string' ? value : JSON.stringify(value);
        size += str.length;
      }

      return size;
    } catch {
      return 0;
    }
  }

  /**
   * Get the storage object
   */
  private getStorage(): Storage | null {
    if (typeof window === 'undefined') return null;

    try {
      const storage = this.storageType === 'localStorage'
        ? window.localStorage
        : window.sessionStorage;

      // Test write access
      const testKey = '__storage_test__';
      storage.setItem(testKey, 'test');
      storage.removeItem(testKey);

      return storage;
    } catch (error) {
      console.warn(`${this.storageType} not available:`, error);
      return null;
    }
  }
}

/**
 * Storage service with expiry support
 */
class StorageWithExpiry extends StorageService {
  /**
   * Set item with expiry time
   */
  setItemWithExpiry<T>(
    key: string,
    value: T,
    expiryMs: number
  ): boolean {
    const data = {
      value,
      expiresAt: Date.now() + expiryMs,
    };

    return this.setItem(key, data);
  }

  /**
   * Get item with expiry check
   */
  getItemWithExpiry<T>(key: string, fallback?: T): T | null {
    const data = this.getItem<{ value: T; expiresAt: number }>(key);

    if (!data) {
      return fallback ?? null;
    }

    // Check expiry
    if (data.expiresAt && Date.now() > data.expiresAt) {
      this.removeItem(key);
      return fallback ?? null;
    }

    return data.value ?? (fallback ?? null);
  }

  /**
   * Check if item exists and not expired
   */
  isValid(key: string): boolean {
    const data = this.getItem<{ expiresAt?: number }>(key);

    if (!data) {
      return false;
    }

    if (data.expiresAt && Date.now() > data.expiresAt) {
      this.removeItem(key);
      return false;
    }

    return true;
  }

  /**
   * Get remaining expiry time in ms
   */
  getExpiryTime(key: string): number | null {
    const data = this.getItem<{ expiresAt?: number }>(key);

    if (!data?.expiresAt) {
      return null;
    }

    const remaining = data.expiresAt - Date.now();
    return remaining > 0 ? remaining : null;
  }
}

// ============ SINGLETONS ============

/**
 * Local storage service
 */
export const localStorage = new StorageWithExpiry('localStorage');

/**
 * Session storage service
 */
export const sessionStorage = new StorageWithExpiry('sessionStorage');

/**
 * Create custom storage service
 */
export const createStorageService = (
  type: 'localStorage' | 'sessionStorage' = 'localStorage'
) => {
  return new StorageWithExpiry(type);
};
