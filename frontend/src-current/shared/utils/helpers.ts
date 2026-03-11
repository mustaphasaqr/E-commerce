/**
 * Shared Helpers
 * Common utility functions for arrays, objects, strings, etc.
 */

// ============ ARRAY UTILITIES ============

/**
 * Remove duplicate values from array
 */
export const uniq = <T>(array: T[]): T[] => {
  return Array.from(new Set(array));
};

/**
 * Remove duplicate objects by key
 */
export const uniqBy = <T extends Record<string, any>>(
  array: T[],
  key: keyof T
): T[] => {
  const seen = new Set();
  return array.filter((item) => {
    const value = item[key];
    if (seen.has(value)) return false;
    seen.add(value);
    return true;
  });
};

/**
 * Flatten nested array one level
 */
export const flatten = <T>(array: (T | T[])[]): T[] => {
  const result: T[] = [];
  for (const val of array) {
    if (Array.isArray(val)) {
      result.push(...(val as T[]));
    } else {
      result.push(val as T);
    }
  }
  return result;
};

/**
 * Flatten deeply nested array
 */
export const flattenDeep = (array: any[]): any[] => {
  return array.reduce(
    (acc, val) => acc.concat(Array.isArray(val) ? flattenDeep(val) : val),
    []
  );
};

/**
 * Chunk array into smaller arrays
 */
export const chunk = <T>(array: T[], size: number): T[][] => {
  const result: T[][] = [];
  for (let i = 0; i < array.length; i += size) {
    result.push(array.slice(i, i + size));
  }
  return result;
};

/**
 * Shuffle array (Fisher-Yates)
 */
export const shuffle = <T>(array: T[]): T[] => {
  const result = [...array];
  for (let i = result.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [result[i], result[j]] = [result[j], result[i]];
  }
  return result;
};

/**
 * Get random element from array
 */
export const sample = <T>(array: T[]): T | undefined => {
  return array[Math.floor(Math.random() * array.length)];
};

/**
 * Get n random elements from array
 */
export const sampleSize = <T>(array: T[], n: number): T[] => {
  return shuffle(array).slice(0, n);
};

/**
 * Find differences between two arrays
 */
export const difference = <T>(array1: T[], array2: T[]): T[] => {
  return array1.filter((item) => !array2.includes(item));
};

/**
 * Find intersection between two arrays
 */
export const intersection = <T>(array1: T[], array2: T[]): T[] => {
  return array1.filter((item) => array2.includes(item));
};

/**
 * Index array by key
 */
export const indexBy = <T extends Record<string, any>>(
  array: T[],
  key: keyof T
): Record<string, T> => {
  return array.reduce(
    (acc, item) => {
      acc[String(item[key])] = item;
      return acc;
    },
    {} as Record<string, T>
  );
};

/**
 * Group array by key
 */
export const groupBy = <T extends Record<string, any>>(
  array: T[],
  key: keyof T
): Record<string, T[]> => {
  return array.reduce(
    (acc, item) => {
      const groupKey = String(item[key]);
      if (!acc[groupKey]) {
        acc[groupKey] = [];
      }
      acc[groupKey].push(item);
      return acc;
    },
    {} as Record<string, T[]>
  );
};

// ============ OBJECT UTILITIES ============

/**
 * Deep clone object
 */
export const deepClone = <T>(obj: T): T => {
  if (obj === null || typeof obj !== 'object') return obj;
  if (obj instanceof Date) return new Date(obj.getTime()) as T;
  if (obj instanceof Array) {
    return obj.map((item) => deepClone(item)) as T;
  }
  if (obj instanceof Object) {
    const clonedObj = {} as T;
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key]);
      }
    }
    return clonedObj;
  }
  return obj;
};

/**
 * Shallow merge objects
 */
export const merge = <T extends Record<string, any>>(
  ...objects: Partial<T>[]
): T => {
  return Object.assign({}, ...objects) as T;
};

/**
 * Deep merge objects
 */
export const mergeDeep = <T extends Record<string, any>>(
  ...objects: Partial<T>[]
): T => {
  return objects.reduce((result: any, obj) => {
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        const value = obj[key];
        const existing = result[key];

        if (
          typeof value === 'object' &&
          value !== null &&
          !Array.isArray(value) &&
          typeof existing === 'object' &&
          existing !== null &&
          !Array.isArray(existing)
        ) {
          result[key] = mergeDeep(existing, value);
        } else {
          result[key] = value;
        }
      }
    }
    return result;
  }, {} as any) as T;
};

/**
 * Pick specific keys from object
 */
export const pick = <T extends Record<string, any>, K extends keyof T>(
  obj: T,
  keys: K[]
): Pick<T, K> => {
  const result = {} as Pick<T, K>;
  keys.forEach((key) => {
    if (key in obj) {
      result[key] = obj[key];
    }
  });
  return result;
};

/**
 * Omit specific keys from object
 */
export const omit = <T extends Record<string, any>, K extends keyof T>(
  obj: T,
  keys: K[]
): Omit<T, K> => {
  const result = { ...obj };
  keys.forEach((key) => {
    delete result[key];
  });
  return result as Omit<T, K>;
};

/**
 * Invert object keys and values
 */
export const invert = (obj: Record<string, string>): Record<string, string> => {
  return Object.entries(obj).reduce(
    (acc, [key, value]) => {
      acc[value] = key;
      return acc;
    },
    {} as Record<string, string>
  );
};

/**
 * Check if object is empty
 */
export const isEmpty = (obj: Record<string, any>): boolean => {
  return Object.keys(obj).length === 0;
};

/**
 * Get all values from object
 */
export const values = <T>(obj: Record<string, T>): T[] => {
  return Object.values(obj);
};

// ============ STRING UTILITIES ============

/**
 * Convert camelCase to snake_case
 */
export const camelToSnake = (str: string): string => {
  return str.replace(/([A-Z])/g, '_$1').toLowerCase();
};

/**
 * Convert snake_case to camelCase
 */
export const snakeToCamel = (str: string): string => {
  return str.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
};

/**
 * Convert kebab-case to camelCase
 */
export const kebabToCamel = (str: string): string => {
  return str.replace(/-([a-z])/g, (_, letter) => letter.toUpperCase());
};

/**
 * Reverse string
 */
export const reverse = (str: string): string => {
  return str.split('').reverse().join('');
};

/**
 * Repeat string n times
 */
export const repeat = (str: string, n: number): string => {
  return str.repeat(n);
};

/**
 * Pad string on left
 */
export const padStart = (str: string, length: number, fillStr = ' '): string => {
  return str.padStart(length, fillStr);
};

/**
 * Pad string on right
 */
export const padEnd = (str: string, length: number, fillStr = ' '): string => {
  return str.padEnd(length, fillStr);
};

/**
 * Count occurrences of substring
 */
export const countOccurrences = (str: string, substring: string): number => {
  if (!substring) return 0;
  return str.split(substring).length - 1;
};

/**
 * Replace all occurrences
 */
export const replaceAll = (
  str: string,
  search: string,
  replace: string
): string => {
  return str.split(search).join(replace);
};

// ============ TYPE CHECKING UTILITIES ============

/**
 * Check if value is null or undefined
 */
export const isNil = (value: any): value is null | undefined => {
  return value === null || value === undefined;
};

/**
 * Check if value is null
 */
export const isNull = (value: any): value is null => {
  return value === null;
};

/**
 * Check if value is undefined
 */
export const isUndefined = (value: any): value is undefined => {
  return value === undefined;
};

/**
 * Check if value is number
 */
export const isNumber = (value: any): value is number => {
  return typeof value === 'number' && !isNaN(value);
};

/**
 * Check if value is string
 */
export const isString = (value: any): value is string => {
  return typeof value === 'string';
};

/**
 * Check if value is boolean
 */
export const isBoolean = (value: any): value is boolean => {
  return typeof value === 'boolean';
};

/**
 * Check if value is object (not array, not null)
 */
export const isObject = (value: any): value is Record<string, any> => {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
};

/**
 * Check if value is array
 */
export const isArray = (value: any): value is any[] => {
  return Array.isArray(value);
};

/**
 * Check if value is function
 */
export const isFunction = (value: any): value is Function => {
  return typeof value === 'function';
};

/**
 * Check if value is promise
 */
export const isPromise = (value: any): boolean => {
  return value instanceof Promise;
};

// ============ UTILITY FUNCTIONS ============

/**
 * Safe JSON parse with fallback
 */
export const safeJsonParse = <T>(str: string, fallback: T): T => {
  try {
    return JSON.parse(str) as T;
  } catch {
    return fallback;
  }
};

/**
 * Safe JSON stringify with fallback
 */
export const safeJsonStringify = (value: any, fallback = ''): string => {
  try {
    return JSON.stringify(value);
  } catch {
    return fallback;
  }
};

/**
 * Wait for specified milliseconds
 */
export const wait = (ms: number): Promise<void> => {
  return new Promise((resolve) => setTimeout(resolve, ms));
};

/**
 * Retry function with exponential backoff
 */
export const retry = async <T>(
  fn: () => Promise<T>,
  options: {
    maxRetries?: number;
    delay?: number;
    backoff?: number;
  } = {}
): Promise<T> => {
  const { maxRetries = 3, delay = 1000, backoff = 2 } = options;

  let lastError: Error | null = null;

  for (let i = 0; i <= maxRetries; i++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error as Error;
      if (i < maxRetries) {
        const waitTime = delay * Math.pow(backoff, i);
        await wait(waitTime);
      }
    }
  }

  throw lastError || new Error('Retry failed');
};

/**
 * Get value by path string
 */
export const getByPath = (
  obj: Record<string, any>,
  path: string,
  fallback?: any
): any => {
  const keys = path.split('.');
  let result: any = obj;

  for (const key of keys) {
    result = result?.[key];
    if (result === undefined) {
      return fallback;
    }
  }

  return result;
};

/**
 * Set value by path string
 */
export const setByPath = (
  obj: Record<string, any>,
  path: string,
  value: any
): Record<string, any> => {
  const result = { ...obj };
  const keys = path.split('.');
  let current = result;

  for (let i = 0; i < keys.length - 1; i++) {
    const key = keys[i];
    if (!current[key] || typeof current[key] !== 'object') {
      current[key] = {};
    }
    current = current[key];
  }

  current[keys[keys.length - 1]] = value;
  return result;
};
