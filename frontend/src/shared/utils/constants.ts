/**
 * Application-wide Constants
 * API endpoints, timeouts, error codes, limits, etc.
 */

// ============ API CONFIGURATION ============

export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export const API_TIMEOUT = 30000; // 30 seconds

export const API_RETRY_ATTEMPTS = 3;

export const API_RETRY_DELAY = 1000; // 1 second

// ============ API ENDPOINTS ============

export const API_ENDPOINTS = {
  // Auth - 6 endpoints
  auth: {
    login: '/auth/login',                               // POST - Public
    logout: '/auth/logout',                             // POST - Protected
    refresh: '/auth/refresh',                           // POST - Protected
    logoutAll: '/auth/logout-all',                      // POST - Protected
    passwordResetRequest: '/auth/password-reset/request',     // POST - Public
    passwordResetComplete: '/auth/password-reset/complete',   // POST - Public
  },

  // Users - 15 endpoints
  users: {
    register: '/users',                                 // POST - Public
    getCurrentUser: '/users/me',                        // GET - Protected
    getById: (id: string) => `/users/${id}`,            // GET - Protected
    getByEmail: (email: string) => `/users/email/${email}`, // GET - Protected
    getByUsername: (username: string) => `/users/username/${username}`, // GET - Protected
    changeEmail: '/users/me/email',                     // PUT - Protected
    changePassword: '/users/me/password',               // PUT - Protected
    verifyEmail: '/users/me/email/verify',              // POST - Protected
    grantMarketingConsent: '/users/me/marketing/grant', // POST - Protected
    revokeMarketingConsent: '/users/me/marketing',      // DELETE - Protected
    activateUser: (id: string) => `/users/${id}/activate`, // POST - OWNER
    deactivateUser: (id: string) => `/users/${id}/deactivate`, // POST - OWNER
    blockUser: (id: string) => `/users/${id}/block`,    // POST - OWNER
    unblockUser: (id: string) => `/users/${id}/unblock`, // POST - OWNER
    deleteUser: (id: string) => `/users/${id}`,         // DELETE - OWNER
  },

  // Admin - 9 endpoints (requires OWNER role)
  admin: {
    users: {
      blockUser: (id: string) => `/admin/users/${id}/block`,            // POST - OWNER
      unblockUser: (id: string) => `/admin/users/${id}/unblock`,        // POST - OWNER
      activateUser: (id: string) => `/admin/users/${id}/activate`,      // POST - OWNER
      deactivateUser: (id: string) => `/admin/users/${id}/deactivate`,  // POST - OWNER
      deleteUser: (id: string) => `/admin/users/${id}`,                 // DELETE - OWNER
      listUsers: '/admin/users',                                        // GET - OWNER
      searchUsersGet: '/admin/users/search',                            // GET - OWNER
      searchUsersPost: '/admin/users/search',                           // POST - OWNER
      changeUserRole: (id: string) => `/admin/users/${id}/role`,        // POST - OWNER
    },
  },

  // Products (kept for reference)
  products: {
    list: '/products',
    search: '/products/search',
    getById: (id: string) => `/products/${id}`,
    categories: '/products/categories',
    reviews: (id: string) => `/products/${id}/reviews`,
    createReview: (id: string) => `/products/${id}/reviews`,
    recommendations: '/products/recommendations',
    filters: '/products/filters',
  },

  // Cart (kept for reference)
  cart: {
    get: '/cart',
    add: '/cart/items',
    update: (itemId: string) => `/cart/items/${itemId}`,
    remove: (itemId: string) => `/cart/items/${itemId}`,
    clear: '/cart/clear',
    validate: '/cart/validate',
    applyCoupon: '/cart/coupons',
    removeCoupon: (couponId: string) => `/cart/coupons/${couponId}`,
  },

  // Orders (kept for reference)
  orders: {
    list: '/orders',
    getById: (id: string) => `/orders/${id}`,
    create: '/orders',
    checkout: '/orders/checkout',
    cancel: (id: string) => `/orders/${id}/cancel`,
    return: (id: string) => `/orders/${id}/return`,
    track: (id: string) => `/orders/${id}/track`,
    invoice: (id: string) => `/orders/${id}/invoice`,
    payment: '/orders/payment',
  },

  // Health
  health: {
    ping: '/health/ping',
    status: '/health/status',
  },
} as const;

// ============ HTTP STATUS CODES ============

export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  ACCEPTED: 202,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  INTERNAL_SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503,
} as const;

// ============ ERROR CODES ============

export const ERROR_CODES = {
  // Authentication
  INVALID_CREDENTIALS: 'INVALID_CREDENTIALS',
  EXPIRED_TOKEN: 'EXPIRED_TOKEN',
  INVALID_TOKEN: 'INVALID_TOKEN',
  NO_TOKEN: 'NO_TOKEN',
  REFRESH_FAILED: 'REFRESH_FAILED',

  // Validation
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  MISSING_FIELDS: 'MISSING_FIELDS',
  INVALID_FORMAT: 'INVALID_FORMAT',

  // Authorization
  ACCESS_DENIED: 'ACCESS_DENIED',
  INSUFFICIENT_PERMISSIONS: 'INSUFFICIENT_PERMISSIONS',

  // Resources
  RESOURCE_NOT_FOUND: 'RESOURCE_NOT_FOUND',
  RESOURCE_ALREADY_EXISTS: 'RESOURCE_ALREADY_EXISTS',
  RESOURCE_CONFLICT: 'RESOURCE_CONFLICT',

  // Operations
  OPERATION_FAILED: 'OPERATION_FAILED',
  OPERATION_TIMEOUT: 'OPERATION_TIMEOUT',
  INVALID_OPERATION: 'INVALID_OPERATION',

  // Network
  NETWORK_ERROR: 'NETWORK_ERROR',
  NO_INTERNET: 'NO_INTERNET',
  CONNECTION_TIMEOUT: 'CONNECTION_TIMEOUT',

  // Server
  INTERNAL_ERROR: 'INTERNAL_ERROR',
  SERVICE_UNAVAILABLE: 'SERVICE_UNAVAILABLE',
} as const;

// ============ STORAGE KEYS ============

export const STORAGE_KEYS = {
  // Auth
  AUTH_TOKEN: 'auth_token',
  REFRESH_TOKEN: 'refresh_token',
  USER_ID: 'user_id',
  SESSION_ID: 'session_id',

  // User Data
  USER_PREFERENCES: 'user_preferences',
  THEME: 'theme',
  LANGUAGE: 'language',

  // Cart
  CART_DATA: 'cart_data',
  CART_TIMESTAMP: 'cart_timestamp',

  // UI State
  SIDEBAR_OPEN: 'sidebar_open',
  MODAL_STATE: 'modal_state',

  // Logs
  ERROR_LOGS: 'error_logs',
  ANALYTICS_CACHE: 'analytics_cache',
} as const;

// ============ PAGINATION ============

export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_PAGE_SIZE: 20,
  MAX_PAGE_SIZE: 100,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100],
} as const;

// ============ LIMITS ============

export const LIMITS = {
  // Form fields
  MIN_PASSWORD_LENGTH: 8,
  MAX_PASSWORD_LENGTH: 128,
  MIN_USERNAME_LENGTH: 3,
  MAX_USERNAME_LENGTH: 50,
  MAX_EMAIL_LENGTH: 255,
  MAX_NAME_LENGTH: 100,
  MAX_PHONE_LENGTH: 20,

  // Product
  MAX_PRODUCT_DESCRIPTION: 5000,
  MAX_PRODUCT_NAME: 255,
  MAX_REVIEW_LENGTH: 1000,
  MIN_REVIEW_LENGTH: 10,

  // Cart
  MAX_CART_ITEMS: 100,
  MAX_QUANTITY_PER_ITEM: 999,
  MIN_ORDER_AMOUNT: 1,

  // File uploads
  MAX_IMAGE_SIZE: 5242880, // 5MB
  MAX_FILE_SIZE: 10485760, // 10MB
  ALLOWED_IMAGE_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
} as const;

// ============ TIMING ============

export const DELAYS = {
  DEBOUNCE_SEARCH: 300,
  DEBOUNCE_SAVE: 1000,
  TOAST_DURATION: 3000,
  AUTO_LOGOUT: 15 * 60 * 1000, // 15 minutes
  SESSION_WARNING: 14 * 60 * 1000, // 14 minutes
  CACHE_TTL: 60 * 1000, // 1 minute
  TOKEN_REFRESH_BUFFER: 5 * 60 * 1000, // 5 minutes before expiry
} as const;

// ============ REGEX PATTERNS ============

export const PATTERNS = {
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE: /^[\d\s\-\+\(\)]{10,}$/,
  URL: /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/,
  SLUG: /^[a-z0-9]+(?:-[a-z0-9]+)*$/,
  CREDIT_CARD: /^\d{13,19}$/,
  ZIP_CODE: /^\d{5}(-\d{4})?$/,
  PASSWORD_STRONG: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
} as const;

// ============ ROLE-BASED ACCESS ============

export const ROLES = {
  CUSTOMER: 'CUSTOMER',
  ADMIN: 'ADMIN',
  MODERATOR: 'MODERATOR',
  SUPPORT: 'SUPPORT',
} as const;

export const PERMISSIONS = {
  // Product
  VIEW_PRODUCTS: 'view:products',
  CREATE_PRODUCT: 'create:product',
  EDIT_PRODUCT: 'edit:product',
  DELETE_PRODUCT: 'delete:product',

  // Orders
  VIEW_ORDERS: 'view:orders',
  CREATE_ORDER: 'create:order',
  EDIT_ORDER: 'edit:order',
  CANCEL_ORDER: 'cancel:order',

  // Users
  VIEW_USERS: 'view:users',
  EDIT_USER: 'edit:user',
  DELETE_USER: 'delete:user',
  DEACTIVATE_USER: 'deactivate:user',

  // Analytics
  VIEW_ANALYTICS: 'view:analytics',
  EXPORT_ANALYTICS: 'export:analytics',

  // Settings
  VIEW_SETTINGS: 'view:settings',
  EDIT_SETTINGS: 'edit:settings',
} as const;

// ============ ORDER STATUS ============

export const ORDER_STATUS = {
  PENDING: 'PENDING',
  CONFIRMED: 'CONFIRMED',
  PROCESSING: 'PROCESSING',
  SHIPPED: 'SHIPPED',
  DELIVERED: 'DELIVERED',
  CANCELLED: 'CANCELLED',
  REFUNDED: 'REFUNDED',
  RETURNED: 'RETURNED',
} as const;

export const PAYMENT_STATUS = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
  REFUNDED: 'REFUNDED',
  CANCELLED: 'CANCELLED',
} as const;

// ============ ANALYTICS EVENTS ============

export const ANALYTICS_EVENTS = {
  // Navigation
  PAGE_VIEW: 'page_view',
  ROUTE_CHANGE: 'route_change',

  // Product
  PRODUCT_VIEW: 'product_view',
  PRODUCT_ADD_TO_CART: 'add_to_cart',
  PRODUCT_REMOVE_FROM_CART: 'remove_from_cart',
  PRODUCT_SEARCH: 'product_search',
  PRODUCT_FILTER: 'product_filter',
  PRODUCT_REVIEW: 'product_review',

  // Cart
  CART_VIEW: 'cart_view',
  CART_UPDATE: 'cart_update',

  // Order
  CHECKOUT_START: 'checkout_start',
  CHECKOUT_COMPLETE: 'checkout_complete',
  ORDER_PLACED: 'order_placed',

  // User
  USER_LOGIN: 'user_login',
  USER_LOGOUT: 'user_logout',
  USER_REGISTER: 'user_register',

  // Errors
  ERROR_OCCURRED: 'error_occurred',
  API_CALL_FAILED: 'api_call_failed',
} as const;

// ============ DATE/TIME FORMATS ============

export const DATE_FORMATS = {
  ISO: 'YYYY-MM-DDTHH:mm:ss.SSSZ',
  DATE_ONLY: 'YYYY-MM-DD',
  TIME_ONLY: 'HH:mm:ss',
  DISPLAY_DATE: 'MMM DD, YYYY',
  DISPLAY_DATETIME: 'MMM DD, YYYY HH:mm',
  DISPLAY_TIME: 'HH:mm',
} as const;

// ============ SENTRY CONFIGURATION ============

export const SENTRY_DSN = import.meta.env.VITE_SENTRY_DSN;

export const SENTRY_ENVIRONMENT = import.meta.env.MODE || 'development';

export const SENTRY_TRACE_SAMPLE_RATE = 0.1; // 10% of transactions

export const SENTRY_REPLAY_SAMPLE_RATE = 0.1; // 10% of sessions

// ============ ENVIRONMENT FLAGS ============

export const IS_DEVELOPMENT = import.meta.env.DEV;

export const IS_PRODUCTION = import.meta.env.PROD;

export const IS_STAGING = import.meta.env.VITE_ENV === 'staging';

// ============ FEATURE FLAGS ============

export const FEATURES = {
  ANALYTICS_ENABLED: !IS_DEVELOPMENT,
  ERROR_TRACKING_ENABLED: true,
  PERFORMANCE_MONITORING_ENABLED: !IS_DEVELOPMENT,
  MOCK_API_ENABLED: IS_DEVELOPMENT && import.meta.env.VITE_MOCK_API === 'true',
  DEBUG_MODE: IS_DEVELOPMENT,
} as const;

// ============ CACHE CONFIGURATION ============

export const CACHE_CONFIG = {
  // Cache durations in milliseconds
  DURATION_SHORT: 5 * 60 * 1000, // 5 minutes
  DURATION_MEDIUM: 30 * 60 * 1000, // 30 minutes
  DURATION_LONG: 2 * 60 * 60 * 1000, // 2 hours

  // Cache keys
  PRODUCTS_LIST: 'cache:products:list',
  PRODUCT_DETAIL: (id: string) => `cache:product:${id}`,
  CATEGORIES: 'cache:categories',
  CART: 'cache:cart',
  USER_PROFILE: 'cache:user:profile',
} as const;
