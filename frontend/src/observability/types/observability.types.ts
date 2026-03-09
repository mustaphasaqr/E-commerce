/**
 * Observability Types
 * Models for client-side technical observability
 * Includes error tracking (Sentry), analytics (GA), performance metrics
 */

export enum EventCategory {
  USER_ACTION = 'user_action',
  BUSINESS_EVENT = 'business_event',
  ERROR = 'error',
  PERFORMANCE = 'performance',
  NAVIGATION = 'navigation',
  API_CALL = 'api_call',
}

export interface TrackingEvent {
  id: string;
  timestamp: string;
  category: EventCategory;
  action: string;
  label?: string;
  value?: number | string;
  context?: Record<string, any>;
  userId?: string;
  sessionId: string;
  pageUrl: string;
}

export interface ErrorEvent {
  id: string;
  timestamp: string;
  message: string;
  errorCode?: string;
  stackTrace?: string;
  severity: 'critical' | 'error' | 'warning';
  context?: Record<string, any>;
  userId?: string;
  sessionId: string;
  pageUrl: string;
  originalError?: any;
}

export interface PerformanceMetric {
  metric: PerformanceMetricType;
  timestamp: string;
  value: number;
  unit: string;
  metadata?: Record<string, any>;
}

export enum PerformanceMetricType {
  // Core Web Vitals
  LCP = 'LCP', // Largest Contentful Paint
  FID = 'FID', // First Input Delay
  CLS = 'CLS', // Cumulative Layout Shift

  // Additional important metrics
  FCP = 'FCP', // First Contentful Paint
  TTFB = 'TTFB', // Time To First Byte
  DCL = 'DCL', // DOM Content Loaded
  LOAD = 'LOAD', // Window Load
  FP = 'FP', // First Paint

  // API metrics
  API_RESPONSE_TIME = 'API_RESPONSE_TIME',
  API_ERROR_RATE = 'API_ERROR_RATE',
  API_CACHE_HIT_RATE = 'API_CACHE_HIT_RATE',

  // JS metrics
  LONG_TASK = 'LONG_TASK',
  TASK_DURATION = 'TASK_DURATION',
  MEMORY_USAGE = 'MEMORY_USAGE',

  // Custom metrics
  PAGE_TIME = 'PAGE_TIME',
  USER_INTERACTION_TIME = 'USER_INTERACTION_TIME',
}

export interface PageViewEvent {
  id: string;
  timestamp: string;
  pageUrl: string;
  title: string;
  referrer?: string;
  viewDuration?: number;
  userId?: string;
  sessionId: string;
  metadata?: Record<string, any>;
}

export interface ApiCallEvent {
  id: string;
  timestamp: string;
  method: string;
  endpoint: string;
  statusCode: number;
  duration: number; // ms
  userId?: string;
  sessionId: string;
  size?: number; // bytes
  cached?: boolean;
}

export interface SessionInfo {
  sessionId: string;
  userId?: string;
  startTime: string;
  lastActivityTime: string;
  duration: number; // in seconds
  pageViews: number;
  errorCount: number;
  theme: string;
  language: string;
  userAgent: string;
  device: 'desktop' | 'mobile' | 'tablet';
  browser: string;
  os: string;
  viewport: {
    width: number;
    height: number;
  };
  screenResolution: {
    width: number;
    height: number;
  };
  connectionType?: string;
  isOnline: boolean;
}

export interface UserBehavior {
  sessionId: string;
  userId?: string;
  clickCount: number;
  scrollDepth: number; // percentage
  timeOnPage: number; // seconds
  interactionCount: number;
  formInteractions: number;
  searchCount: number;
  filterActions: number;
  checkoutStepsCompleted: number;
}

export enum LogLevel {
  DEBUG = 'DEBUG',
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR',
}

export interface LogEntry {
  timestamp: string;
  level: LogLevel;
  message: string;
  context?: Record<string, any>;
  userId?: string;
  sessionId?: string;
  stackTrace?: string;
}

export interface SentryConfig {
  dsn: string;
  environment: 'development' | 'staging' | 'production';
  sampleRate: number;
  tracesSampleRate: number;
  beforeSend?: (event: any) => any;
  integrations?: string[];
}

export interface AnalyticsConfig {
  googleAnalyticsId: string;
  enableTracking: boolean;
  trackingLevel: 'minimal' | 'standard' | 'full';
  anonymizeIp: boolean;
  cookieConsent?: boolean;
}

export interface ObservabilityConfig {
  sentry?: SentryConfig;
  googleAnalytics?: AnalyticsConfig;
  enableLocalLogging: boolean;
  enablePerformanceMonitoring: boolean;
  enableErrorTracking: boolean;
  enableSessionTracking: boolean;
  logLevel: LogLevel;
  batchingEnabled: boolean;
  batchInterval: number; // milliseconds
  maxBatchSize: number;
}

export interface Vital {
  name: PerformanceMetricType;
  value: number;
  rating: 'good' | 'needs-improvement' | 'poor';
  threshold: {
    good: number;
    needsImprovement: number;
  };
}

export interface CoreWebVitals {
  lcp: Vital;
  fid: Vital;
  cls: Vital;
}

export interface CrashReport {
  id: string;
  timestamp: string;
  message: string;
  stackTrace: string;
  userId?: string;
  sessionId: string;
  pageUrl: string;
  deviceInfo: {
    userAgent: string;
    os: string;
    browser: string;
  };
  reproductionSteps?: string[];
  screenshotUrl?: string;
}
