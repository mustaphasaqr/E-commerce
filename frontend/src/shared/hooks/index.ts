/**
 * Shared Hooks - Main Export
 * Central point for importing all custom hooks
 */

export { useApi, useGet, usePost, usePut, usePatch, useDelete } from './useApi';
export type { UseApiOptions, UseApiResult, RequestStatus } from './useApi';

export { useLocalStorage, useSessionStorage } from './useLocalStorage';

export { useForm } from './useForm';
export type { FormState, FormValidator, FormSubmitHandler, UseFormOptions, UseFormResult } from './useForm';

export {
  useDebounce,
  useThrottle,
  useDebouncedCallback,
  useThrottledCallback,
} from './useDebounce';

export {
  useCache,
  useCacheKey,
  clearAllCache,
  clearCacheByPattern,
  getCacheStats,
} from './useCache';
