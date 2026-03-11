/**
 * Common Types
 * Shared types and enums used across the entire application
 */

export enum RequestStatus {
  IDLE = 'idle',
  PENDING = 'pending',
  SUCCESS = 'success',
  ERROR = 'error',
  LOADING = 'loading',
  REFETCH = 'refetch',
}

export enum SortOrder {
  ASC = 'asc',
  DESC = 'desc',
}

export enum AsyncStatus {
  IDLE = 'idle',
  LOADING = 'loading',
  SUCCEEDED = 'succeeded',
  FAILED = 'failed',
}

export interface AsyncState<T> {
  status: AsyncStatus;
  data: T | null;
  error: string | null;
}

export interface PageMeta {
  page: number;
  pageSize: number;
  totalRecords: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev: boolean;
}

export interface PaginationParams {
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: SortOrder;
}

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  duration?: number;
  action?: {
    label: string;
    callback: () => void;
  };
}

export interface CacheEntry<T> {
  value: T;
  timestamp: number;
  ttl: number;
}

export enum HttpStatusCode {
  OK = 200,
  CREATED = 201,
  ACCEPTED = 202,
  NO_CONTENT = 204,
  BAD_REQUEST = 400,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  CONFLICT = 409,
  UNPROCESSABLE_ENTITY = 422,
  TOO_MANY_REQUESTS = 429,
  INTERNAL_SERVER_ERROR = 500,
  BAD_GATEWAY = 502,
  SERVICE_UNAVAILABLE = 503,
  GATEWAY_TIMEOUT = 504,
}

export enum FileType {
  IMAGE = 'image',
  DOCUMENT = 'document',
  VIDEO = 'video',
  AUDIO = 'audio',
  ARCHIVE = 'archive',
}

/**
 * Generic filter criteria used across modules
 */
export interface FilterValue {
  field: string;
  operator: 'equals' | 'contains' | 'gt' | 'gte' | 'lt' | 'lte' | 'in' | 'nin' | 'between';
  value: string | number | boolean | (string | number | boolean)[];
}

/**
 * Debounce configuration
 */
export interface DebounceConfig {
  wait: number;
  leading?: boolean;
  trailing?: boolean;
}

/**
 * Throttle configuration
 */
export interface ThrottleConfig {
  limit: number;
  interval: number;
}
