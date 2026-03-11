/**
 * Notification Service
 * Centralized toast/notification management
 */

import { DELAYS } from '@shared/utils/constants';

/**
 * Notification type
 */
export type NotificationType = 'success' | 'error' | 'warning' | 'info';

/**
 * Notification object
 */
export interface Notification {
  id: string;
  type: NotificationType;
  message: string;
  title?: string;
  duration?: number;
  action?: {
    label: string;
    onClick: () => void;
  };
  dismissible?: boolean;
  timestamp: number;
}

/**
 * Notification event for components to listen to
 */
export interface NotificationEvent extends Notification {
  eventType?: 'add' | 'remove';
}

/**
 * Notification service
 */
class NotificationService {
  private notifications: Map<string, Notification> = new Map();
  private listeners: Set<(event: NotificationEvent) => void> = new Set();

  /**
   * Subscribe to notifications
   */
  subscribe(callback: (notification: NotificationEvent) => void): () => void {
    this.listeners.add(callback);

    // Return unsubscribe function
    return () => {
      this.listeners.delete(callback);
    };
  }

  /**
   * Emit notification to listeners
   */
  private emit(event: NotificationEvent): void {
    this.listeners.forEach((callback) => {
      try {
        callback(event);
      } catch (error) {
        console.error('Error in notification listener:', error);
      }
    });
  }

  /**
   * Generate unique ID
   */
  private generateId(): string {
    return `notification-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Show success notification
   */
  success(
    message: string,
    options?: {
      title?: string;
      duration?: number;
      action?: { label: string; onClick: () => void };
    }
  ): string {
    return this.show('success', message, options);
  }

  /**
   * Show error notification
   */
  error(
    message: string,
    options?: {
      title?: string;
      duration?: number;
      action?: { label: string; onClick: () => void };
    }
  ): string {
    return this.show('error', message, options);
  }

  /**
   * Show warning notification
   */
  warning(
    message: string,
    options?: {
      title?: string;
      duration?: number;
      action?: { label: string; onClick: () => void };
    }
  ): string {
    return this.show('warning', message, options);
  }

  /**
   * Show info notification
   */
  info(
    message: string,
    options?: {
      title?: string;
      duration?: number;
      action?: { label: string; onClick: () => void };
    }
  ): string {
    return this.show('info', message, options);
  }

  /**
   * Show generic notification
   */
  show(
    type: NotificationType,
    message: string,
    options?: {
      title?: string;
      duration?: number;
      action?: { label: string; onClick: () => void };
      dismissible?: boolean;
    }
  ): string {
    const id = this.generateId();
    const duration = options?.duration ?? DELAYS.TOAST_DURATION;
    const dismissible = options?.dismissible ?? true;

    const notification: Notification = {
      id,
      type,
      message,
      title: options?.title,
      duration: duration > 0 ? duration : undefined,
      action: options?.action,
      dismissible,
      timestamp: Date.now(),
    };

    // Store notification
    this.notifications.set(id, notification);

    // Emit add event
    this.emit({
      ...notification,
      eventType: 'add',
    });

    // Auto-dismiss if duration is set
    if (duration > 0) {
      setTimeout(() => {
        this.dismiss(id);
      }, duration);
    }

    return id;
  }

  /**
   * Dismiss notification by ID
   */
  dismiss(id: string): void {
    const notification = this.notifications.get(id);

    if (notification) {
      this.notifications.delete(id);

      // Emit remove event
      this.emit({
        ...notification,
        eventType: 'remove',
      });
    }
  }

  /**
   * Dismiss all notifications
   */
  dismissAll(): void {
    const ids = Array.from(this.notifications.keys());
    ids.forEach((id) => this.dismiss(id));
  }

  /**
   * Get notification by ID
   */
  get(id: string): Notification | undefined {
    return this.notifications.get(id);
  }

  /**
   * Get all notifications
   */
  getAll(): Notification[] {
    return Array.from(this.notifications.values());
  }

  /**
   * Get notifications by type
   */
  getByType(type: NotificationType): Notification[] {
    return Array.from(this.notifications.values()).filter((n) => n.type === type);
  }

  /**
   * Count notifications
   */
  count(): number {
    return this.notifications.size;
  }

  /**
   * Check if has notifications
   */
  hasNotifications(): boolean {
    return this.notifications.size > 0;
  }

  /**
   * Clear all notifications
   */
  clear(): void {
    this.dismissAll();
  }
}

// ============ SINGLETON INSTANCE ============

/**
 * Global notification service instance
 */
export const notificationService = new NotificationService();

/**
 * Quick access methods
 */
export const notify = {
  success: notificationService.success.bind(notificationService),
  error: notificationService.error.bind(notificationService),
  warning: notificationService.warning.bind(notificationService),
  info: notificationService.info.bind(notificationService),
  dismiss: notificationService.dismiss.bind(notificationService),
  dismissAll: notificationService.dismissAll.bind(notificationService),
};

export { NotificationService };
