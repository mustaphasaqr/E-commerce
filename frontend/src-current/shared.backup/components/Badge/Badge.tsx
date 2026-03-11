/**
 * Badge Component
 * Small label/tag component
 */

import React, { ReactNode } from 'react';
import styles from './Badge.module.css';

/**
 * Badge variants
 */
export type BadgeVariant =
  | 'primary'
  | 'secondary'
  | 'success'
  | 'warning'
  | 'danger'
  | 'info';

/**
 * Badge sizes
 */
export type BadgeSize = 'sm' | 'md' | 'lg';

/**
 * Badge props
 */
export interface BadgeProps {
  children: ReactNode;
  variant?: BadgeVariant;
  size?: BadgeSize;
  className?: string;
  icon?: ReactNode;
  onRemove?: () => void;
}

/**
 * Badge component
 */
export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'primary',
  size = 'md',
  className,
  icon,
  onRemove,
}) => {
  const badgeClasses = [
    styles.badge,
    styles[`variant-${variant}`],
    styles[`size-${size}`],
    onRemove && styles.removable,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <span className={badgeClasses}>
      {icon && <span className={styles.icon}>{icon}</span>}
      <span>{children}</span>
      {onRemove && (
        <button
          type="button"
          className={styles.removeButton}
          onClick={onRemove}
          aria-label="Remove badge"
        >
          ✕
        </button>
      )}
    </span>
  );
};

/**
 * Status badge (dot + text)
 */
export interface StatusBadgeProps {
  status: 'active' | 'inactive' | 'pending' | 'error';
  label?: string;
  size?: 'sm' | 'md';
  className?: string;
}

/**
 * StatusBadge component
 */
export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  label,
  size = 'md',
  className,
}) => {
  const statusVariantMap: Record<typeof status, BadgeVariant> = {
    active: 'success',
    inactive: 'secondary',
    pending: 'warning',
    error: 'danger',
  };

  const statusLabelMap: Record<typeof status, string> = {
    active: 'Active',
    inactive: 'Inactive',
    pending: 'Pending',
    error: 'Error',
  };

  return (
    <Badge
      variant={statusVariantMap[status]}
      size={size}
      className={className}
      icon="●"
    >
      {label || statusLabelMap[status]}
    </Badge>
  );
};

/**
 * Count badge (for notifications, etc.)
 */
export interface CountBadgeProps {
  count: number;
  max?: number;
  color?: BadgeVariant;
  className?: string;
}

/**
 * CountBadge component (typically used as absolute positioned element)
 */
export const CountBadge: React.FC<CountBadgeProps> = ({
  count,
  max = 99,
  color = 'danger',
  className,
}) => {
  const displayCount = count > max ? `${max}+` : count;

  return (
    <Badge variant={color} size="sm" className={[styles.countBadge, className].filter(Boolean).join(' ')}>
      {displayCount}
    </Badge>
  );
};
